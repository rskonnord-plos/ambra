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
 * An interface for a builder/factory of Criterions.
 *
 * @author Pradeep Krishnan
 * @see org.topazproject.otm.TripleStore#setCriterionBuilder 
  */
public interface CriterionBuilder {
  /**
   * Creates a Criterion based on a function name
   *
   * @param func the store specific function
   *
   * @return the newly created Criterion
   *
   * @throws OtmException if an error occurred
   */
  public Criterion create(String func, Object... args)
                   throws OtmException;
}
