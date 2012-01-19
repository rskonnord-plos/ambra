/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.metadata;

/**
 * A reference to another definition.
 *
 * @author Pradeep Krishnan
 *
 */
public interface Reference {
  /**
   * Gets the referred definition.
   *
   * @return the referred definition
   */
  public String getReferred();
}
