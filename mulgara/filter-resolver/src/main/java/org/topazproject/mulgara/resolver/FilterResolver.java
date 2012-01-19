/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.topazproject.mulgara.resolver;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.log4j.Logger;

import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;

import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.ConstraintIs;
import org.mulgara.query.ConstraintNegation;
import org.mulgara.query.ConstraintNotOccurs;
import org.mulgara.query.ConstraintOccurs;
import org.mulgara.query.ConstraintOccursLessThan;
import org.mulgara.query.ConstraintOccursMoreThan;
import org.mulgara.query.LocalNode;
import org.mulgara.query.QueryException;
import org.mulgara.query.SingleTransitiveConstraint;
import org.mulgara.query.TransitiveConstraint;
import org.mulgara.query.WalkConstraint;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.AbstractXAResource;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/** 
 * A Mulgara resolver for filtering operations. This resolver "wraps" around the system resolver,
 * passing all inserts, deletes, and queries through to the underlying graph (after changing the
 * graph name), and then notifying the list of registered {@link FilterHandler handlers} whenever
 * a graph is created, removed, or modified. The name of the underlying graph, as well as possibly
 * other parameters, are specified in the graph's URI. The syntax of URI's for the graphs handled
 * by this resolver is:
 * <pre>
 *   &lt;dbURI&gt;#filter:graph=&lt;graphName&gt;(;&lt;pN&gt;=&lt;valueN&gt;)*
 * </pre>
 * The &lt;graphName&gt; species the name of the (system) graph this graph is to wrap; the optional
 * additional parameters are currently unused and are ignored. For example:
 * <pre>
 *   rmi://localhost/fedora#filter:graph=ri
 * </pre>
 * 
 * @author Ronald Tschalär
 */
public class FilterResolver implements Resolver {
  /** the graph type we handle */
  public static final URI GRAPH_TYPE = URI.create("http://topazproject.org/graphs#filter");

  private static final Logger logger = Logger.getLogger(FilterResolver.class);
  private static final Map    graphTranslationCache = new HashMap();

  private final URI             dbURI;
  private final long            sysGraphType;
  private final ResolverSession resolverSession;
  private final Resolver        systemResolver;
  private final FilterHandler[] handlers;
  private final XAResource      xaRes;

  /** 
   * Create a new FilterResolver instance. 
   * 
   * @param dbURI           the absolute URI of the database; used as the base for creating the
   *                        absolute URI of a graph
   * @param sysGraphType    the system-graph type; used when creating a new graph
   * @param systemResolver  the system-resolver; used for creating and modifying graphs
   * @param resolverSession our environment; used for globalizing and localizing nodes
   * @param factory         the filter-resolver-factory we belong to
   * @param handlers        the filter handlers to use
   */
  FilterResolver(URI dbURI, long sysGraphType, Resolver systemResolver,
                 ResolverSession resolverSession, FilterResolverFactory factory,
                 FilterHandler[] handlers) {
    this.dbURI           = dbURI;
    this.sysGraphType    = sysGraphType;
    this.systemResolver  = systemResolver;
    this.resolverSession = resolverSession;
    this.handlers        = handlers;
    this.xaRes           = new MultiXAResource(factory, handlers);
  }

  /**
   * @return the updater's XAResource
   */
  public XAResource getXAResource() {
    return xaRes;
  }

  public void createModel(long graph, URI graphType) throws ResolverException, LocalizeException {
    // check graph type
    if (!graphType.equals(GRAPH_TYPE))
      throw new ResolverException("Unknown graph-type '" + graphType + "'");

    // get system graph type URI
    try {
      Node mtURI = resolverSession.globalize(sysGraphType);
      if (!(mtURI instanceof URIReference))
        throw new ResolverException("systemGraphType '" + mtURI + "' not a URIRef ");

      graphType = ((URIReference) mtURI).getURI();
    } catch (GlobalizeException ge) {
      throw new ResolverException("Failed to globalize SystemGraph Type", ge);
    }

    // convert filter graph uri to real graph uri
    URI filterGraphURI = toURI(graph);
    URI realGraphURI   = toRealGraphURI(filterGraphURI);
    if (logger.isDebugEnabled())
      logger.debug("Creating graph '" + realGraphURI + "' for '" + filterGraphURI + "'");

    // create the real graph (a no-op if it already exists)
    try {
      graph = resolverSession.localizePersistent(new URIReferenceImpl(realGraphURI, false));
      systemResolver.createModel(graph, graphType);
    } catch (LocalizeException le) {
      throw new ResolverException("Error localizing graph uri '" + realGraphURI + "'", le);
    }

    for (FilterHandler h : handlers)
      h.graphCreated(filterGraphURI, realGraphURI);
  }

