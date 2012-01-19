/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.auth.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.auth.AuthConstants;
import org.plos.auth.db.DatabaseException;
import org.plos.auth.db.DatabaseContext;
import org.plos.auth.service.UserService;

import org.apache.commons.configuration.Configuration;
import org.plos.configuration.ConfigurationStore;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;
import java.util.Properties;

/**
 * Initialize the DatabaseContext and UserService for cas.<p>
 *
 * Be sure to add to CAS' web.xml as a servlet context listner. Uses commons-config
 * for configuration.
 *
 * @author Viru
 * @author Eric Brown
 */
public class AuthServletContextListener implements ServletContextListener {
  private DatabaseContext dbContext;
  private static final Log log = LogFactory.getLog(AuthServletContextListener.class);

  public void contextInitialized(final ServletContextEvent event) {
    final ServletContext context = event.getServletContext();

    Configuration conf = ConfigurationStore.getInstance().getConfiguration();

    final Properties dbProperties = new Properties();
    dbProperties.setProperty("url", conf.getString("cas.db.url"));
    dbProperties.setProperty("user", conf.getString("cas.db.user"));
    dbProperties.setProperty("password", conf.getString("cas.db.password"));

    try {
      dbContext = DatabaseContext.createDatabaseContext(
              conf.getString("cas.db.driver"),
              dbProperties,
              conf.getInt("cas.db.initialSize"),
              conf.getInt("cas.db.maxActive"),
              conf.getString("cas.db.connectionValidationQuery"));
    } catch (final DatabaseException ex) {
      log.error("Failed to initialize the database context", ex);
    }

    final UserService userService = new UserService(
                                          dbContext,
                                          conf.getString("cas.db.usernameToGuidSql"),
                                          conf.getString("cas.db.guidToUsernameSql"));

    context.setAttribute(AuthConstants.USER_SERVICE, userService);
  }

  public void contextDestroyed(final ServletContextEvent event) {
    try {
      dbContext.close();
    } catch (final DatabaseException ex) {
      log.error("Failed to shutdown the database context", ex);
    }
  }
}
