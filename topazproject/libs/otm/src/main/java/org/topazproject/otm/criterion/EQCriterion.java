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

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.annotations.Entity;

/**
 * A criterion for an "equals" operation on a field value.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Criterion.RDF_TYPE + "/eq")
public class EQCriterion extends AbstractBinaryCriterion {
  /**
   * Creates a new EQCriterion object.
   */
  public EQCriterion() {
  }

  /**
   * Creates a new EQCriterion object.
   *
   * @param name field/predicate name
   * @param value field/predicate value
   */
  public EQCriterion(String name, Object value) {
    super(name, value);
  }

  /*
   * inherited javadoc
   */
  public String toQuery(Criteria criteria, String subjectVar, String varPrefix, QL ql)
                throws OtmException {
    Criterion     impl;
    ClassMetadata cm = criteria.getClassMetadata();

    if (cm.getIdField().getName().equals(getFieldName()))
      impl = new SubjectCriterion(getValue().toString());
    else
      impl = new PredicateCriterion(getFieldName(), getValue());

    return impl.toQuery(criteria, subjectVar, varPrefix, ql);
  }
}
