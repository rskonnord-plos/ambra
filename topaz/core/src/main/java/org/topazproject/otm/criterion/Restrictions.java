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
   * Creates a criterion where an object property has a known value.
   *
   * @param name the property name
   * @param value its value
   *
   * @return a newly created Criterion object
   *
   * @see EQCriterion
   */
  public static Criterion eq(String name, Object value) {
    return new EQCriterion(name, value);
  }

  /**
   * Apply a "not equals" criterion.
   *
   * @param name the property name
   * @param value its value
   *
   * @return a newly created Criterion object
   *
   * @see NECriterion
   */
  public static Criterion ne(String name, Object value) {
    return new NECriterion(name, value);
  }

  /**
   * Apply an "exists" criterion.
   *
   * @param name the property name
   *
   * @return a newly created Criterion object
   *
   * @see ExistsCriterion
   */
  public static Criterion exists(String name) {
    return new ExistsCriterion(name);
  }

  /**
   * Apply a "not" criterion.
   *
   * @param name the property name
   *
   * @return a newly created Criterion object
   *
   * @see NotExistsCriterion
   */
  public static Criterion notExists(String name) {
    return new NotExistsCriterion(name);
  }

  /**
   * Apply a "greater than" criterion.
   *
   * @param name the property name
   * @param value its value
   *
   * @return a newly created Criterion object
   *
   * @see GTCriterion
   */
  public static Criterion gt(String name, Object value) {
    return new GTCriterion(name, value);
  }

  /**
   * Apply a "less than" criterion.
   *
   * @param name the property name
   * @param value its value
   *
   * @return a newly created Criterion object
   *
   * @see LTCriterion
   */
  public static Criterion lt(String name, Object value) {
    return new LTCriterion(name, value);
  }

  /**
   * Apply a "less than or equals" criterion.
   *
   * @param name the property name
   * @param value its value
   *
   * @return a newly created Criterion object
   *
   * @see LECriterion
   */
  public static Criterion le(String name, Object value) {
    return new LECriterion(name, value);
  }

  /**
   * Apply a "greater than or equals" criterion.
   *
   * @param name the property name
   * @param value its value
   *
   * @return a newly created Criterion object
   *
   * @see GECriterion
   */
  public static Criterion ge(String name, Object value) {
    return new GECriterion(name, value);
  }

  /**
   * Apply a "minus" criterion. Performs A - B.
   *
   * @param minuend subtract from
   * @param subtrahend subtract this
   *
   * @return a newly created Criterion object
   *
   * @see MinusCriterion
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
   *
   * @see NotCriterion
   */
  public static Criterion not(Criterion criterion) {
    return new NotCriterion(criterion);
  }

  /**
   * Creates a criterion that finds the objects that match the property value transitively.
   *
   * @param name the property name
   * @param value its value
   *
   * @return a newly created Criterion object
   *
   * @see TransCriterion
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
   *
   * @see WalkCriterion
   */
  public static Criterion walk(String name, Object value) {
    return new WalkCriterion(name, value);
  }

  /**
   * Creates a Criterion that executes a triple-store specific function.
   *
   * @param func the function to execute.
   * @param args the arguments to the function
   *
   * @return a newly created Criterion object
   *
   * @see ProxyCriterion
   * @see CriterionBuilder
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
   * @return Disjunction
   */
  public static Disjunction disjunction() {
    return new Disjunction();
  }
}
