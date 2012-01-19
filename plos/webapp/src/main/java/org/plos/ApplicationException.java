/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos;

/**
 * Generic application level exception
 */
public class ApplicationException extends Exception {
  public ApplicationException(final Exception e) {
    super(e);
  }

  public ApplicationException(final String message, final Exception e) {
    super(message, e);
  }

  public ApplicationException (final String message) {
    super (message);
  }
}
