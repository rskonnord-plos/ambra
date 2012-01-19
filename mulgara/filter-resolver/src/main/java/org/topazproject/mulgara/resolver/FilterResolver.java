/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
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
 * passing all inserts, deletes, and queries through to the underlying model (after changing the
 * model name), and then notifying the list of registered {@link FilterHandler handlers} whenever
 * a model is created, removed, or modified. The name of the underlying model, as well as possibly
 * other parameters, are specified in the model's URI. The syntax of URI's for the models handled
 * by this resolver is:
 * <pre>
 *   &lt;dbURI&gt;#filter:model=&lt;modelName&gt;(;&lt;pN&gt;=&lt;valueN&gt;)*
 * </pre>
 * The &lt;modelName&gt; species the name of the (system) model this model is to wrap; the optional
 * additional parameters are currently unused and are ignored. For example:
 * <pre>
 *   rmi://localhost/fedora#filter:model=ri
 * </pre>
 * 
 * @author Ronald Tschalär
 */
public class FilterResolver implements Resolver {
  /** the model type we handle */
  public static final URI MODEL_TYPE = URI.create("http://topazproject.org/models#filter");

  private static final Logger logger = Logger.getLogger(FilterResolver.class);
  private static final Map    modelTranslationCache = new HashMap();

  private final URI             dbURI;
  private final long            sysModelType;
  private final ResolverSession resolverSession;
  private final Resolver        systemResolver;
  private final FilterHandler[] handlers;
  private final XAResource      xaRes;

