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

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.annotations.Entity;

/**
 * A criterion that performs a set minus.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Criterion.RDF_TYPE + "/minus")
public class MinusCriterion extends Criterion {
  private Criterion minuend;
  private Criterion subtrahend;

  /**
   * Creates a new MinusCriterion object.
   */
  public MinusCriterion() {
  }

  /**
   * Creates a new MinusCriterion object.
   *
   * @param minuend subtract from
   * @param subtrahend subtract this
   */
  public MinusCriterion(Criterion minuend, Criterion subtrahend) {
    this.minuend      = minuend;
    this.subtrahend   = subtrahend;
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                throws OtmException {
    return "( (" + minuend.toItql(criteria, subjectVar, varPrefix + "m1") + ") minus ("
           + subtrahend.toItql(criteria, subjectVar, varPrefix + "m2") + ") )";
  }

  /*
   * inherited javadoc
   */
  public String toOql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    throw new OtmException("'minus' is not supported by OQL");
  }

  /**
   * Get minuend.
   *
   * @return minuend as Criterion.
   */
  public Criterion getMinuend() {
    return minuend;
  }

  /**
   * Set minuend.
   *
   * @param minuend the value to set.
   */
  public void setMinuend(Criterion minuend) {
    this.minuend = minuend;
  }

  /**
   * Get subtrahend.
   *
   * @return subtrahend as Criterion.
   */
  public Criterion getSubtrahend() {
    return subtrahend;
  }

  /**
   * Set subtrahend.
   *
   * @param subtrahend the value to set.
   */
  public void setSubtrahend(Criterion subtrahend) {
    this.subtrahend = subtrahend;
  }

  /*
   * inherited javadoc
   */
  public Set<String> getParamNames() {
    Set<String> s1 = minuend.getParamNames();
    Set<String> s2 = subtrahend.getParamNames();

    if (s1.size() + s2.size() == 0)
      return Collections.emptySet();

    Set<String> s = new HashSet<String>();
    s.addAll(s1);
    s.addAll(s2);
    return s;
  }

  public String toString() {
    return "Minus[" + minuend + ", " + subtrahend + "]";
  }

  /*
   * inherited javadoc
   */
  public void onPreInsert(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    minuend.onPreInsert(ses, dc, cm);
    subtrahend.onPreInsert(ses, dc, cm);
  }

  /*
   * inherited javadoc
   */
  public void onPostLoad(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    minuend.onPostLoad(ses, dc, cm);
    subtrahend.onPostLoad(ses, dc, cm);
  }
}
