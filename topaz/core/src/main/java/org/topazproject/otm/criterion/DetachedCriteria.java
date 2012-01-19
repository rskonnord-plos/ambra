/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.criterion;

import java.net.URI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.event.PreInsertEventListener;
import org.topazproject.otm.event.PostLoadEventListener;
import org.topazproject.otm.mapping.Mapper;

/**
 * DetachedCriteria is similar to Criteria except that it can exist without a Session. This
 * makes it ideal for persistance. DetachedCriteria can be converted to an executable Criteria by
 * calling the {@link #getExecutableCriteria} method. DetachedCriteria is built the same
 * way as Criteria, ie. by {@link #add}ing {@link org.topazproject.otm.criterion.Criterion}
 * objects.
 *
 * <p> For persistence, make sure that {@link org.topazproject.otm.criterion.Criterion#MODEL} is 
 * configured in the SessionFactory. Also note that not all of the setter methods are for use
 * by application code. They are there to support a load from a triple-store.</p>
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Criterion.NS + "Criteria", model = Criterion.MODEL)
@UriPrefix(Criterion.NS)
public class DetachedCriteria implements PreInsertEventListener, PostLoadEventListener {
  private static final Log        log               = LogFactory.getLog(DetachedCriteria.class);
  private static final String     NL                = System.getProperty("line.separator");

  private           String        alias;
  private DetachedCriteria        parent;
  private Integer                 maxResults;
  private Integer                 firstResult;
  @Predicate(storeAs = Predicate.StoreAs.rdfSeq)
  private List<Criterion>         criterionList     = new ArrayList<Criterion>();
  @Predicate(storeAs = Predicate.StoreAs.rdfSeq)
  private List<Order>             orderList         = new ArrayList<Order>();
  @Predicate(storeAs = Predicate.StoreAs.rdfSeq)
  private List<DetachedCriteria>  childCriteriaList = new ArrayList<DetachedCriteria>();

  // Only valid in the root criteria
  @Predicate(storeAs = Predicate.StoreAs.rdfSeq)
  private List<Order>             rootOrderList     = new ArrayList<Order>();

  @Embedded
  public DeAliased da = new DeAliased();

  /**
   * The id field used for persistence. Ignored otherwise.
   */
  @Id
  @GeneratedValue(uriPrefix = Criterion.NS + "Criteria/Id/")
  public URI criteriaId;

  /**
   * Creates a new DetachedCriteria object. 
   *
   */
  public DetachedCriteria() {
  }

  /**
   * Creates a new DetachedCriteria object.
   *
   * @param entity the entity for which the criteria is created. It could be an entity name or the
   * class name. 
   */
  public DetachedCriteria(String entity) {
    this.alias   = entity;
    this.parent  = null;
  }

  private DetachedCriteria(DetachedCriteria parent, String path) {
    this.alias    = path;
    this.parent   = parent;
  }

  /**
   * Creates an executable criteria in the given session. Must be called only  on a root
   * criteria.
   *
   * @param session the execution context
   *
   * @return a newly created Criteria object
   *
   * @throws OtmException on an error
   * @throws UnsupportedOperationException if called on a child
   */
  public Criteria getExecutableCriteria(Session session)
                                 throws OtmException, UnsupportedOperationException {
    if (parent != null)
      throw new UnsupportedOperationException("Can't create executable Criteria from a detached child criteria");

    ClassMetadata cm = session.getSessionFactory().getClassMetadata(alias);

    if (cm == null)
      throw new OtmException("Entity name '" + alias + "' is not found in session factory");

    Criteria c = session.createCriteria(cm.getSourceClass());
    copyTo(c);
    c.getOrderPositions().clear();
    c.getOrderPositions().addAll(rootOrderList);

    return c;
  }

  private void copyTo(Criteria c) throws OtmException {
    if (maxResults != null)
      c.setMaxResults(maxResults);

    if (firstResult != null)
      c.setFirstResult(firstResult);

    for (Criterion cr : criterionList)
      c.add(cr);

    for (Order or : orderList)
      c.addOrder(or);

    for (DetachedCriteria dc : childCriteriaList)
      dc.copyTo(c.createCriteria(dc.getAlias()));
  }

  /**
   * Creates a new sub-criteria for an association.
   *
   * @param path to the association
   *
   * @return the newly created sub-criteria
   *
   * @throws OtmException on an error
   */
  public DetachedCriteria createCriteria(String path) throws OtmException {
    DetachedCriteria c = new DetachedCriteria(this, path);
    childCriteriaList.add(c);

    return c;
  }

  /**
   * Get parent. 
   *
   * @return parent as Criteria.
   */
  public DetachedCriteria getParent() {
    return parent;
  }

  /**
   * Set parent. For use by persistence.
   *
   * DO NOT USE DIRECTLY. Use {@link #createCriteria} instead on the parent.
   *
   * @param parent the value to set.
   */
  public void setParent(DetachedCriteria parent) {
    this.parent = parent;
  }

  /**
   * Adds a Criterion.
   *
   * @param criterion the criterion to add
   *
   * @return this for method call chaining
   */
  public DetachedCriteria add(Criterion criterion) {
    criterionList.add(criterion);

    return this;
  }

  /**
   * Adds an ordering criterion.
   *
   * @param order the order definition
   *
   * @return this for method call chaining
   */
  public DetachedCriteria addOrder(Order order) {
    orderList.add(order);
    getRoot().getRootOrderList().add(order);

    return this;
  }

  private ClassMetadata getClassMetadata(SessionFactory sf) {
    if (parent == null)
      return sf.getClassMetadata(alias);
    ClassMetadata cm = parent.getClassMetadata(sf);
    if (cm == null)
      return null;
    Mapper m = cm.getMapperByName(alias);
    return (m == null) ? null : sf.getClassMetadata(m.getComponentType());
  }

  /*
   * inherited javadoc
   */
  public void onPreInsert(Session session, Object object) {
    assert this == object;
    SessionFactory sf = session.getSessionFactory();

    if (parent == null) {
      ClassMetadata cm = sf.getClassMetadata(alias);

      if (cm == null)
        log.warn("onPreInsert: Entity name '" + alias 
            + "' is not found in session factory.");
      else {
        da.rdfType = URI.create(cm.getType());
        da.predicateUri = null;
        da.inverse = false;

        if (log.isDebugEnabled())
          log.debug("onPreInsert: converted entity '" + alias + "' to <" + da.rdfType + ">");

        for (Criterion cr : criterionList)
          cr.onPreInsert(this, cm);

        for (Order or : orderList)
          or.onPreInsert(this, cm);
      }
    } else {
      ClassMetadata cm = parent.getClassMetadata(sf);
      if (cm == null)
        log.warn("onPreInsert: Parent of '" + alias + "' not found in session factory");
      else {
        Mapper m = cm.getMapperByName(alias);
        if (m == null)
          log.warn("onPreInsert: Field '" + alias + "' not found in " + cm);
        else {
          cm = sf.getClassMetadata(m.getComponentType());
          da.rdfType = (cm == null) ? null : URI.create(cm.getType());
          da.predicateUri = URI.create(m.getUri());
          da.inverse = m.hasInverseUri();
          if (log.isDebugEnabled())
            log.debug("onPreInsert: converted field '" + alias + "' to "  + da);

          for (Criterion cr : criterionList)
            cr.onPreInsert(this, cm);

          for (Order or : orderList)
            or.onPreInsert(this, cm);
        }
      }
    }
  }

  /*
   * inherited javadoc
   */
  public void onPostLoad(Session session, Object object) {
    assert this == object;
    SessionFactory sf = session.getSessionFactory();

    if (parent == null) {
      Class cl = sf.mostSpecificSubClass(Object.class, Collections.singleton(ser(da.rdfType)), true);

      if (cl == null) {
        log.warn("onPostLoad: A class matching the rdf:type <" + da.rdfType 
          + "> is not found in session factory. Entity name will remain as '" + alias + "'");
      } else {
        ClassMetadata cm = sf.getClassMetadata(cl);
        alias = cm.getName();

        if (log.isDebugEnabled())
          log.debug("onPostLoad: converted rdfType <" + da.rdfType + "> to entity '" + alias + "'");

        for (Criterion cr : criterionList)
          cr.onPostLoad(this, cm);

        for (Order or : orderList)
          or.onPostLoad(this, cm);
      }
    } else {
      ClassMetadata cm = parent.getClassMetadata(sf);
      if (cm == null)
        log.warn("onPostLoad: Parent of '" + alias + "' not found in session factory");
      else {
        Mapper m = cm.getMapperByUri(ser(da.predicateUri), da.inverse, ser(da.rdfType));
        if (m == null)
          log.warn("onPostLoad: A field matching " + da +  " not found in " + cm);
        else {
          alias = m.getName();

          if (log.isDebugEnabled())
            log.debug("onPostLoad: converted " + da + " to '" + alias + "'");

          cm = sf.getClassMetadata(m.getComponentType());

          for (Criterion cr : criterionList)
            cr.onPostLoad(this, cm);

          for (Order or : orderList)
            or.onPostLoad(this, cm);
        }
      }
    }
  }

  private static String ser(URI u) {
    return (u == null) ? null : u.toString();
  }

  /**
   * Gets the list of child Criteria.
   *
   * @return list of child Criteria
   */
  public List<DetachedCriteria> getChildCriteriaList() {
    return childCriteriaList;
  }

  /**
   * Sets the list of child Criteria. For use by persistence.
   *
   *
   * @param list of child Criteria
   */
  public void setChildCriteriaList(List<DetachedCriteria> list) {
    childCriteriaList = list;
  }

  /**
   * Gets the list of Criterions.
   *
   * @return list of Criterions
   */
  public List<Criterion> getCriterionList() {
    return criterionList;
  }

  /**
   * Sets the list of Criterions. For use by persitence.
   *
   * DO NOT USE DIRECTLY. Use {@link #add} instead.
   *
   * @param list of Criterions
   */
  public void setCriterionList(List<Criterion> list) {
    criterionList = list;
  }

  /**
   * Gets the list of Order definitions.
   *
   * @return lis of Order dedinitions
   */
  public List<Order> getOrderList() {
    return orderList;
  }

  /**
   * Sets the list of Order definitions. For use by persistence.
   *
   * DO NOT USE DIRECTLY. Use {@link #addOrder} instead.
   *
   * @param list of Order dedinitions
   */
  public void setOrderList(List<Order> list) {
    orderList = list;
  }

  /**
   * Gets the root order list.
   *
   * @return the root order list
   */
  public List<Order> getRootOrderList() {
    return rootOrderList;
  }

  /**
   * Gets the root order list. For use by persistence.
   *
   * DO NOT USE DIRECTLY. Use {@link #addOrder} instead.
   *
   * @param list the root order list
   */
  public void setRootOrderList(List<Order> list) {
    rootOrderList = list;
  }

  /**
   * Set a limit upon the number of objects to be retrieved.
   *
   * @param maxResults the maximum number of results
   *
   * @return this (for method chaining)
   */
  public DetachedCriteria setMaxResults(Integer maxResults) {
    this.maxResults = maxResults;

    return this;
  }

  /**
   * Set the first result to be retrieved.
   *
   * @param firstResult the first result to retrieve, numbered from <tt>0</tt>
   *
   * @return this (for method chaining)
   */
  public DetachedCriteria setFirstResult(Integer firstResult) {
    this.firstResult = firstResult;

    return this;
  }

  /**
   * Get a limit upon the number of objects to be retrieved.
   *
   * @return the maximum number of results
   */
  public Integer getMaxResults() {
    return maxResults;
  }

  /**
   * Get the first result to be retrieved.
   *
   * @return the first result to retrieve, numbered from <tt>0</tt>
   */
  public Integer getFirstResult() {
    return firstResult;
  }

  private DetachedCriteria getRoot() {
    return (parent == null) ? this : parent.getRoot();
  }

  /**
   * Get alias.
   *
   * @return alias as String.
   */
  public String getAlias() {
    return alias;
  }

  /**
   * Set alias. For use by persistence
   *
   * DO NOT USE DIRECTLY. Use the constructor instead.
   *
   * @param alias the value to set.
   */
  public void setAlias(String alias) {
    this.alias = alias;
  }

  /**
   * Return the list of parameter names.
   *
   * @return the set of names; will be empty if there are no parameters
   */
  public Set<String> getParameterNames() {
    if (parent != null)
      return parent.getParameterNames();

    return getParameterNames(new HashSet<String>());
  }

  private Set<String> getParameterNames(Set<String> paramNames) {
    for (DetachedCriteria dc : childCriteriaList)
      dc.getParameterNames(paramNames);

    for (Criterion c : criterionList)
      paramNames.addAll(c.getParamNames());

    return paramNames;
  }

  public String toString() {
    return toString("");
  }

  /**
   * @param indent the string to prefix every line with
   * @return a string representation of this detached-criteria
   */
  public String toString(String indent) {
    StringBuilder sb = new StringBuilder(50);
    sb.append(getAlias()).append(": ");

    for (Criterion c : getCriterionList())
      sb.append(NL).append(indent).append(c);
    for (DetachedCriteria c : getChildCriteriaList())
      sb.append(NL).append(indent).append(c.toString(indent + "  "));
    for (Order o : getOrderList())
      sb.append(NL).append(indent).append(o);

    return sb.toString();
  }

  @UriPrefix(Criterion.NS)
  public static class DeAliased {
    public URI rdfType;
    public URI predicateUri;
    public boolean inverse;

    public String toString() {
      return "[predicate: <" + predicateUri
            + ">, rdf:type: <" + rdfType
            + ">, inverse: " + inverse
            + "]";
    }
  }
}
