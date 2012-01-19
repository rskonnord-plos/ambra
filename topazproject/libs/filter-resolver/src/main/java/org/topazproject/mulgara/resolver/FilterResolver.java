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
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/** 
 * The factory for {@link FilterResolver}s. The model URI used for this filter
 * is:
 * <pre>
 *   &lt;dbURI&gt;#filter:model=&lt;modelName&gt;;ds=&lt;datastream&gt;
 * </pre>
 * For example:
 * <pre>
 *   rmi://localhost:/fedora#filter:model=ri;ds=RELS-EXT
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

  /** 
   * Create a new FilterResolver instance. 
   * 
   * @param dbURI           the absolute URI of the database; used as the base for creating the
   *                        absolute URI of a model
   * @param sysModelType    the system-model type; used when creating a new model
   * @param systemResolver  the system-resolver; used for creating and modifying models
   * @param resolverSession our environment; used for globalizing and localizing nodes
   * @param handlers        the filter handlers to use
   */
  FilterResolver(URI dbURI, long sysModelType, Resolver systemResolver,
                 ResolverSession resolverSession, FilterHandler[] handlers) {
    this.dbURI           = dbURI;
    this.sysModelType    = sysModelType;
    this.systemResolver  = systemResolver;
    this.resolverSession = resolverSession;
    this.handlers        = handlers;
  }

  /**
   * @return the updater's XAResource
   */
  public XAResource getXAResource() {
    return new MultiXAResource(handlers);
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

    for (int idx = 0; idx < handlers.length; idx++)
      handlers[idx].modelCreated(filterModelURI, realModelURI);
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

    for (int idx = 0; idx < handlers.length; idx++)
      handlers[idx].modelModified(filterModelURI, realModelURI, statements, occurs,
                                  resolverSession);
  }

  public void removeModel(long model) throws ResolverException {
    URI filterModelURI = toURI(model);
    URI realModelURI   = toRealModelURI(filterModelURI);

    if (logger.isDebugEnabled())
      logger.debug("Removing model '" + filterModelURI + "'");

    for (int idx = 0; idx < handlers.length; idx++)
      handlers[idx].modelRemoved(filterModelURI, realModelURI);
  }

  public Resolution resolve(Constraint constraint) throws QueryException {
    return systemResolver.resolve(translateModel(constraint));
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

  private static class MultiXAResource implements XAResource {
    private final List xaResources = new ArrayList();

    MultiXAResource(FilterHandler[] handlers) {
      for (int idx = 0; idx < handlers.length; idx++) {
        XAResource res = handlers[idx].getXAResource();
        if (res != null)
          xaResources.add(res);
      }
    }

    public void start(Xid xid, int flags) throws XAException {
      for (Iterator iter = xaResources.iterator(); iter.hasNext(); )
        ((XAResource) iter.next()).start(xid, flags);
    }

    public void end(Xid xid, int flags) throws XAException {
      for (Iterator iter = xaResources.iterator(); iter.hasNext(); )
        ((XAResource) iter.next()).end(xid, flags);
    }

    public int prepare(Xid xid) throws XAException {
      int res = XA_OK;

      for (Iterator iter = xaResources.iterator(); iter.hasNext(); )
        res |= ((XAResource) iter.next()).prepare(xid);

      return res;
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
      for (Iterator iter = xaResources.iterator(); iter.hasNext(); )
        ((XAResource) iter.next()).commit(xid, onePhase);
    }

    public void rollback(Xid xid) throws XAException {
      for (Iterator iter = xaResources.iterator(); iter.hasNext(); )
        ((XAResource) iter.next()).rollback(xid);
    }

    public int getTransactionTimeout() throws XAException {
      int to = Integer.MAX_VALUE;

      for (Iterator iter = xaResources.iterator(); iter.hasNext(); )
        to = Math.min(((XAResource) iter.next()).getTransactionTimeout(), to);

      return to;
    }

    public boolean setTransactionTimeout(int transactionTimeout) throws XAException {
      boolean res = true;

      for (Iterator iter = xaResources.iterator(); iter.hasNext(); )
        res &= ((XAResource) iter.next()).setTransactionTimeout(transactionTimeout);

      return res;
    }

    public Xid[] recover(int flag) throws XAException {
      List xids = new ArrayList();

      for (Iterator iter = xaResources.iterator(); iter.hasNext(); ) {
        Xid[] l = ((XAResource) iter.next()).recover(flag);
        for (int idx = 0; idx < l.length; idx++)
          xids.add(l);
      }

      return (Xid[]) xids.toArray(new Xid[xids.size()]);
    }

    public void forget(Xid xid) throws XAException {
      for (Iterator iter = xaResources.iterator(); iter.hasNext(); )
        ((XAResource) iter.next()).forget(xid);
    }

    public boolean isSameRM(XAResource xaResource) throws XAException {
      return xaResource == this;
    }
  }
}
