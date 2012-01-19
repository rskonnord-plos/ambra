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
