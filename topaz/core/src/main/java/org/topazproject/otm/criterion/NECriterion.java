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
 * A criterion for a "not equals" operation on a field value.
 *
 * @author Pradeep Krishnan
 *
 * @see NotCriterion
 * @see EQCriterion
 */
@Entity(type = Criterion.RDF_TYPE + "/ne")
public class NECriterion extends AbstractBinaryCriterion {
  /**
   * Creates a new NECriterion object.
   */
  public NECriterion() {
  }

  /**
   * Creates a new NECriterion object.
   *
   * @param name field/predicate name
   */
  public NECriterion(String name, Object value) {
    super(name, value);
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                throws OtmException {
    Criterion impl = new EQCriterion(getFieldName(), getValue());
    impl = new NotCriterion(impl);

    return impl.toItql(criteria, subjectVar, varPrefix);
  }

  /*
   * inherited javadoc
   */
  public String toOql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    String res = subjectVar;

    if (getFieldName() == null)
      res += ".{" + varPrefix + "p ->}";
    else
      res += "." + getFieldName();

    res += " != " + serializeValue(getValue(), criteria, getFieldName());

    return res;
  }
}
