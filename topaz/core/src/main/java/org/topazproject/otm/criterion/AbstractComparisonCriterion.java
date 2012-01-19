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

import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;

/**
 * An abstract base criterion for comparison operation on a field value. Comparisons are not 
 * built in functions in ITQL. Therefore a custom mulgara resolver needs to be installed to 
 * make this work. To configure a store that has this resolver already installed a {@link
 * CriterionBuilder} like {@link org.topazproject.otm.criterion.itql.ComparisonCriterionBuilder} 
 * must be installed using the {@link org.topazproject.otm.TripleStore#setCriterionBuilder
 * setCriterionBuilder()} method.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractComparisonCriterion extends AbstractBinaryCriterion {
  /**
   * Creates a new EqualsCriterion object.
   */
  public AbstractComparisonCriterion() {
  }

  /**
   * Creates a new AbstractComparisonCriterion object.
   *
   * @param name field/predicate name
   * @param value field/predicate value
   */
  public AbstractComparisonCriterion(String name, Object value) {
    super(name, value);
  }

  protected String toQuery(Criteria criteria, String subjectVar, String varPrefix, String operator,
                           QL ql)
                throws OtmException {
    CriterionBuilder cb =
      criteria.getSession().getSessionFactory().getTripleStore().getCriterionBuilder(operator);

    if (cb != null)
      return cb.create(operator, getFieldName(), getValue()).
                toQuery(criteria, subjectVar, varPrefix, ql);

    throw new OtmException("Function " + operator + "' is unsupported by the triple-store");
  }
}
