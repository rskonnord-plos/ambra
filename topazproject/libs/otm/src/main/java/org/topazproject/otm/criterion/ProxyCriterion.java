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

/**
 * A proxy criterion for executing store specific functions
 *
 * @author Pradeep Krishnan
 */
public class ProxyCriterion extends Criterion {
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
  public String toQuery(Criteria criteria, String subjectVar, String varPrefix, QL ql)
                throws OtmException {
    if (criterion == null) {
      CriterionBuilder cb =
        criteria.getSession().getSessionFactory().getTripleStore().getCriterionBuilder(func);

      if (cb != null)
        criterion = cb.create(func, args);
    }

    if (criterion == null)
      throw new OtmException("Function '" + func + "' is unsupported by the triple-store");

    return criterion.toQuery(criteria, subjectVar, varPrefix, ql);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(15 + args.length * 10);
    sb.append("Proxy[").append(func).append("(");

    for (Object o : args)
      sb.append(o).append(", ");
    if (args.length > 0)
      sb.setLength(sb.length() - 2);

    sb.append(")]");
    return sb.toString();
  }

  /*
   * inherited javadoc
   */
  public void onPreInsert(DetachedCriteria dc, ClassMetadata cm) {
    throw new UnsupportedOperationException("Not meant to be persisted");
  }

  /*
   * inherited javadoc
   */
  public void onPostLoad(DetachedCriteria dc, ClassMetadata cm) {
    throw new UnsupportedOperationException("Not meant to be persisted");
  }
}
