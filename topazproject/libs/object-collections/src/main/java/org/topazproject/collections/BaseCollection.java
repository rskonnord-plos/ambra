/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.collections;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.List;

import org.topazproject.otm.Session;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.Junction;

/**
 * Define common routines for all otm collections. This class implements both the
 * java.util.Collection interface and the org.topazproject.otm.criterion.Criterion
 * interfaces.<p>
 *
 * This class would be called AbstractCollection except for a confusing naming
 * conflict with java.util.AbstractCollection.<p>
 *
 * TODO: Define a way to persist collection definitions
 *
 * @author Eric Brown
 */
abstract class BaseCollection<T> extends AbstractCollection<T> implements Criterion {
  private Criteria criteria;
  private Junction junction;

  /**
   * Create a base collection.<p>
   *
   * Stupid java did not create a way to get runtime information on parmertized types, so
   * we have to pass the parameterized type in a second time.
   *
   * @param session the session on which to base the collection on
   * @param clazz   the otm managed class returned by this collection (must match parameterization)
   * @throw OtmException if otm does not manage class or some other otm error occurs
   */
  BaseCollection(Session session, Class clazz, Junction junction) throws OtmException {
    criteria = session.createCriteria(clazz);
    this.junction = junction;
    criteria.add(junction);
  }

  /**
   * Add criterion to the list of criterion bein applied to this collection.
   *
   * @param c the criterion to add
   */
  protected BaseCollection<T> add(Criterion c) {
    junction.add(c);
    return this;
  }

  /**
   * Return the criteria object that will be used to query the session for a set of objects.
   * This can be used to tweak the order objects are returned or to tweak the collection in
   * other ways. Most uses of this in the context of collection are discouraged, however.
   *
   * @return The criteria object that will be used to query the session for a set of objects.
   */
  public Criteria getCriteria() {
    return criteria;
  }

  /**
   * <code>toItql()</code> is needed to implement the Criterion interface -- we do
   * this so that a Collection can be added as a Criterion in a SmartCollection.
   * It is not intended for a consumer of Collections to call this directly.
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    return junction.toItql(criteria, subjectVar, varPrefix);
  }

  /* The following methods are not strictly necessary... getCriteria().list() by itself
   * is sufficient. But making something called a "..Collection" actually be a
   * java.util.Collection seems more intuitive.
   */

  /**
   * Returns an iterator over the objects returned by this collection.
   *
   * @return an iterator over the objects returned by this collection.
   */
  public Iterator<T> iterator() {
    return (Iterator<T>) criteria.list().iterator();
  }

  /**
   * Returns the number of objects in this collection.
   *
   * @return the number of elements in this collection.
   */
  public int size() {
    return criteria.list().size();
  }
}