  /** 
   * Create a new FilterResolver instance. 
   * 
   * @param dbURI           the absolute URI of the database; used as the base for creating the
   *                        absolute URI of a model
   * @param sysModelType    the system-model type; used when creating a new model
   * @param systemResolver  the system-resolver; used for creating and modifying models
   * @param resolverSession our environment; used for globalizing and localizing nodes
   * @param factory         the filter-resolver-factory we belong to
   * @param handlers        the filter handlers to use
   */
  FilterResolver(URI dbURI, long sysModelType, Resolver systemResolver,
                 ResolverSession resolverSession, FilterResolverFactory factory,
                 FilterHandler[] handlers) {
    this.dbURI           = dbURI;
    this.sysModelType    = sysModelType;
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

  public void createModel(long model, URI modelType) throws ResolverException, LocalizeException {
    // check model type
    if (!modelType.equals(MODEL_TYPE))
      throw new ResolverException("Unknown model-type '" + modelType + "'");

    // get system model type URI
    try {
      Node mtURI = resolverSession.globalize(sysModelType);
      if (!(mtURI instanceof URIReference))
        throw new ResolverException("systemModelType '" + mtURI + "' not a URIRef ");

      modelType = ((URIReference) mtURI).getURI();
    } catch (GlobalizeException ge) {
      throw new ResolverException("Failed to globalize SystemModel Type", ge);
    }

    // convert filter model uri to real model uri
    URI filterModelURI = toURI(model);
    URI realModelURI   = toRealModelURI(filterModelURI);
    if (logger.isDebugEnabled())
      logger.debug("Creating model '" + realModelURI + "'");

    // create the real model (a no-op if it already exists)
    try {
      model = resolverSession.localizePersistent(new URIReferenceImpl(realModelURI, false));
      systemResolver.createModel(model, modelType);
    } catch (LocalizeException le) {
      throw new ResolverException("Error localizing model uri '" + realModelURI + "'", le);
    }

    for (FilterHandler h : handlers)
      h.modelCreated(filterModelURI, realModelURI);
  }

  public void modifyModel(long model, Statements statements, boolean occurs)
      throws ResolverException {
    URI filterModelURI = toURI(model);
    URI realModelURI   = toRealModelURI(filterModelURI);
    if (logger.isDebugEnabled())
      logger.debug("Modifying model '" + realModelURI + "'");

    try {
      systemResolver.modifyModel(lookupRealNode(model), statements, occurs);
    } catch (QueryException qe) {
      throw new ResolverException("Failed to look up model", qe);
    }

    for (FilterHandler h : handlers)
      h.modelModified(filterModelURI, realModelURI, statements, occurs, resolverSession);
  }

  public void removeModel(long model) throws ResolverException {
    URI filterModelURI = toURI(model);
    URI realModelURI   = toRealModelURI(filterModelURI);

    if (logger.isDebugEnabled())
      logger.debug("Removing model '" + filterModelURI + "'");

    for (FilterHandler h : handlers)
      h.modelRemoved(filterModelURI, realModelURI);
  }

  public Resolution resolve(Constraint constraint) throws QueryException {
    return systemResolver.resolve(translateModel(constraint));
  }

  public void abort() {
    for (FilterHandler h : handlers)
      h.abort();
  }

  /**
   * Translate the model (element 4) of the constraint to the underlying model.
   *
   * According to tests, the only classes we ever see here are ConstraintImpl and
   * ConstraintNegation. However, to be safe, we handle all known implementations of
   * Constraint here.
   */
  private Constraint translateModel(Constraint constraint) throws QueryException {
    if (logger.isDebugEnabled())
      logger.debug("translating constraint class '" + constraint.getClass().getName() + "'");

    // handle the non-leaf constraints first

    if (constraint instanceof WalkConstraint) {
      WalkConstraint wc = (WalkConstraint) constraint;
      return new WalkConstraint(translateModel(wc.getAnchoredConstraint()),
                                translateModel(wc.getUnanchoredConstraint()));
    }

    if (constraint instanceof TransitiveConstraint) {
      TransitiveConstraint tc = (TransitiveConstraint) constraint;
      return new TransitiveConstraint(translateModel(tc.getAnchoredConstraint()),
                                      translateModel(tc.getUnanchoredConstraint()));
    }

    if (constraint instanceof SingleTransitiveConstraint) {
      SingleTransitiveConstraint stc = (SingleTransitiveConstraint) constraint;
      return new SingleTransitiveConstraint(translateModel(stc.getTransConstraint()));
    }

    // is leaf constraint, so get elements and translate model

    ConstraintElement subj = constraint.getElement(0);
    ConstraintElement pred = constraint.getElement(1);
    ConstraintElement obj  = constraint.getElement(2);

    LocalNode model = (LocalNode) constraint.getElement(3);
    model = lookupRealNode(model);

    if (logger.isDebugEnabled()) {
      logger.debug("Resolved model '" + model + "'");
      logger.debug("constraint: subj='" + constraint.getElement(0) + "'");
      logger.debug("constraint: pred='" + constraint.getElement(1) + "'");
      logger.debug("constraint:  obj='" + constraint.getElement(2) + "'");
    }

    // handle each constraint type

    if (constraint instanceof ConstraintImpl)
      return new ConstraintImpl(subj, pred, obj, model);

    if (constraint instanceof ConstraintIs)
      return new ConstraintIs(subj, obj, model);

    if (constraint instanceof ConstraintNegation) {
      ConstraintNegation cn = (ConstraintNegation) constraint;
      Constraint inner;
      if (cn.isInnerConstraintIs())
        inner = new ConstraintIs(subj, obj, model);
      else
        inner = new ConstraintImpl(subj, pred, obj, model);
      return new ConstraintNegation(inner);
    }

    if (constraint instanceof ConstraintOccurs)
      return new ConstraintOccurs(subj, obj, model);
    if (constraint instanceof ConstraintNotOccurs)
      return new ConstraintNotOccurs(subj, obj, model);
    if (constraint instanceof ConstraintOccursLessThan)
      return new ConstraintOccursLessThan(subj, obj, model);
    if (constraint instanceof ConstraintOccursMoreThan)
      return new ConstraintOccursMoreThan(subj, obj, model);

    throw new QueryException("Unknown constraint class '" + constraint.getClass().getName() + "'");
  }


  private long lookupRealNode(long model) throws QueryException {
    return lookupRealNode(new LocalNode(model)).getValue();
  }

  private LocalNode lookupRealNode(LocalNode model) throws QueryException {
    // check cache
    LocalNode res = (LocalNode) modelTranslationCache.get(model);
    if (res != null)
      return res;

    // nope, so convert to URI (globalize), rewrite URI, and convert back (localize)
    URI modelURI;
    try {
      modelURI = toURI(model.getValue());
    } catch (ResolverException re) {
      throw new QueryException("Failed to get model URI", re);
    }

    URI resURI = null;
    try {
      resURI = toRealModelURI(modelURI);
      long resId = resolverSession.lookup(new URIReferenceImpl(resURI, false));
      res = new LocalNode(resId);
    } catch (ResolverException re) {
      throw new QueryException("Failed to parse model '" + modelURI + "'", re);
    } catch (LocalizeException le) {
      throw new QueryException("Couldn't localize model '" + resURI + "'", le);
    }

    // cache and return the result
    if (logger.isDebugEnabled())
      logger.debug("Adding translation for model '" + modelURI + "' -> '" + resURI + "'");

    modelTranslationCache.put(model, res);
    return res;
  }

  /** 
   * Get the model name from the uri, checking that the uri is properly formed.
   * 
   * @param uri the filter uri; must be of the form 
   *            &lt;dbURI&gt;#filter:model=&lt;modelName&gt;;&lt;p2&gt;=&lt;value2&gt;
   * @return the modelName
   * @throws ResolverException if the uri is not properly formed
   */
  static String getModelName(URI uri) throws ResolverException {
    String modelName = uri.getRawFragment();
    if (!modelName.startsWith("filter:"))
      throw new ResolverException("Model-name '" + modelName + "' doesn't start with 'filter:'");

    String[] params = modelName.substring(7).split(";");
    for (int idx = 0; idx < params.length; idx++) {
      if (params[idx].startsWith("model="))
        return params[idx].substring(6);
    }

    throw new ResolverException("invalid model name encountered: '" + uri + "' - must be of " +
                                "the form <dbURI>#filter:model=<model>");
  }

  private URI toURI(long model) throws ResolverException {
    Node globalModel = null;

    // Globalise the model
    try {
      globalModel = resolverSession.globalize(model);
    } catch (GlobalizeException ge) {
      throw new ResolverException("Couldn't globalize model", ge);
    }

    // Check that our node is a URIReference
    if (!(globalModel instanceof URIReference))
      throw new ResolverException("Model parameter " + globalModel + " isn't a URI reference");

    // Get the URI from the globalised node
    return ((URIReference) globalModel).getURI();
  }

  /**
   * <code>rmi://localhost/fedora#filter:model=ri;ds=RELS-EXT</code> -&gt; <code>rmi://localhost/fedora#ri</code>
   */
  private URI toRealModelURI(URI model) throws ResolverException {
    // Note: URI.resolve() collapses the slashes on local:///foo, so we do this thing by hand
    String u = dbURI.getScheme() + ':' + dbURI.getRawSchemeSpecificPart() +
               '#' + getModelName(model);
    return URI.create(u);
  }

  /**
   * A simple XAResource that delegates to a list of underlying XAResources. This is <em>not</em> a
   * full implementation; in particular, many aspects of error handling have been left out in the
   * assumption the the underlying XAResources will behave.
   */
  private static class MultiXAResource
      extends AbstractXAResource<AbstractXAResource.RMInfo<MultiXAResource.MultiTxInfo>, MultiXAResource.MultiTxInfo> {
    private static class MultiTxInfo extends AbstractXAResource.TxInfo {
      final List<XAResource> xaResources = new ArrayList<XAResource>();
    }

    MultiXAResource(FilterResolverFactory factory, FilterHandler[] handlers) {
      super(10, factory, newTxInfo(handlers));
    }

    @Override
    protected RMInfo<MultiTxInfo> newResourceManager() {
      return new RMInfo<MultiTxInfo>();
    }

    private static MultiTxInfo newTxInfo(FilterHandler[] handlers) {
      MultiTxInfo ti = new MultiTxInfo();

      for (FilterHandler h : handlers) {
        XAResource res = h.getXAResource();
        if (res != null)
          ti.xaResources.add(res);
      }

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

      for (XAResource xaRes : tmpTxInfo.xaResources)
        to = Math.min(xaRes.getTransactionTimeout(), to);

      return to;
    }

    @Override
    public boolean setTransactionTimeout(int transactionTimeout) throws XAException {
      boolean res = true;

      for (XAResource xaRes : tmpTxInfo.xaResources)
        res &= xaRes.setTransactionTimeout(transactionTimeout);

      return res;
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
      List<Xid> xids = new ArrayList<Xid>();

      for (XAResource xaRes : tmpTxInfo.xaResources) {
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
