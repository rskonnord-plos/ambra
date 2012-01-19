/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.util;

import com.opensymphony.util.GUID;

/**
 * Token generator to be used for generating unique tokens.
 */
public class TokenGenerator {

  /**
   * Creates a unique guid.
   * @return a unique guid token
   */
  public static String getUniqueToken() {
    return GUID.generateGUID();
  }
}
