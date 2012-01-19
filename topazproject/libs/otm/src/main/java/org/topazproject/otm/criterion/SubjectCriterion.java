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

/**
 * A criterion for a triple pattern where the subject value is known.
 *
 * @author Pradeep Krishnan
 */
public class SubjectCriterion implements Criterion {
  private String id;

  /**
   * Creates a new SubjectCriterion object.
   *
   * @param id the id/subject-uri
   */
  public SubjectCriterion(String id) {
    this.id = id;
  }

  /**
   * Gets the id/subject-uri.
   *
   * @return the id/subject-uri
   */
  public String getId() {
    return id;
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix) {
    return subjectVar + " <mulgara:is> <" + id + ">";
  }
}
