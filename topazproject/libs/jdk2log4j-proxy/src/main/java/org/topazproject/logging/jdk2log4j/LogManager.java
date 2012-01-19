/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.logging.jdk2log4j;

import java.util.logging.Logger;

/**
 * Custom jkd14 LogManager class that causes all use of {@link java.util.logging} to
 * use log4j for configuration and output.
 * <p>
 * This LogManager replaces the default LogManager if the <tt>java.util.logging.manager</tt>
 * system property is set to <tt>org.topazproject.logging.jdk2log4j.LogManager</tt>.
 * <p>
 * This LogManager overrides  default LogManager to use {@link ProxyLogger} for
 * logging. It is ProxyLogger that does the actual proxying of log records over to
 * log4j.
 *
 * @author Eric Brown
 */
public class LogManager extends java.util.logging.LogManager {
  /**
   * This method overrides {@link java.util.logging.LogManager#addLogger} to create our
   * ProxyLoggers instead of the jdk's Loggers. It is generally used internally by
   * LogManager mostly despite being declared public.
   * <p>
   * {@inheritDoc}
   */
  public synchronized boolean addLogger(Logger logger) {
    // If you ask me, the reason this works is convoluted. Read java.util.logging.LogManager source
    
    final String name = logger.getName();
    if (name == null) {
      throw new NullPointerException();
    }

    Logger old = this.getLogger(name);
    if (old != null) {
      // We already have a registered logger with the given name.
      return false;
    }

    return super.addLogger(new ProxyLogger(name));
  }
}
