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
 * A criterion for an "exists" operation on a field.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Criterion.RDF_TYPE + "/exists")
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
