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
 * Conjunction of Criterions.
 *
 * @author Pradeep Krishnan
 */
public class Conjunction extends Junction {
  /**
   * Creates a new Conjunction object.
   */
  public Conjunction() {
    super("and");
  }
}
