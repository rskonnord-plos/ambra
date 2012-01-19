/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.alerts.impl;

import java.io.StringWriter;

/**
 * Signals a problem generating the alert email message.
 *
 * @author Eric Brown
 */
public class AlertsGenerationException extends Exception {
  /** 
   * Create a new exception instance. 
   * 
   * @param message a message describing the error
   */
  public AlertsGenerationException(String message) {
    super(message);
  }

  /** 
   * Create a new exception instance. 
   * 
   * @param message a message describing the error
   * @param cause   the exception that caused the error
   */
  public AlertsGenerationException(String message, Throwable cause) {
    super(message, cause);
  }

  /** 
   * This is just here so axis will generate a service version with a contructor that takes the
   * message.
   *
   * @return the message
   */
  public String getMessage() {
    return super.getMessage();
  }
}