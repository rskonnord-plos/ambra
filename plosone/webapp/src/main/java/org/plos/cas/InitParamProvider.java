/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.cas;

import java.util.Enumeration;

/**
 * An interface to make it easier to reuse code when working with
 * config wrappers for ServletConfig and FilterConfig
 */
public interface InitParamProvider {
  /**
   * Get init parameter names
   * @return Enumeration
   */
  Enumeration getInitParameterNames();

  /**
   * Get the init parameter for a given key
   * @param key parameter name
   * @return value
   */
  String getInitParameter(final String key);
}
