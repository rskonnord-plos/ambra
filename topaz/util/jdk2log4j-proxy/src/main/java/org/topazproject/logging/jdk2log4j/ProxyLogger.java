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

import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.LocationInfo;

/**
 * A ProxyLogger logs to log4j. It also uses log4j configuration information to
 * decide what level to log at. See {@link LogManager} for instructions on how to
 * activate ProxyLogger. Normally the caller doesn't even know that they're using
 * ProxyLogger and believes itself to be using the standard jdk {@link Logger}.
 * <p>
 * Jdk logging levels ({@link java.util.logging.Level}) are converted to log4j logging
 * levels ({@link org.apache.log4j.Level}) as follows:
 * <dl><dt><dd><table>
 * <tr><th>jdk    </th><th>log4j</th></tr>
 * <tr><td>OFF    </td><td>OFF  </td></tr>
 * <tr><td>ALL    </td><td>ALL  </td></tr>
 * <tr><td>FINEST </td><td>TRACE</td></tr>
 * <tr><td>FINER  </td><td>TRACE</td></tr>
 * <tr><td>FINE   </td><td>DEBUG</td></tr>
 * <tr><td>CONFIG </td><td>DEBUG</td></tr>
 * <tr><td>INFO   </td><td>INFO </td></tr>
 * <tr><td>WARNING</td><td>WARN </td></tr>
 * <tr><td>SEVERE </td><td>FATAL</td></tr>
 * </table></dl>
 * <p>
 * An attempt is made to make this as performance sensitive as possible, but some
 * minor performance is lost in translating log4j configuration (what levels should
 * be logged) and jdk log requests. (This can be improved by further redefining all
 * java.util.logging.Logger log methods.)
 *
 * @author Eric Brown
 */
public class ProxyLogger extends java.util.logging.Logger {
  /**
   * The log4j logger we're proxying to.
   */
  private org.apache.log4j.Logger log4jLogger;

  // Try to make level mapping as efficient as possible
  private final static HashMap jdk2log4jLevelMap = new HashMap(9);
  private final static HashMap log4j2jdkLevelMap = new HashMap(9);
  static {
    jdk2log4jLevelMap.put(Level.OFF,     org.apache.log4j.Level.OFF);
    jdk2log4jLevelMap.put(Level.ALL,     org.apache.log4j.Level.ALL);

    // jdk: 300 - 1000                   log4j: 5000 - 50,000
    jdk2log4jLevelMap.put(Level.FINEST,  org.apache.log4j.Level.TRACE); // 300, 5000
    jdk2log4jLevelMap.put(Level.FINER,   org.apache.log4j.Level.TRACE);
    jdk2log4jLevelMap.put(Level.FINE,    org.apache.log4j.Level.DEBUG);
    jdk2log4jLevelMap.put(Level.CONFIG,  org.apache.log4j.Level.DEBUG);
    jdk2log4jLevelMap.put(Level.INFO,    org.apache.log4j.Level.INFO);
    jdk2log4jLevelMap.put(Level.WARNING, org.apache.log4j.Level.WARN);
    jdk2log4jLevelMap.put(Level.SEVERE,  org.apache.log4j.Level.FATAL); // 1000, 50000

    // Create reverse map
    for (Iterator it = jdk2log4jLevelMap.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry entry = (Map.Entry)it.next();
      log4j2jdkLevelMap.put(entry.getValue(), entry.getKey());
    }
  }

  /**
   * Convert a jdk log level to a log4j log level. If the level is a custom one,
   * convert between the integer levels as best we can.
   */
  private static final org.apache.log4j.Level getLog4jLevel(Level jdkLevel) {
    org.apache.log4j.Level log4jLevel = (org.apache.log4j.Level)jdk2log4jLevelMap.get(jdkLevel);
    if (log4jLevel != null)
      return log4jLevel;
    else
      return org.apache.log4j.Level.toLevel(jdkLevel.intValue() / 5000);
  }

