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
   * Apply a "minus" criterion. Performs A - B.
   *
   * @param minuend subtract from
   * @param subtrahend subtract this
   *
   * @return a newly created Criterion object
   */
  public static Criterion minus(Criterion minuend, Criterion subtrahend) {
    return new MinusCriterion(minuend, subtrahend);
  }

  /**
   * Apply a "not" criterion.
   *
   * @param criterion the criterion to negate
   *
   * @return a newly created Criterion object
   */
  public static Criterion not(Criterion criterion) {
    return minus(new PredicateCriterion(), criterion);
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
    return not(eq(name, value));
  }

  /**
   * Apply an "exists" criterion.
   *
   * @param name the property name
   *
   * @return a newly created Criterion object
   */
  public static Criterion exists(String name) {
    return new PredicateCriterion(name);
  }

  /**
   * Apply a "not" criterion.
   *
   * @param name the property name
   *
   * @return a newly created Criterion object
   */
  public static Criterion notExists(String name) {
    return not(exists(name));
  }

  /**
   * Apply a "greater than" criterion.
   *
   * @param name the property name
   * @param value its value
   *
   * @return a newly created Criterion object
   */
  public static Criterion gt(String name, Object value) {
    return func("gt", name, value);
  }

  /**
   * Apply a "less than" criterion.
   *
   * @param name the property name
   * @param value its value
   *
   * @return a newly created Criterion object
   */
  public static Criterion lt(String name, Object value) {
    return func("lt", name, value);
  }

  /**
   * Apply a "less than or equals" criterion.
   *
   * @param name the property name
   * @param value its value
   *
   * @return a newly created Criterion object
   */
  public static Criterion le(String name, Object value) {
    return minus(new PredicateCriterion(name), gt(name, value));
  }

  /**
   * Apply a "greater than or equals" criterion.
   *
   * @param name the property name
   * @param value its value
   *
   * @return a newly created Criterion object
   */
  public static Criterion ge(String name, Object value) {
    return minus(new PredicateCriterion(name), lt(name, value));
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
   * Creates a Criterion that walks a property chain and finds the objects matching the
   * property value.
   *
   * @param func the property name
   *
   * @return a newly created Criterion object
   */
  public static Criterion func(String func, Object... args) {
    return new ProxyCriterion(func, args);
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