  public void modifyModel(long graph, Statements statements, boolean occurs)
      throws ResolverException {
    URI filterGraphURI = toURI(graph);
    URI realGraphURI   = toRealGraphURI(filterGraphURI);
    if (logger.isDebugEnabled())
      logger.debug("Modifying graph '" + realGraphURI + "'");

    try {
      systemResolver.modifyModel(lookupRealNode(graph), statements, occurs);
    } catch (QueryException qe) {
      throw new ResolverException("Failed to look up graph", qe);
    }

    for (FilterHandler h : handlers)
      h.graphModified(filterGraphURI, realGraphURI, statements, occurs, resolverSession);
  }

  public void removeModel(long graph) throws ResolverException {
    URI filterGraphURI = toURI(graph);
    URI realGraphURI   = toRealGraphURI(filterGraphURI);

    if (logger.isDebugEnabled())
      logger.debug("Removing graph '" + filterGraphURI + "'");

    for (FilterHandler h : handlers)
      h.graphRemoved(filterGraphURI, realGraphURI);
  }

  public Resolution resolve(Constraint constraint) throws QueryException {
    return systemResolver.resolve(translateGraph(constraint));
  }

  public void abort() {
    for (FilterHandler h : handlers)
      h.abort();
  }

  /**
   * Translate the graph (element 4) of the constraint to the underlying graph.
   *
   * According to tests, the only classes we ever see here are ConstraintImpl and
   * ConstraintNegation. However, to be safe, we handle all known implementations of
   * Constraint here.
   */
  private Constraint translateGraph(Constraint constraint) throws QueryException {
    if (logger.isDebugEnabled())
      logger.debug("translating constraint class '" + constraint.getClass().getName() + "'");

    // handle the non-leaf constraints first

    if (constraint instanceof WalkConstraint) {
      WalkConstraint wc = (WalkConstraint) constraint;
      return new WalkConstraint(translateGraph(wc.getAnchoredConstraint()),
                                translateGraph(wc.getUnanchoredConstraint()));
    }

    if (constraint instanceof TransitiveConstraint) {
      TransitiveConstraint tc = (TransitiveConstraint) constraint;
      return new TransitiveConstraint(translateGraph(tc.getAnchoredConstraint()),
                                      translateGraph(tc.getUnanchoredConstraint()));
    }

    if (constraint instanceof SingleTransitiveConstraint) {
      SingleTransitiveConstraint stc = (SingleTransitiveConstraint) constraint;
      return new SingleTransitiveConstraint(translateGraph(stc.getTransConstraint()));
    }

    // is leaf constraint, so get elements and translate graph

    ConstraintElement subj = constraint.getElement(0);
    ConstraintElement pred = constraint.getElement(1);
    ConstraintElement obj  = constraint.getElement(2);

    LocalNode graph = (LocalNode) constraint.getElement(3);
    graph = lookupRealNode(graph);

    if (logger.isDebugEnabled()) {
      logger.debug("Resolved graph '" + graph + "'");
      logger.debug("constraint: subj='" + constraint.getElement(0) + "'");
      logger.debug("constraint: pred='" + constraint.getElement(1) + "'");
      logger.debug("constraint:  obj='" + constraint.getElement(2) + "'");
    }

    // handle each constraint type

    if (constraint instanceof ConstraintImpl)
      return new ConstraintImpl(subj, pred, obj, graph);

    if (constraint instanceof ConstraintIs)
      return new ConstraintIs(subj, obj, graph);

    if (constraint instanceof ConstraintNegation) {
      ConstraintNegation cn = (ConstraintNegation) constraint;
      Constraint inner;
      if (cn.isInnerConstraintIs())
        inner = new ConstraintIs(subj, obj, graph);
      else
        inner = new ConstraintImpl(subj, pred, obj, graph);
      return new ConstraintNegation(inner);
    }

    if (constraint instanceof ConstraintOccurs)
      return new ConstraintOccurs(subj, obj, graph);
    if (constraint instanceof ConstraintNotOccurs)
      return new ConstraintNotOccurs(subj, obj, graph);
    if (constraint instanceof ConstraintOccursLessThan)
      return new ConstraintOccursLessThan(subj, obj, graph);
    if (constraint instanceof ConstraintOccursMoreThan)
      return new ConstraintOccursMoreThan(subj, obj, graph);

    throw new QueryException("Unknown constraint class '" + constraint.getClass().getName() + "'");
  }


