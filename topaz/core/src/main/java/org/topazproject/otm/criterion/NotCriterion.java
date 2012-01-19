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

import java.util.Set;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.annotations.Entity;

/**
 * A criterion for doing a 'not' operation.
 *
 * @author Pradeep Krishnan
 *
 * @see MinusCriterion
 * @see PredicateCriterion
 */
@Entity(type = Criterion.RDF_TYPE + "/not")
public class NotCriterion extends Criterion {
  private Criterion criterion;

  /**
   * Creates a new NotCriterion object.
   */
  public NotCriterion() {
  }

  /**
   * Creates a new NotCriterion object.
   *
   * @param criterion the criterion to NOT
   */
  public NotCriterion(Criterion criterion) {
    setCriterion(criterion);
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                throws OtmException {
    Criterion impl = new MinusCriterion(new PredicateCriterion(), criterion);

    return impl.toItql(criteria, subjectVar, varPrefix);
  }

  /*
   * inherited javadoc
   */
  public String toOql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    throw new OtmException("'not' is not supported by OQL");
  }

  /**
   * Get criterion.
   *
   * @return criterion as Criterion.
   */
  public Criterion getCriterion() {
    return criterion;
  }

  /**
   * Set criterion.
   *
   * @param criterion the value to set.
   */
  public void setCriterion(Criterion criterion) {
    this.criterion = criterion;
  }

  /*
   * inherited javadoc
   */
  public Set<String> getParamNames() {
    return criterion.getParamNames();
  }

  public String toString() {
    return "Not[" + criterion + "]";
  }

  /*
   * inherited javadoc
   */
  public void onPreInsert(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    criterion.onPreInsert(ses, dc, cm);
  }

  /*
   * inherited javadoc
   */
  public void onPostLoad(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    criterion.onPostLoad(ses, dc, cm);
  }
}
