/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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
package org.topazproject.otm.criterion;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;

/**
 * A criterion that performs a set minus.
 *
 * @author Pradeep Krishnan
 */
@Entity(types = {Criterion.RDF_TYPE + "/minus"})
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
  public String toItql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    return "( (" + getMinuend().toItql(criteria, subjectVar, varPrefix + "m1") + ") minus ("
           + getSubtrahend().toItql(criteria, subjectVar, varPrefix + "m2") + ") )";
  }

  /*
   * inherited javadoc
   */
  public String toOql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    return "( (" + getMinuend().toOql(criteria, subjectVar, varPrefix + "m1") + ") minus ("
           + getSubtrahend().toOql(criteria, subjectVar, varPrefix + "m2") + ") )";
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
  @Predicate
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
  @Predicate
  public void setSubtrahend(Criterion subtrahend) {
    this.subtrahend = subtrahend;
  }

  /*
   * inherited javadoc
   */
  public Set<String> getParamNames() {
    Set<String> s1 = getMinuend().getParamNames();
    Set<String> s2 = getSubtrahend().getParamNames();

    if ((s1.size() + s2.size()) == 0)
      return Collections.emptySet();

    Set<String> s = new HashSet<String>();
    s.addAll(s1);
    s.addAll(s2);

    return s;
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return "Minus[" + getMinuend() + ", " + getSubtrahend() + "]";
  }

  /*
   * inherited javadoc
   */
  public void onPreInsert(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    getMinuend().onPreInsert(ses, dc, cm);
    getSubtrahend().onPreInsert(ses, dc, cm);
  }

  /*
   * inherited javadoc
   */
  public void onPostLoad(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    getMinuend().onPostLoad(ses, dc, cm);
    getSubtrahend().onPostLoad(ses, dc, cm);
  }
}
