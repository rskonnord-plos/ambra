/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.service;

/**
 * Used to indicate that the verification token is invalid.
 */
public class VerificationTokenInvalidException extends Exception {
  /**
   * Constructor with loginName and verificationToken
   * @param loginName loginName
   * @param verificationToken verificationToken
   */
  public VerificationTokenInvalidException(final String loginName, final String verificationToken) {
    super("loginName:" + loginName + ", verificationToken:" + verificationToken);
  }
}
