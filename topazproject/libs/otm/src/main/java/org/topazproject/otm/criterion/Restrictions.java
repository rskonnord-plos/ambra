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

/**
 * A factory class for creating Criterion objects for adding to a Criteria.
 *
 * @author Pradeep Krishnan
 */
public class Restrictions {
  /**
   * Creates a criterion where the id of the retrieved object is known.
   *
   * @param value the subject-uri/id of the object
   *
   * @return a newly created Criterion object
   */
  public static Criterion id(String value) {
    return new SubjectCriterion(value);
  }

  /**
   * Creates a criterion where an object property has a known value.
   *
   * @param name the property name
   * @param value its value
   *
   * @return a newly created Criterion object
   */
  public static Criterion eq(String name, Object value) {
    return new PredicateCriterion(name, value);
  }

  /**
   * Apply a "not equals" criterion.
   *
   * @param name the property name
   * @param value its value
   *
   * @return a newly created Criterion object
   */
  public static Criterion ne(String name, Object value) {
    return new MinusCriterion(name, value);
  }

  /**
   * Creates a criterion that finds the objects that match the property value transitively.
   *
   * @param name the property name
   * @param value its value
   *
   * @return a newly created Criterion object
   */
  public static Criterion trans(String name, Object value) {
    return new TransCriterion(name, value);
  }

  /**
   * Creates a Criterion that walks a property chain and finds the objects matching the
   * property value.
   *
   * @param name the property name
   * @param value its value
   *
   * @return a newly created Criterion object
   */
  public static Criterion walk(String name, Object value) {
    return new WalkCriterion(name, value);
  }

  /**
   * Group expressions together in a single conjunction (A and B and C...)
   *
   * @return Conjunction
   */
  public static Conjunction conjunction() {
    return new Conjunction();
  }

  /**
   * Group expressions together in a single disjunction (A or B or C...)
   *
   * @return Conjunction
   */
  public static Disjunction disjunction() {
    return new Disjunction();
  }
}
