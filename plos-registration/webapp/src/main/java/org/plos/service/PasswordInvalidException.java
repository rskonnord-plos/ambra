/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.service;

/**
 * Password does not match the loginName exception.
 */
public class PasswordInvalidException extends Exception {
  /**
   * Constructor with loginName and password.
   * @param loginName loginName
   * @param password password
   */
  public PasswordInvalidException(final String loginName, final String password) {
    super("loginName:"+ loginName + ",password:"+ password);
  }
}
