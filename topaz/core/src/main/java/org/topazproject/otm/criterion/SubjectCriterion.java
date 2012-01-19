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

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.Session;

/**
 * A criterion for a triple pattern where the subject value is known.
 *
 * @author Pradeep Krishnan
 */
public class SubjectCriterion extends Criterion {
  private String id;

  /**
   * Creates a new SubjectCriterion object.
   *
   * @param id the id/subject-uri
   */
  public SubjectCriterion(String id) {
    this.id = id;
  }

  /**
   * Gets the id/subject-uri.
   *
   * @return the id/subject-uri
   */
  public String getId() {
    return id;
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix) {
    return subjectVar + " <mulgara:is> <" + id + ">";
  }

  /*
   * inherited javadoc
   */
  public String toOql(Criteria criteria, String subjectVar, String varPrefix) {
    ClassMetadata cm = criteria.getClassMetadata();
    return subjectVar + "." + cm.getIdField().getName() + " = <" + id + ">";
  }

  public String toString() {
    return "Subject[" + id + "]";
  }

  /*
   * inherited javadoc
   */
  public void onPreInsert(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    throw new UnsupportedOperationException("Not meant to be persisted");
  }

  /*
   * inherited javadoc
   */
  public void onPostLoad(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    throw new UnsupportedOperationException("Not meant to be persisted");
  }
}
