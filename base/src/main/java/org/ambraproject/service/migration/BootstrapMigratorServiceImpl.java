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

import org.ambraproject.models.SavedSearchQuery;
import org.ambraproject.models.Version;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.util.TextUtils;
import org.apache.commons.configuration.Configuration;
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
import org.ambraproject.configuration.ConfigurationStore;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Does migrations on startup.
 * <p/>
 * Note we store version as integers to avoid floating point / decimal rounding issues with mysql and java. So just
 * multiply release values by 100.
 *
 * @author Joe Osowski
 */
public class BootstrapMigratorServiceImpl extends HibernateServiceImpl implements BootstrapMigratorService {
  private static Logger log = LoggerFactory.getLogger(BootstrapMigratorServiceImpl.class);

  private double dbVersion;
  private double binaryVersion;

  /**
   * Apply all migrations.
   *
   * @throws Exception on an error
   */
  public void migrate() throws Exception {
    Configuration conf = ConfigurationStore.getInstance().getConfiguration();

    setVersionData();

    //Throws an exception if the database version is further into
    //the future then this version of the ambra war
    if (binaryVersion < dbVersion) {
      log.error("Binary version: " + binaryVersion + ", DB version: " + dbVersion);
      throw new Exception("The ambra war is out of date with the database, " +
          "update this war file to the latest version.");
    }

    waitForOtherMigrations();

    if (dbVersion < 220) {
      migrate210();
    }

    if (dbVersion < 223) {
      migrate222();
    }

    if (dbVersion < 232) {
      migrate230();
    }

    if (dbVersion < 234) {
      migrate232();
    }

    if (dbVersion < 237) {
      migrate234();
    }

    if (dbVersion < 240) {
      migrate237();
    }

    if (dbVersion < 243) {
      migrate240();
    }

    if (dbVersion < 246) {
      migrate245();
    }

    if (dbVersion < 247) {
      migrate246();
    }

    if (dbVersion < 249) {
      migrate248();
    }

    if (dbVersion < 250) {
      migrate249();
    }

    if (dbVersion < 255) {
      migrate250();
    }

    if (dbVersion < 280) {
      migrate255();
    }

    if (dbVersion < 283) {
      migrate280();
    }
  }

