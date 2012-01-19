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

/**
 * Disjunction of Criterions.
 *
 * @author Pradeep Krishnan
 */
public class Disjunction extends Junction {
  /**
   * Creates a new Disjunction object.
   */
  public Disjunction() {
    super("or");
  }
}
