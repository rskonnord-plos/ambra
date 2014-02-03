/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.service.migration;

import com.google.common.collect.ImmutableList;
import org.ambraproject.models.Version;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Does migrations on startup.
 *
 * @author Joe Osowski
 */
public class BootstrapMigratorServiceImpl extends HibernateServiceImpl implements BootstrapMigratorService {
  private static final Logger log = LoggerFactory.getLogger(BootstrapMigratorServiceImpl.class);

  /**
   * Apply all migrations.
   *
   * @throws Exception on an error
   */
  public void migrate() throws Exception {
    ImmutableList<Migration> migrations = Migrations.getAllMigrations();
    int binaryVersion = migrations.get(migrations.size() - 1).getVersion();
    int dbVersion = fetchDatabaseVersion();

    //Throws an exception if the database version is further into
    //the future then this version of the ambra war
    if (binaryVersion < dbVersion) {
      log.error("Binary version: " + binaryVersion + ", DB version: " + dbVersion);
      throw new Exception("The ambra war is out of date with the database, " +
          "update this war file to the latest version.");
    }

    waitForOtherMigrations();

    for (Migration migration : migrations) {
      if (dbVersion < migration.getVersion()) {
        migration.migrate(hibernateTemplate);
      }
    }
  }

  /*
   * Wait for other migrations to complete.  This will prevent two instances of ambra from attempting to execute the
   * same migration
   */
  private void waitForOtherMigrations() throws InterruptedException {
    while (isMigrateRunning()) {
      log.debug("Waiting for another migration to complete.");
      Thread.sleep(10000);
    }
  }

  /*
   * Determine if a migration is already running
   */
  private boolean isMigrateRunning() {
    return (Boolean) hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        SQLQuery q = session.createSQLQuery("show tables");
        List<String> tables = q.list();

        //Check to see if the version table exists.
        //If it does not exist then no migrations have been run yet
        if (!tables.contains("version")) {
          return false;
        }

        //If we get this far, return the version column out of the database
        Criteria c = session.createCriteria(Version.class)
            .setProjection(Projections.max("version"));

        Integer version = (Integer) c.uniqueResult();
        if (version == null) {
          return false; // no migrations have been run yet
        }

        c = session.createCriteria(Version.class)
            .add(Restrictions.eq("version", version));

        Version v = (Version) c.uniqueResult();

        return (v == null) ? false : v.getUpdateInProcess();
      }
    });
  }

  /*
   * Load a mysql script from a resource
   */
  static String getSQLScript(String filename) throws IOException {
    InputStream is = BootstrapMigratorServiceImpl.class.getResourceAsStream(filename);
    StringBuilder out = new StringBuilder();

    byte[] b = new byte[4096];
    for (int n; (n = is.read(b)) != -1; ) {
      out.append(new String(b, 0, n));
    }

    return out.toString();
  }

  private static List<String> getSQLCommands(String filename) throws IOException {
    String sqlString = getSQLScript(filename);
    List<String> sqlCommands = new ArrayList<String>();

    String sqlCommandsTemp[] = sqlString.split(";");

    for (String sqlCommand : sqlCommandsTemp) {
      if (sqlCommand.trim().length() > 0) {
        sqlCommands.add(sqlCommand);
      }
    }
    return sqlCommands;
  }

  /*
   * Get the current version of the database
   */
  @SuppressWarnings("unchecked")
  private int fetchDatabaseVersion() {
    return hibernateTemplate.execute(new HibernateCallback<Integer>() {
      @Override
      public Integer doInHibernate(Session session) throws HibernateException, SQLException {
        SQLQuery q = session.createSQLQuery("show tables");
        List<String> tables = q.list();

        //Check to see if the version table exists.
        //If it does not exist then it's ambra 2.00
        if (!tables.contains("version")) {
          return LegacyMigration.MIN_VERSION;
        }

        //If we get this far, return the version column out of the database
        Criteria c = session.createCriteria(Version.class)
            .setProjection(Projections.max("version"));

        Integer i = (Integer) c.uniqueResult();

        return (i == null) ? LegacyMigration.MIN_VERSION : i;
      }
    });
  }

  static void execSQLScript(Session session, String sqlScript) throws SQLException, HibernateException {
    log.debug("{} started.", sqlScript);
    List<String> sqlStatements;

    Transaction transaction = session.getTransaction();

    try {
      sqlStatements = getSQLCommands(sqlScript);
    } catch (IOException ex) {
      throw new HibernateException(ex.getMessage(), ex);
    }

    transaction.begin();

    for (String sqlStatement : sqlStatements) {
      log.debug("Running: {}", sqlStatement);
      session.createSQLQuery(sqlStatement).executeUpdate();
    }

    transaction.commit();
    log.debug("{} completed.", sqlScript);
  }
}
