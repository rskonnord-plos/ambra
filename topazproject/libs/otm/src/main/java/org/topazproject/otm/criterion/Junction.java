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

import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;

/**
 * Base class for junctions on Criterions.
 *
 * @author Pradeep Krishnan
 */
public class Junction implements Criterion {
  private List<Criterion> criterions = new ArrayList<Criterion>();
  private String          op;

/**
   * Creates a new Junction object.
   *
   * @param op the operation
   */
  protected Junction(String op) {
    this.op                          = op;
  }

  /**
   * Adds a Criterion.
   *
   * @param c the criterion
   *
   * @return this for expression chaining
   */
  public Junction add(Criterion c) {
    criterions.add(c);

    return this;
  }

  /**
   * Gets the list of criterions.
   *
   * @return list or criterions
   */
  public List<Criterion> getCriterions() {
    return criterions;
  }

  /**
   * Gets the operation.
   *
   * @return the operation
   */
  public String getOp() {
    return op;
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    String sep   = "(";
    String query = "";
    int    i     = 0;

    for (Criterion c : getCriterions()) {
      query += sep + c.toItql(criteria, subjectVar, varPrefix + "j" + i++);
      sep = " " + getOp() + " ";
    }

    if (i > 0)
      query += ")";

    return query;
  }
}