  /**
   * Convert a log4j log level to a jdk log level. If the level is a custom one,
   * convert between the integer levels as best we can.
   */
  private static final Level getJdkLevel(org.apache.log4j.Level log4jLevel) {
    Level jdkLevel = (Level)log4j2jdkLevelMap.get(log4jLevel);
    if (jdkLevel != null)
      return jdkLevel;
    else {
      try {
        // Level.parse converts a string or an integer convert to a string to a level object
        return Level.parse(new Integer(log4jLevel.toInt() * 5000).toString());
      } catch (IllegalArgumentException iae) {
        // Impossible to get here, so whatever (should log something, but might infinitely recurse)
        return java.util.logging.Level.SEVERE;
      }
    }
  }

  /**
   * Instantiate a ProxyLogger from a log4j Logger.
   *
   * @param log4jLogger the logger to proxy for. The same name will be used.
   */
  ProxyLogger(org.apache.log4j.Logger log4jLogger) {
    // Do we need to do name translation for the root logger? See log4j docs on root logger name.
    super(log4jLogger.getName(), null);
    this.log4jLogger = log4jLogger;
    this.setLevel(Level.ALL); // We decide this based on log4j level, NOT jdk level
    
    /* If we want to be most efficient and don't care about any log4j configuration changes
     * or initialization order dependencies, setting a fixed level here would offer the
     * greates performance:
     *   this.setLevel(getJdkLevel(log4jLogger.getEffectiveLevel()));
     */
  }

  /**
   * Instantiate a ProxyLogger.
   *
   * Used by {@link LogManager}.
   *
   * @param name the name of the logger to create.
   */
  ProxyLogger(String name) {
    this(org.apache.log4j.Logger.getLogger(name));
  }

  /**
   * Overrided method to log via log4j instead of the jdk.
   * <p>
   * {@inheritDoc}
   */
  public void log(final LogRecord record) {
    org.apache.log4j.Level log4jLevel = getLog4jLevel(record.getLevel());
    if (!this.log4jLogger.isEnabledFor(log4jLevel))
      return;

    LoggingEvent event = new LoggingEvent(this.log4jLogger.getClass().getName(),
                                          log4jLogger,
                                          record.getMillis(),
                                          log4jLevel,
                                          record.getMessage(),
                                          record.getThrown()) {
        public LocationInfo getLocationInformation() {
          /* Find location by finding entry in Throwable's stack-trace after calling class
           * This proxy is usually (always) seen as the jdk Logger
           * FYI: LocationInfo for class name could be more efficient from LogRecord (maybe)
           */
          return new LocationInfo(new Throwable(), "java.util.logging.Logger") {
              public String getClassName()  { return record.getSourceClassName();  }
              public String getMethodName() { return record.getSourceMethodName(); }
            };
        }
      };

    this.log4jLogger.callAppenders(event);

    /* Less code, but slightly less fidelity. Probably slower too?
     *   this.log4jLogger.log(record.getSourceClassName(),
     *                        getLog4jLevel(record.getLevel()),
     *                        record.getMessage(),
     *                        record.getThrown());
     */
  }

  /**
   * This method overrides {@link java.util.logging.Logger#isLoggable} to check if a message
   * of the given translated level would actually be logged by log4j.
   * <p>
   * {@inheritDoc}
   */
  public boolean isLoggable(Level level) {
    return this.log4jLogger.isEnabledFor(getLog4jLevel(level));
  }

  // TODO: If we really wanted to be efficient, this would be the way to do things (#64)
  public void warning(String msg) {
    /* Could compare level values directly and possibly save some function call overhead.
     *   if (org.apache.log4j.Level.WARN_INT < this.log4jLogger.getEffectiveLevel().toInt()) {...}
     */
    if (!this.log4jLogger.isEnabledFor(org.apache.log4j.Level.WARN))
      return;
    log(Level.WARNING, msg);
  }
}
