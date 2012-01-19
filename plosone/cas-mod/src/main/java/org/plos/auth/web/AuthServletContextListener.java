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

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;
import java.util.Properties;

/**
 * Initializes the following:
 * - DatabaseContext
 * - UserService

 <context-param>
   <param-name>jdbcDriver</param-name>
   <param-value>org.postgresql.Driver</param-value>
 </context-param>
 <context-param>
   <param-name>jdbcUrl</param-name>
   <param-value>jdbc:postgresql://localhost/postgres</param-value>
 </context-param>
 <context-param>
   <param-name>usernameToGuidSql</param-name>
   <param-value>select id from plos_user where loginname=?</param-value>
 </context-param>
 <context-param>
   <param-name>guidToUsernameSql</param-name>
   <param-value>select loginname from plos_user where id=?</param-value>
 </context-param>
 <context-param>
   <param-name>connectionValidationQuery</param-name>
   <param-value>select 1</param-value>
 </context-param>
 <context-param>
   <param-name>initialSize</param-name>
   <param-value>2</param-value>
 </context-param>
 <context-param>
   <param-name>maxActive</param-name>
   <param-value>10</param-value>
 </context-param>
 <context-param>
   <param-name>adminUser</param-name>
   <param-value>postgres</param-value>
 </context-param>
 <context-param>
   <param-name>adminPassword</param-name>
   <param-value>postgres</param-value>
 </context-param>

 <listener>
   <listener-class>org.plos.auth.web.AuthServletContextListener</listener-class>
 </listener>
 */
public class AuthServletContextListener implements ServletContextListener {
  private DatabaseContext dbContext;
  private static final Log log = LogFactory.getLog(AuthServletContextListener.class);

  public void contextInitialized(final ServletContextEvent event) {
    final ServletContext context = event.getServletContext();

    final Properties dbProperties = new Properties();
    dbProperties.setProperty("url", context.getInitParameter("jdbcUrl"));
    dbProperties.setProperty("user", context.getInitParameter("dbUser"));
    dbProperties.setProperty("password", context.getInitParameter("dbPassword"));

    try {
      dbContext = DatabaseContext.createDatabaseContext(
              context.getInitParameter("jdbcDriver"),
              dbProperties,
              Integer.parseInt(context.getInitParameter("initialSize")),
              Integer.parseInt(context.getInitParameter("maxActive")),
              context.getInitParameter("connectionValidationQuery"));
    } catch (final DatabaseException ex) {
      log.error("Failed to initialize the database context", ex);
    }

    final UserService userService = new UserService(
                                          dbContext,
                                          context.getInitParameter("usernameToGuidSql"),
                                          context.getInitParameter("guidToUsernameSql"));

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
