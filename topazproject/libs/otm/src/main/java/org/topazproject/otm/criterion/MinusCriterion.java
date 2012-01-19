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

import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.mapping.Mapper;

/**
 * A criterion that performs a set minus.
 *
 * @author Pradeep Krishnan
 */
public class MinusCriterion implements Criterion {
  private Criterion minuend;
  private Criterion subtrahend;

/**
   * Creates a new MinusCriterion object.
   *
   * @param minuend subtract from
   * @param subtrahend subtract this
   */
  public MinusCriterion(Criterion minuend, Criterion subtrahend) {
    this.minuend      = minuend;
    this.subtrahend   = subtrahend;
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                throws OtmException {
    return "(" + minuend.toItql(criteria, subjectVar, varPrefix + "m1") + " minus "
           + subtrahend.toItql(criteria, subjectVar, varPrefix + "m2") + ")";
  }
}
