/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos;

/**
 *
 */
 public class ApplicationException extends RuntimeException {
  public ApplicationException() {
  }

  public ApplicationException(final String message) {
    super(message);
  }

  /**
   * Constructor with message and exception.
   * @param message message
   * @param ex exception
   */
  public ApplicationException(final String message, final Exception ex) {
    super(message, ex);
  }
}
