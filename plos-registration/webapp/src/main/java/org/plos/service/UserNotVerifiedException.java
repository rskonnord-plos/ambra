package org.plos.service;
/* $HeadURL::                                                                            $
 * $Id$
 */

/**
 * User has not been verified exception. May happen if the user never verified his email address
 */
public class UserNotVerifiedException extends Exception {
  public UserNotVerifiedException(final String loginName) {
    super(loginName);
  }
}