  private long lookupRealNode(long graph) throws QueryException {
    return lookupRealNode(new LocalNode(graph)).getValue();
  }

  private LocalNode lookupRealNode(LocalNode graph) throws QueryException {
    // check cache
    LocalNode res = (LocalNode) graphTranslationCache.get(graph);
    if (res != null)
      return res;

    // nope, so convert to URI (globalize), rewrite URI, and convert back (localize)
    URI graphURI;
    try {
      graphURI = toURI(graph.getValue());
    } catch (ResolverException re) {
      throw new QueryException("Failed to get graph URI", re);
    }

    URI resURI = null;
    try {
      resURI = toRealGraphURI(graphURI);
      long resId = resolverSession.lookup(new URIReferenceImpl(resURI, false));
      res = new LocalNode(resId);
    } catch (ResolverException re) {
      throw new QueryException("Failed to parse graph '" + graphURI + "'", re);
    } catch (LocalizeException le) {
      throw new QueryException("Couldn't localize graph '" + resURI + "'", le);
    }

    // cache and return the result
    if (logger.isDebugEnabled())
      logger.debug("Adding translation for graph '" + graphURI + "' -> '" + resURI + "'");

    graphTranslationCache.put(graph, res);
    return res;
  }

  /** 
   * Get the graph name from the uri, checking that the uri is properly formed.
   * 
   * @param uri the filter uri; must be of the form 
   *            &lt;dbURI&gt;#filter:graph=&lt;graphName&gt;;&lt;p2&gt;=&lt;value2&gt;
   * @return the graphName
   * @throws ResolverException if the uri is not properly formed
   */
  static String getGraphName(URI uri) throws ResolverException {
    String graphName = uri.getRawFragment();
    if (!graphName.startsWith("filter:"))
      throw new ResolverException("Graph-name '" + graphName + "' doesn't start with 'filter:'");

    for (String param : graphName.substring(7).split(";")) {
      if (param.startsWith("graph="))
        return param.substring(6);
    }

    throw new ResolverException("invalid graph name encountered: '" + uri + "' - must be of " +
                                "the form <dbURI>#filter:graph=<graph>");
  }

  private URI toURI(long graph) throws ResolverException {
    Node globalGraph = null;

    // Globalise the graph
    try {
      globalGraph = resolverSession.globalize(graph);
    } catch (GlobalizeException ge) {
      throw new ResolverException("Couldn't globalize graph", ge);
    }

    // Check that our node is a URIReference
    if (!(globalGraph instanceof URIReference))
      throw new ResolverException("Graph parameter " + globalGraph + " isn't a URI reference");

    // Get the URI from the globalised node
    return ((URIReference) globalGraph).getURI();
  }

  /**
   * <code>rmi://localhost/fedora#filter:graph=ri;ds=RELS-EXT</code> -&gt; <code>rmi://localhost/fedora#ri</code>
   */
  private URI toRealGraphURI(URI graph) throws ResolverException {
    // Note: URI.resolve() collapses the slashes on local:///foo, so we do this thing by hand
    String u = graph.getScheme() + "://" +
               (graph.getRawAuthority() != null ? graph.getRawAuthority() : "") +
               graph.getRawPath() +
               '#' + getGraphName(graph);
    return URI.create(u);
  }

