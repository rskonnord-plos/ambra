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

/**
 * An interface for all query criterion used as restrictions in a 
 * {@link org.topazproject.otm.Criteria}.
 *
 * @author Pradeep Krishnan
  */
public interface Criterion {
  /**
   * Creates an ITQL query 'where clause' fragment.
   *
   * @param criteria the Criteria
   * @param subjectVar the subject designator variable (eg. $s etc.)
   * @param varPrefix namespace for internal variables (ie. not visible on select list)
   *
   * @return the itql query fragment
   * @throws OtmException if an error occurred
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException;
}
