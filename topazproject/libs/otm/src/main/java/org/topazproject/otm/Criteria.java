/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm;

import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.mapping.Mapper;

/**
 * An API for retrieving objects based on filtering and ordering conditions specified  using
 * {@link org.topazproject.otm.criterion.Criterion}.
 *
 * @author Pradeep Krishnan
 */
public class Criteria {
  private Session         session;
  private ClassMetadata   classMetadata;
  private Criteria        parent;
  private Mapper          mapping;
  private int             maxResults  = -1;
  private int             firstResult = -1;
  private List<Criterion> criterions  = new ArrayList<Criterion>();
  private List<Order>     orders      = new ArrayList<Order>();
  private List<Criteria>  children    = new ArrayList<Criteria>();

/**
   * Creates a new Criteria object. Called by {@link Session#createCriteria}.
   *
   * @param session The session that created it
   * @param parent The parent criteria for which this is a sub-criteria
   * @param mapping The mapping of the association field in parent 
   * @param classMetadata The class meta-data of this criteria
   */
  public Criteria(Session session, Criteria parent, Mapper mapping, ClassMetadata classMetadata) {
    this.session         = session;
    this.parent          = parent;
    this.mapping         = mapping;
    this.classMetadata   = classMetadata;
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
  public Criteria createCriteria(String path) throws OtmException {
    Criteria c = session.createCriteria(parent, path);
    children.add(c);

    return c;
  }

  /**
   * Get session.
   *
   * @return session as Session.
   */
  public Session getSession() {
    return session;
  }

  /**
   * Get class metadata.
   *
   * @return classMetadata as ClassMetadata.
   */
  public ClassMetadata getClassMetadata() {
    return classMetadata;
  }

  /**
   * Get parent.
   *
   * @return parent as Criteria.
   */
  public Criteria getParent() {
    return parent;
  }

  /**
   * Gets the mapping of the association field in parent.
   *
   * @return the mapper
   */
  public Mapper getMapping() {
    return mapping;
  }

  /**
   * Adds a Criterion.
   *
   * @param criterion the criterion to add
   *
   * @return this for method call chaining
   */
  public Criteria add(Criterion criterion) {
    criterions.add(criterion);

    return this;
  }

  /**
   * Adds an ordering criterion.
   *
   * @param order the order definition
   *
   * @return this for method call chaining
   */
  public Criteria addOrder(Order order) {
    orders.add(order);

    return this;
  }

  /**
   * Lists the objects satisfying this Criteria.
   *
   * @return the list of objects.
   *
   * @throws OtmException on an error
   */
  public List list() throws OtmException {
    return (parent != null) ? parent.list() : session.list(this);
  }

  /**
   * Gets the list of child Criteria.
   *
   * @return list of child Criteria
   */
  public List<Criteria> getChildren() {
    return children;
  }

  /**
   * Gets the list of Criterions.
   *
   * @return list of Criterions
   */
  public List<Criterion> getCriterionList() {
    return criterions;
  }

  /**
   * Gets the list of Order definitions.
   *
   * @return lis of Order dedinitions
   */
  public List<Order> getOrderList() {
    return orders;
  }

  /**
   * Set a limit upon the number of objects to be retrieved.
   *
   * @param maxResults the maximum number of results
   *
   * @return this (for method chaining)
   */
  public Criteria setMaxResults(int maxResults) {
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
  public Criteria setFirstResult(int firstResult) {
    this.firstResult = firstResult;

    return this;
  }

  /**
   * Get a limit upon the number of objects to be retrieved.
   *
   * @return the maximum number of results
   */
  public int getMaxResults() {
    return maxResults;
  }

  /**
   * Get the first result to be retrieved.
   *
   * @return the first result to retrieve, numbered from <tt>0</tt>
   */
  public int getFirstResult() {
    return firstResult;
  }
}
