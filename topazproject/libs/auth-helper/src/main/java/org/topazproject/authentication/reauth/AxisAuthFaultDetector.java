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

import org.apache.axis.AxisFault;

/**
 * Detector for authentication faults on axis SOAP http calls.
 *
 * @author Pradeep Krishnan
 */
public class AxisAuthFaultDetector implements AuthFaultDetector {
  /*
   * @see org.topazproject.authentication.reauth.AuthFaultDetector#isAuthFault
   */
  public boolean isAuthFault(Throwable fault) {
    if (!(fault instanceof AxisFault))
      return false;

    String msg = fault.getMessage();

    // xxx: is there a better way?
    return ((msg.indexOf("(401)") >= 0)
            || (msg.indexOf("(444)Invalid CAS Ticket") >= 0)
            || (msg.indexOf("NoProxyTicketException") >= 0));
  }
}
