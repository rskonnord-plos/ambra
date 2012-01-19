/*
 * Copyright 2004-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.topazproject.otm.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;

/**
 * A factory for creating {@link TransactionManagerLookup} instances. It also provides a {@link
 * #setTransactionManager helper} to set the transaction-manager for a {@link SessionFactory}.
 *
 * @author kimchy
 * @author Ronald Tschalär
 */
public final class TransactionManagerLookupFactory {
  private static final Log log = LogFactory.getLog(TransactionManagerLookupFactory.class);

  private static final Class[] autoDetectOrder = {
    WebSphere.class, Weblogic.class, JOnAS.class, JOTM.class, JBoss.class,
    Glassfish.class, Orion.class, Resin.class, OC4J.class, JRun4.class
  };

  /**
   * The system-property with which to configure the class to use to look up the current
   * transaction manager: {@value}
   */
  public static final String TX_MANAGER_LOOKUP_CLS =
                                          "org.topazproject.otm.transaction.tm_lookup_class";

  private TransactionManagerLookupFactory() {
  }

  /** 
   * Get the current jta transaction-manager lookup class. If the system property {@link
   * #TX_MANAGER_LOOKUP_CLS} is specified, then use that class; else attempt to auto-detect the
   * current jta transaction-manager being used.
   * 
   * @return the transaction-manager-lookup instance, or null if not found
   * @throws OtmException if a lookup class was explicitly specified but could not be instantiated
   */
  public static TransactionManagerLookup getTransactionManagerLookup() throws OtmException {
    return getTransactionManagerLookup(System.getProperty(TX_MANAGER_LOOKUP_CLS));
  }

  /** 
   * Get the current jta transaction-manager lookup class.
   * 
   * @param tmLookupClass the transaction-manager-lookup class to instantiate, or null to use
   *                      auto-detection
   * @return the transaction-manager-lookup instance, or null if not found
   * @throws OtmException if a lookup class was explicitly specified but could not be instantiated
   */
  public static TransactionManagerLookup getTransactionManagerLookup(String tmLookupClass)
      throws OtmException {
    if (tmLookupClass == null) {
      // try and auto detect the transaction manager
      log.info("JTA Transaction Manager Lookup setting not found, auto detecting....");
      for (Class tmClass : autoDetectOrder) {
        if (log.isDebugEnabled()) {
          log.debug("Trying [" + tmClass.getName() + "]");
        }
        TransactionManagerLookup tmLookup = detect(tmClass);
        if (tmLookup != null) {
          log.info("Detected JTA Transaction Manager [" + tmClass.getName() + "]");
          return tmLookup;
        }
      }
      log.info("No JTA Transaction Manager Lookup could be auto-detected");
      return null;
    } else {
      log.info("Instantiating TransactionManagerLookup [" + tmLookupClass + "]");
      try {
        return (TransactionManagerLookup) forName(tmLookupClass).newInstance();
      } catch (Exception e) {
        throw new OtmException("Could not instantiate TransactionManagerLookup " + tmLookupClass,
                               e);
      }
    }
  }

  private static TransactionManagerLookup detect(Class tmClass) {
    try {
      TransactionManagerLookup tmLookup = (TransactionManagerLookup) tmClass.newInstance();
      if (tmLookup.getTransactionManager() != null) {
        return tmLookup;
      }
      if (log.isDebugEnabled())
        log.debug("detection of " + tmClass.getName() + " failed");
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("detection of " + tmClass.getName() + " failed", e);
    }
    return null;
  }

  /** 
   * Attempt to find the given class, first looking in the thread-context-loader, then in the
   * current class' loader. 
   * 
   * @param cName the fully qualified name of the class to load
   * @return the class
   * @throws Exception if the class could not be found or an error occurred loading the class 
   */
  static Class forName(String cName) throws Exception {
    try {
      return Class.forName(cName, true, Thread.currentThread().getContextClassLoader());
    } catch (Exception e) {
      return Class.forName(cName);
    }
  }

  /** 
   * Set the given session-factory's transaction-manager to the current transaction-manager. This
   * uses {@link #getTransactionManagerLookup} to attempt to auto-detect the current tm being used. 
   * 
   * @param sf the session-factory to configure
   * @throws OtmException if no transaction-manager could be found
   */
  public static void setTransactionManager(SessionFactory sf) throws OtmException {
    TransactionManagerLookup tmLookup = getTransactionManagerLookup();
    if (tmLookup == null)
      throw new OtmException("No TransactionManagerLookup found");
    sf.setTransactionManager(tmLookup.getTransactionManager());
  }
}
