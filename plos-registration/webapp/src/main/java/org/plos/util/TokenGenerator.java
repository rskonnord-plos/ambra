/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.util;

import org.apache.struts2.util.TokenHelper;

/**
 * Token generator to be used for generating unique tokens.
 */
public class TokenGenerator {

  /**
   * Creates a unique guid.
   * @return a unique guid token
   */
  public static String getUniqueToken() {
    return TokenHelper.generateGUID();
  }
}
