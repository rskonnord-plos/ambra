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
 * A criterion for a "greater than" operation on a field value.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Criterion.RDF_TYPE + "/gt")
public class GTCriterion extends AbstractComparisonCriterion {
  /**
   * Creates a new GTCriterion object.
   */
  public GTCriterion() {
  }

  /**
   * Creates a new GTCriterion object.
   *
   * @param name field/predicate name
   * @param value field/predicate value
   */
  public GTCriterion(String name, Object value) {
    super(name, value);
  }

  /*
   * inherited javadoc
   */
  public String toQuery(Criteria criteria, String subjectVar, String varPrefix, QL ql)
                throws OtmException {
    return toQuery(criteria, subjectVar, varPrefix, "gt", ql);
  }
}
