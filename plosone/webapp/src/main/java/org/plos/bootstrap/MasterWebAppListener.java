/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.bootstrap;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;
import org.plos.configuration.WebAppListener;

/**
 * A listener class for web-apps to initialize things at startup.
 *
 * This class will call other ServletContextListeners configured.
 *
 * @author Eric Brown
 */
public class MasterWebAppListener implements ServletContextListener {
  private static Log log = LogFactory.getLog(MasterWebAppListener.class);

  private WebAppListener confListener = new WebAppListener();
  private List listeners;
  /**
   * Shutdown things.
   *
   * @param event the destryed event
   */
  public void contextDestroyed(ServletContextEvent event) {
    log.info("Shutting down topaz-web-application...");
    call("contextDestroyed", event);
    confListener.contextDestroyed(event);
    log.info("Topaz-web-application is now stopped.");
  }

  /**
   * Initialize things.
   *
   * @param event destroyed event
   */
  public void contextInitialized(ServletContextEvent event) {
    log.info("Initializing topaz-web-application...");
    confListener.contextInitialized(event);
    Configuration conf = ConfigurationStore.getInstance().getConfiguration();
    listeners = conf.getList("topaz.life-cycle-listeners.listener");
    call("contextInitialized", event);
    log.info("Topaz-web-application is now initialized.");
  }

  private void call(String methodName, ServletContextEvent event) {
    try {
      Iterator it = listeners.iterator();
      while (it.hasNext()) {
        String listenerName = (String) it.next();
        try {
          Class theClass = Class.forName(listenerName);
          ServletContextListener listener = (ServletContextListener) theClass.newInstance();
          log.debug("Calling " + methodName + " on " + listenerName);
          Method method = theClass.getMethod(methodName, new Class[] {ServletContextEvent.class});
          method.invoke(listener, new Object[] {event});
        } catch (Exception e) {
          log.warn("Error calling " + methodName + " on " + listenerName, e);
        }
      }
    } catch (Exception e) {
      log.warn("Error calling " + methodName + " on topaz life-cycle-listeners", e);
    }
  }
}