  /**
   * A simple XAResource that delegates to a list of underlying XAResources. This is <em>not</em> a
   * full implementation; in particular, many aspects of error handling have been left out in the
   * assumption the the underlying XAResources will behave.
   */
  private static class MultiXAResource
      extends AbstractXAResource<AbstractXAResource.RMInfo<MultiXAResource.MultiTxInfo>, MultiXAResource.MultiTxInfo> {
    private final List<XAResource> xaResources = new ArrayList<XAResource>();

    private static class MultiTxInfo extends AbstractXAResource.TxInfo {
      final List<XAResource> xaResources = new ArrayList<XAResource>();
    }

    MultiXAResource(FilterResolverFactory factory, FilterHandler[] handlers) {
      super(10, factory);

      for (FilterHandler h : handlers) {
        XAResource res = h.getXAResource();
        if (res != null)
          xaResources.add(res);
      }
    }

    @Override
    protected RMInfo<MultiTxInfo> newResourceManager() {
      return new RMInfo<MultiTxInfo>();
    }

    @Override
    protected MultiTxInfo newTransactionInfo() {
      MultiTxInfo ti = new MultiTxInfo();
      ti.xaResources.addAll(xaResources);
      return ti;
    }

    @Override
    protected void doStart(MultiTxInfo tx, int flags, boolean isNew) throws XAException {
      for (XAResource xaRes : tx.xaResources)
        xaRes.start(tx.xid, flags);
    }

    @Override
    protected void doEnd(MultiTxInfo tx, int flags) throws XAException {
      for (XAResource xaRes : tx.xaResources)
        xaRes.end(tx.xid, flags);
    }

    @Override
    protected int doPrepare(MultiTxInfo tx) throws XAException {
      XAException exc = null;

      for (Iterator<XAResource> iter = tx.xaResources.iterator(); iter.hasNext(); ) {
        XAResource xaRes = iter.next();
        try {
          if (xaRes.prepare(tx.xid) == XA_RDONLY)
            iter.remove();
        } catch (XAException xae) {
          exc = xae;
          iter.remove();
          break;
        }
      }

      if (exc != null) {
        for (XAResource xaRes : tx.xaResources) {
          try {
            xaRes.rollback(tx.xid);
          } catch (XAException xae) {
            logger.warn("Error rolling back resource", xae);
          }
        }

        throw exc;
      }

      return tx.xaResources.isEmpty() ? XA_RDONLY : XA_OK;
    }

    @Override
    protected void doCommit(MultiTxInfo tx) throws XAException {
      XAException exc = null;

      for (XAResource xaRes : tx.xaResources) {
        try {
          xaRes.commit(tx.xid, false);
        } catch (XAException xae) {
          exc = xae;
          logger.warn("Error rolling back resource", xae);
        }
      }

      if (exc != null)
        throw exc;
    }

    @Override
    protected void doRollback(MultiTxInfo tx) throws XAException {
      XAException exc = null;

      for (XAResource xaRes : tx.xaResources) {
        try {
          xaRes.rollback(tx.xid);
        } catch (XAException xae) {
          exc = xae;
          logger.warn("Error rolling back resource", xae);
        }
      }

      if (exc != null)
        throw exc;
    }

    @Override
    public int getTransactionTimeout() throws XAException {
      int to = Integer.MAX_VALUE;

      for (XAResource xaRes : xaResources)
        to = Math.min(xaRes.getTransactionTimeout(), to);

      return to;
    }

    @Override
    public boolean setTransactionTimeout(int transactionTimeout) throws XAException {
      boolean res = true;

      for (XAResource xaRes : xaResources)
        res &= xaRes.setTransactionTimeout(transactionTimeout);

      return res;
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
      List<Xid> xids = new ArrayList<Xid>();

      for (XAResource xaRes : xaResources) {
        Xid[] l = xaRes.recover(flag);
        for (Xid xid : l)
          xids.add(xid);
      }

      return xids.toArray(new Xid[xids.size()]);
    }

    @Override
    protected void doForget(MultiTxInfo tx) throws XAException {
      for (XAResource xaRes : tx.xaResources)
        xaRes.forget(tx.xid);
    }
  }
}