  /**
   * The pattern to match method name is to match earlier db version.
   * For example, if earlier db version is 280,
   * next migration method name should be migrate280()
   */
  private void migrate280() {
    log.info("Migration from 280 starting");

    final Long versionID = (Long)hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Version v = new Version();
        //this should match the ambra version we will be going to deploy
        v.setName("Ambra 2.83");
        v.setVersion(283);
        v.setUpdateInProcess(true);
        session.save(v);

        execSQLScript(session, "migrate_ambra_2_8_3_part1.sql");

        v.setUpdateInProcess(false);
        session.update(v);

        return null;
      }
    });

    log.info("Migration from 280 complete");
  }


  /**
   * The pattern to match method name is to match earlier db version.
   * For example, if earlier db version is 237,
   * next migration method name should be migrate237()
   */
  private void migrate255() {
    log.info("Migration from 255 starting");

    final Long versionID = (Long)hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Version v = new Version();
        //this should match the ambra version we will be going to deploy
        v.setName("Ambra 2.80");
        v.setVersion(280);
        v.setUpdateInProcess(true);
        session.save(v);

        execSQLScript(session, "migrate_ambra_2_8_0_part1.sql");

        v.setUpdateInProcess(false);
        session.update(v);

        return null;
      }
    });

    log.info("Migration from 255 complete");
  }

  /**
   * The pattern to match method name is to match earlier db version.
   * For example, if earlier db version is 237,
   * next migration method name should be migrate237()
   */
  private void migrate250() {
    log.info("Migration from 250 starting");

    final Long versionID = (Long)hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Version v = new Version();
        //this should match the ambra version we will be going to deploy
        v.setName("Ambra 2.55");
        v.setVersion(255);
        v.setUpdateInProcess(true);
        session.save(v);

        execSQLScript(session, "migrate_ambra_2_5_5_part1.sql");

        return v.getID();
      }
    });

    //Now we have to populate a hash used to identify unique searchParameters
    List<SavedSearchQuery> queries = hibernateTemplate.loadAll(SavedSearchQuery.class);

    for(SavedSearchQuery query : queries) {
      String hash = TextUtils.createHash(query.getSearchParams());
      query.setHash(hash);

      hibernateTemplate.update(query);
    }

    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Version v = (Version) session.load(Version.class, versionID);

        //Now that hash is populated, add a null constraint, unique constraint and created index
        execSQLScript(session, "migrate_ambra_2_5_5_part2.sql");

        v.setUpdateInProcess(false);

        session.update(v);

        return null;
      }
    });


    log.info("Migration from 250 complete");
  }

  /**
   * The pattern to match method name is to match earlier db version.
   * For example, if earlier db version is 237,
   * next migration method name should be migrate237()
   */
  private void migrate249() {
    log.info("Migration from 249 starting");

    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Version v = new Version();
        //this should match the ambra version we will be going to deploy
        v.setName("Ambra 2.50");
        v.setVersion(250);
        v.setUpdateInProcess(true);
        session.save(v);

        execSQLScript(session, "migrate_ambra_2_5_0.sql");

        v.setUpdateInProcess(false);
        session.update(v);

        return null;
      }
    });
    log.info("Migration from 249 complete");
  }


  /**
   * The pattern to match method name is to match earlier db version.
   * For example, if earlier db version is 237,
   * next migration method name should be migrate237()
   */
  private void migrate248() {
    log.info("Migration from 248 starting");

    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Version v = new Version();
        //this should match the ambra version we will be going to deploy
        v.setName("Ambra 2.49");
        v.setVersion(249);
        v.setUpdateInProcess(true);
        session.save(v);

        log.debug("Creating new table.");

        execSQLScript(session, "migrate_ambra_2_4_9_part1.sql");

        v.setUpdateInProcess(false);
        session.update(v);

        return null;
      }
    });
    log.info("Migration from 248 complete");
  }

  /**
   * The pattern to match method name is to match earlier db version.
   * For example, if earlier db version is 237,
   * next migration method name should be migrate237()
  */
  private void migrate246() {
    log.info("Migration from 246 starting");

    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Version v = new Version();
        //this should match the ambra version we will be going to deploy
        v.setName("Ambra 2.48");
        v.setVersion(248);
        v.setUpdateInProcess(true);
        session.save(v);

        log.debug("Creating new table.");

        execSQLScript(session, "migrate_ambra_2_4_8_part1.sql");

        v.setUpdateInProcess(false);
        session.update(v);

        return null;
      }
    });
    log.info("Migration from 246 complete");
  }

  private void migrate245() {
    log.info("Migration from 245 starting");

    hibernateTemplate.execute(new HibernateCallback<Void>() {
      @Override
      public Void doInHibernate(Session session) throws HibernateException, SQLException {
        Version v = new Version();
        v.setName("Ambra 2.46");
        v.setVersion(246);
        v.setUpdateInProcess(true);
        session.save(v);

        execSQLScript(session, "migrate_ambra_2_4_6_part1.sql");

        v.setUpdateInProcess(false);
        session.update(v);

        return null;
      }
    });

    log.info("Migration from 245 complete");
  }

  private void migrate240() {
    log.info("Migration from 240 starting");

    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Version v = new Version();
        v.setName("Ambra 2.43");
        v.setVersion(243);
        v.setUpdateInProcess(true);
        session.save(v);

        execSQLScript(session, "migrate_ambra_2_4_3_part1.sql");

        v.setUpdateInProcess(false);
        session.update(v);

        return null;
      }
    });

    log.info("Migration from 240 complete");
  }


  private void migrate237() {
    log.info("Migration from 237 starting");

    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Version v = new Version();
        v.setName("Ambra 2.40");
        v.setVersion(240);
        v.setUpdateInProcess(true);
        session.save(v);

        log.debug("Creating new table.");

        execSQLScript(session, "migrate_ambra_2_4_0_part1.sql");

        v.setUpdateInProcess(false);
        session.update(v);

        return null;
      }
    });

    log.info("Migration from 237 complete");
  }

  private void migrate234() {
    log.info("Migration from 234 starting");

    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Version v = new Version();
        v.setName("Ambra 2.37");
        v.setVersion(237);
        v.setUpdateInProcess(true);
        session.save(v);

        log.debug("Creating new table.");

        execSQLScript(session, "migrate_ambra_2_3_7_part1.sql");

        log.debug("Table created, now generating data.");

        execSQLScript(session, "migrate_ambra_2_3_7_part2.sql");

        v.setUpdateInProcess(false);
        session.update(v);

        return null;
      }
    });

    log.info("Migration from 234 complete");
  }

  /*
  * Run the migration from 232 to 234
  **/
  private void migrate232() {
    log.info("Migration from 232 starting");

    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Version v = new Version();
        v.setName("Ambra 2.34");
        v.setVersion(234);
        v.setUpdateInProcess(true);
        session.save(v);

        log.debug("Creating new tables.");

        execSQLScript(session, "migrate_ambra_2_3_4_part1.sql");

        log.debug("Tables created, now migrating data.");

        execSQLScript(session, "migrate_ambra_2_3_4_part2.sql");

        log.debug("Migrated data, now dropping tables");

        execSQLScript(session, "migrate_ambra_2_3_4_part3.sql");

        v.setUpdateInProcess(false);
        session.update(v);

        return null;
      }
    });

    log.info("Migration from 232 complete");
  }

  /*
  * Run the migration from 230 to 232
  **/
  private void migrate230() {
    log.info("Migration from 230 starting");

    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Version v = new Version();
        v.setName("Ambra 2.32");
        v.setVersion(232);
        v.setUpdateInProcess(true);
        session.save(v);

        log.debug("Creating new tables.");
        execSQLScript(session, "migrate_ambra_2_3_2_part1.sql");
        log.debug("Tables created, now migrating and cleaning up data.");
        execSQLScript(session, "migrate_ambra_2_3_2_part2.sql");
        log.debug("Migrated data, now dropping tables");
        execSQLScript(session, "migrate_ambra_2_3_2_part3.sql");

        v.setUpdateInProcess(false);
        session.update(v);

        return null;
      }
    });

    log.info("Migration from 230 complete");
  }

  /*
  * Run the migration from 222 to 223
  **/
  private void migrate222() {
    log.info("Migration from 222 starting");

    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Version v = new Version();
        v.setName("Ambra 2.30");
        v.setVersion(230);
        v.setUpdateInProcess(true);
        session.save(v);

        log.debug("Creating new tables.");

        execSQLScript(session, "migrate_ambra_2_2_2_part1.sql");

        log.debug("Tables created, now migrating data.");

        execSQLScript(session, "migrate_ambra_2_2_2_part2.sql");

        log.debug("Cleaning up data");

        execSQLScript(session, "migrate_ambra_2_2_2_part3.sql");

        log.debug("Migrated data, now dropping tables");

        execSQLScript(session, "migrate_ambra_2_2_2_part4.sql");

        v.setUpdateInProcess(false);
        session.update(v);

        return null;
      }
    });

    log.info("Migration from 222 complete");
  }

  /*
  * Run the migration for ambra 2.10 to 2.20
  **/
  private void migrate210() {
    log.info("Migration from 210 starting");
    //First create version table and add one row

    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        log.debug("Creating new tables.");

        execSQLScript(session, "migrate_ambra_2_2_0_part1.sql");

        Version v = new Version();
        v.setName("Ambra 2.20");
        v.setVersion(220);
        v.setUpdateInProcess(true);
        session.save(v);

        log.debug("Tables created, now migrating data and removing old tables.");

        //We execute step #2 in a slightly different way as this file has SQL delimited in a different fashion
        //since it creates a trigger
        String sqlScript = "";
        try {
          log.debug("migrate_ambra_2_2_0_part2.sql started");
          sqlScript = getSQLScript("migrate_ambra_2_2_0_part2.sql");
          log.debug("migrate_ambra_2_2_0_part2.sql completed");
        } catch (IOException ex) {
          throw new HibernateException(ex.getMessage(), ex);
        }

        session.createSQLQuery(sqlScript).executeUpdate();

        execSQLScript(session, "migrate_ambra_2_2_0_part3.sql");

        //step 4 also creates a trigger, so we need to execute it the same as with step 2
        try {
          log.debug("migrate_ambra_2_2_0_part4.sql started");
          sqlScript = getSQLScript("migrate_ambra_2_2_0_part4.sql");
          log.debug("migrate_ambra_2_2_0_part4.sql completed");
        } catch (IOException ex) {
          throw new HibernateException(ex.getMessage(), ex);
        }
        session.createSQLQuery(sqlScript).executeUpdate();


        execSQLScript(session, "migrate_ambra_2_2_0_part5.sql");
        execSQLScript(session, "migrate_ambra_2_2_0_part6.sql");

        v.setUpdateInProcess(false);
        session.update(v);

        return null;
      }
    });

    log.info("Migration from 210 complete");
  }

  /*
  * Wait for other migrations to complete.  This will prevent two instances of ambra from attempting to execute the
  * same migration
  * */
  private void waitForOtherMigrations() throws InterruptedException {
    while (isMigrateRunning()) {
      log.debug("Waiting for another migration to complete.");
      Thread.sleep(10000);
    }
  }

  /*
  * Determine if a migration is already running
  **/
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

        int version = (Integer) c.uniqueResult();

        c = session.createCriteria(Version.class)
            .add(Restrictions.eq("version", version));

        Version v = (Version) c.uniqueResult();

        return (v == null) ? false : v.getUpdateInProcess();
      }
    });
  }

  /*
  * Load a mysql script from a resource
  * */
  private static String getSQLScript(String filename) throws IOException {
    InputStream is = BootstrapMigratorServiceImpl.class.getResourceAsStream(filename);
    StringBuilder out = new StringBuilder();

    byte[] b = new byte[4096];
    for (int n; (n = is.read(b)) != -1; ) {
      out.append(new String(b, 0, n));
    }

    return out.toString();
  }

  private static String[] getSQLCommands(String filename) throws IOException {
    String sqlString = getSQLScript(filename);
    ArrayList<String> sqlCommands = new ArrayList<String>();

    String sqlCommandsTemp[] = sqlString.split(";");

    for (String sqlCommand : sqlCommandsTemp) {
      if (sqlCommand.trim().length() > 0) {
        sqlCommands.add(sqlCommand);
      }
    }
    return sqlCommands.toArray(new String[0]);
  }

  private void setVersionData() throws IOException {
    setBinaryVersion();
    setDatabaseVersion();
  }

  /**
   * Get the current version of the binaries
   * <p/>
   * Assumptions about the version number: Only contains single-digit integers between dots (e.g., 2.2.1.6.9.3)
   *
   * @return binary version
   * @throws IOException when the class loader fails
   */
  private void setBinaryVersion() throws IOException {
    InputStream is = BootstrapMigratorServiceImpl.class.getResourceAsStream("version.properties");

    Properties prop = new Properties();
    prop.load(is);

    String sVersion = (String) prop.get("version");

    //Collapse pom version into an integer
    //Assume it is always three digits
    this.binaryVersion = Integer.parseInt(sVersion.replace(".", "").substring(0, 3));
  }

  /*
  * Get the current version of the database
  * */
  @SuppressWarnings("unchecked")
  private void setDatabaseVersion() {
    this.dbVersion = ((Integer) hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        SQLQuery q = session.createSQLQuery("show tables");
        List<String> tables = q.list();

        //Check to see if the version table exists.
        //If it does not exist then it's ambra 2.00
        if (!tables.contains("version")) {
          return 210;
        }

        //If we get this far, return the version column out of the database
        Criteria c = session.createCriteria(Version.class)
            .setProjection(Projections.max("version"));

        Integer i = (Integer) c.uniqueResult();

        return (i == null) ? 210 : c.uniqueResult();
      }
    }));
  }

  private void execSQLScript(Session session, String sqlScript) throws SQLException, HibernateException {
    log.debug("{} started.", sqlScript);
    String sqlStatements[] = {""};

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
