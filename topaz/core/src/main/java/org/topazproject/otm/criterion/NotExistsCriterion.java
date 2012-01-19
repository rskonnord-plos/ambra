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
 * A criterion for a "not exists" operation on a field value.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Criterion.RDF_TYPE + "/notExists")
public class NotExistsCriterion extends AbstractUnaryCriterion {

  /**
   * Creates a new NotExistsCriterion object.
   */
  public NotExistsCriterion() {
  }

  /**
   * Creates a new NotExistsCriterion object.
   *
   * @param name field/predicate name
   */
  public NotExistsCriterion(String name) {
    super(name);
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                throws OtmException {
    Criterion impl = new PredicateCriterion(getFieldName());
    impl = new NotCriterion(impl);

    return impl.toItql(criteria, subjectVar, varPrefix);
  }

  /*
   * inherited javadoc
   */
  public String toOql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    if (criteria != null)       // always true - dummy for compiler
      throw new OtmException("'not-exists' is not supported in OQL (yet)");

    String res = subjectVar;

    if (getFieldName() == null)
      res += ".{" + varPrefix + "p ->}";
    else
      res += "." + getFieldName();

    res += " == null";

    return res;
  }

}
