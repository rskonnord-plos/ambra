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
 * A proxy criterion for executing store specific functions
 *
 * @author Pradeep Krishnan
 */
public class ProxyCriterion implements Criterion {
  private String    func;
  private Object[]  args;
  private Criterion criterion = null;

  /**
   * Creates a new ProxyCriterion object.
   *
   * @param func the function
   * @param args arguments
   */
  public ProxyCriterion(String func, Object... args) {
    this.func   = func;
    this.args   = args;
  }

  /**
   * Gets the function name.
   *
   * @return function name
   */
  public String getFunction() {
    return func;
  }

  /**
   * Gets the function arguments.
   *
   * @return arguments
   */
  public Object[] getArguments() {
    return args;
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                throws OtmException {
    if (criterion == null) {
      CriterionBuilder cb =
        criteria.getSession().getSessionFactory().getTripleStore().getCriterionBuilder(func);

      if (cb != null)
        criterion = cb.create(func, args);
    }

    if (criterion == null)
      throw new OtmException("Function '" + func + "' is unsupported by the triple-store");

    return criterion.toItql(criteria, subjectVar, varPrefix);
  }
}
