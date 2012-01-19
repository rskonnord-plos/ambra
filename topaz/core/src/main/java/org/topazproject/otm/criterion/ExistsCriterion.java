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
import org.topazproject.otm.annotations.Entity;

/**
 * A criterion for an "exists" operation on a field. A field exists if statements with
 * the fields's predicate URI exists in the triplestore.
 *
 * @author Pradeep Krishnan
 *
 * @see PredicateCriterion
 */
@Entity(types = {Criterion.RDF_TYPE + "/exists"})
public class ExistsCriterion extends AbstractUnaryCriterion {

  /**
   * Creates a new ExistsCriterion object.
   */
  public ExistsCriterion() {
  }

  /**
   * Creates a new ExistsCriterion object.
   *
   * @param name field/predicate name
   */
  public ExistsCriterion(String name) {
    super(name);
  }

  /*
   * inherited javadoc
   */
  public String toQuery(Criteria criteria, String subjectVar, String varPrefix, QL ql)
                throws OtmException {
    Criterion impl = new PredicateCriterion(getFieldName());

    return impl.toQuery(criteria, subjectVar, varPrefix, ql);
  }

}
