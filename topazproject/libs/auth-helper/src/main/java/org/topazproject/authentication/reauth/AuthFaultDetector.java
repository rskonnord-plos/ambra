/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.authentication.reauth;

/**
 * An interface to abstract out the detection of authentication failures.
 *
 * @author Pradeep Krishnan
 */
public interface AuthFaultDetector {
  /**
   * Checks if the passed in fault is caused by an authentication failure.
   *
   * @param fault the fault to checj
   *
   * @return Returns true if the exception is due to a failed authentication.
   */
  public boolean isAuthFault(Throwable fault);
}
