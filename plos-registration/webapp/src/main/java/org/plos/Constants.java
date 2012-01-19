/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos;

/**
 * Some of the constants for the Plosone application.
 */
public interface Constants {

  /**
   * Defines the length of various fields used by Webwork Annotations
   */
  interface Length {
    String EMAIL = "256";
    String PASSWORD_MAX = "255";
    String PASSWORD_MIN = "6";
    String DISPLAY_NAME_MIN = "4";
    String DISPLAY_NAME_MAX = "18";
  }
}
