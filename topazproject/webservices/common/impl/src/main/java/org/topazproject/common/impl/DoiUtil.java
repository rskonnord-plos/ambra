/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.common.impl;

/**
 * Utility functions for converting between PID's and fedora-URI's.
 *
 * @author Eric Brown and Ronald Tschalär (from ArticleImpl.java)
 */
public class DoiUtil {
  /**
   * Convert a PID to a fedora-URI.
   *
   * @param pid the PID to convert
   * @return the URI
   */
  public static String pid2URI(String pid) {
    return "info:fedora/" + pid;
  }

  /**
   * Convert a fedora-URI to a PID.
   *
   * @param uri the URI to convert
   * @return the PID
   */
  public static String uri2PID(String uri) {
    if (!uri.startsWith("info:fedora/"))
      throw new IllegalArgumentException("Can only convert fedora-URI's; uri='" + uri + "'");
    return uri.substring(12);
  }
}
