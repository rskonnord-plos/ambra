/* $HeadURL::                                                                            $
 * $Id$
 *
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
