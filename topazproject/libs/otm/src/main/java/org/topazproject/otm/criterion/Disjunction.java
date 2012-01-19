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

import org.topazproject.otm.annotations.Entity;

/**
 * Disjunction of Criterions.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Criterion.RDF_TYPE + "/or")
public class Disjunction extends Junction {
  /**
   * Creates a new Disjunction object.
   */
  public Disjunction() {
    super("or");
  }
}
