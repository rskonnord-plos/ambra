/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
 *    http://plos.org
 *    http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.service.migration;

import com.google.common.base.Preconditions;
import org.ambraproject.models.SavedSearchQuery;
import org.ambraproject.models.Version;
import org.ambraproject.util.TextUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Hard-coded migration logic abstracted into the {@link Migration} interface, but otherwise untouched.
 * <p/>
 * The version numbering on these migrations comes from the legacy practice of tying schema version numbers to Ambra
 * release numbers. Hence, all of these versions, and only these, are less than 1000.
 * <p/>
 * Do not add any new values to this enum.
 */
enum LegacyMigration implements Migration {

  M280(282) {
    public void migrate(HibernateTemplate hibernateTemplate) {
      log.info("Migration from 280 starting");

      final Long versionID = (Long) hibernateTemplate.execute(new HibernateCallback() {
        @Override
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
          Version v = new Version();
          //this should match the ambra version we will be going to deploy
          v.setName("Ambra 2.82");
          v.setVersion(282);
          v.setUpdateInProcess(true);
          session.save(v);

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_8_2_part1.sql");

          v.setUpdateInProcess(false);
          session.update(v);

          return null;
        }
      });

      log.info("Migration from 280 complete");
    }
  },

  M255(280) {
    public void migrate(HibernateTemplate hibernateTemplate) {
      log.info("Migration from 255 starting");

      final Long versionID = (Long) hibernateTemplate.execute(new HibernateCallback() {
        @Override
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
          Version v = new Version();
          //this should match the ambra version we will be going to deploy
          v.setName("Ambra 2.80");
          v.setVersion(280);
          v.setUpdateInProcess(true);
          session.save(v);

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_8_0_part1.sql");

          v.setUpdateInProcess(false);
          session.update(v);

          return null;
        }
      });

      log.info("Migration from 255 complete");
    }
  },

  M250(255) {
    public void migrate(HibernateTemplate hibernateTemplate) {
      log.info("Migration from 250 starting");

      final Long versionID = (Long) hibernateTemplate.execute(new HibernateCallback() {
        @Override
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
          Version v = new Version();
          //this should match the ambra version we will be going to deploy
          v.setName("Ambra 2.55");
          v.setVersion(255);
          v.setUpdateInProcess(true);
          session.save(v);

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_5_5_part1.sql");

          return v.getID();
        }
      });

      //Now we have to populate a hash used to identify unique searchParameters
      List<SavedSearchQuery> queries = hibernateTemplate.loadAll(SavedSearchQuery.class);

      for (SavedSearchQuery query : queries) {
        String hash = TextUtils.createHash(query.getSearchParams());
        query.setHash(hash);

        hibernateTemplate.update(query);
      }

      hibernateTemplate.execute(new HibernateCallback() {
        @Override
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
          Version v = (Version) session.load(Version.class, versionID);

          //Now that hash is populated, add a null constraint, unique constraint and created index
          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_5_5_part2.sql");

          v.setUpdateInProcess(false);

          session.update(v);

          return null;
        }
      });


      log.info("Migration from 250 complete");
    }
  },

  M249(250) {
    public void migrate(HibernateTemplate hibernateTemplate) {
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

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_5_0.sql");

          v.setUpdateInProcess(false);
          session.update(v);

          return null;
        }
      });
      log.info("Migration from 249 complete");
    }
  },

  M248(249) {
    public void migrate(HibernateTemplate hibernateTemplate) {
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

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_4_9_part1.sql");

          v.setUpdateInProcess(false);
          session.update(v);

          return null;
        }
      });
      log.info("Migration from 248 complete");
    }
  },

  M246(247) {
    public void migrate(HibernateTemplate hibernateTemplate) {
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

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_4_8_part1.sql");

          v.setUpdateInProcess(false);
          session.update(v);

          return null;
        }
      });
      log.info("Migration from 246 complete");
    }
  },

  M245(246) {
    public void migrate(HibernateTemplate hibernateTemplate) {
      log.info("Migration from 245 starting");

      hibernateTemplate.execute(new HibernateCallback<Void>() {
        @Override
        public Void doInHibernate(Session session) throws HibernateException, SQLException {
          Version v = new Version();
          v.setName("Ambra 2.46");
          v.setVersion(246);
          v.setUpdateInProcess(true);
          session.save(v);

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_4_6_part1.sql");

          v.setUpdateInProcess(false);
          session.update(v);

          return null;
        }
      });

      log.info("Migration from 245 complete");
    }
  },

  M240(243) {
    public void migrate(HibernateTemplate hibernateTemplate) {
      log.info("Migration from 240 starting");

      hibernateTemplate.execute(new HibernateCallback() {
        @Override
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
          Version v = new Version();
          v.setName("Ambra 2.43");
          v.setVersion(243);
          v.setUpdateInProcess(true);
          session.save(v);

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_4_3_part1.sql");

          v.setUpdateInProcess(false);
          session.update(v);

          return null;
        }
      });

      log.info("Migration from 240 complete");
    }
  },

  M237(240) {
    public void migrate(HibernateTemplate hibernateTemplate) {
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

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_4_0_part1.sql");

          v.setUpdateInProcess(false);
          session.update(v);

          return null;
        }
      });

      log.info("Migration from 237 complete");
    }
  },

  M234(237) {
    public void migrate(HibernateTemplate hibernateTemplate) {
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

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_3_7_part1.sql");

          log.debug("Table created, now generating data.");

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_3_7_part2.sql");

          v.setUpdateInProcess(false);
          session.update(v);

          return null;
        }
      });

      log.info("Migration from 234 complete");
    }
  },

  M232(234) {
    public void migrate(HibernateTemplate hibernateTemplate) {
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

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_3_4_part1.sql");

          log.debug("Tables created, now migrating data.");

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_3_4_part2.sql");

          log.debug("Migrated data, now dropping tables");

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_3_4_part3.sql");

          v.setUpdateInProcess(false);
          session.update(v);

          return null;
        }
      });

      log.info("Migration from 232 complete");
    }
  },

  M230(232) {
    public void migrate(HibernateTemplate hibernateTemplate) {
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
          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_3_2_part1.sql");
          log.debug("Tables created, now migrating and cleaning up data.");
          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_3_2_part2.sql");
          log.debug("Migrated data, now dropping tables");
          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_3_2_part3.sql");

          v.setUpdateInProcess(false);
          session.update(v);

          return null;
        }
      });

      log.info("Migration from 230 complete");
    }
  },

  M222(223) {
    public void migrate(HibernateTemplate hibernateTemplate) {
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

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_2_2_part1.sql");

          log.debug("Tables created, now migrating data.");

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_2_2_part2.sql");

          log.debug("Cleaning up data");

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_2_2_part3.sql");

          log.debug("Migrated data, now dropping tables");

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_2_2_part4.sql");

          v.setUpdateInProcess(false);
          session.update(v);

          return null;
        }
      });

      log.info("Migration from 222 complete");
    }
  },

  M210(220) {
    public void migrate(HibernateTemplate hibernateTemplate) {
      log.info("Migration from 210 starting");
      //First create version table and add one row

      hibernateTemplate.execute(new HibernateCallback() {
        @Override
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
          log.debug("Creating new tables.");

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_2_0_part1.sql");

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
            sqlScript = BootstrapMigratorServiceImpl.getSQLScript("migrate_ambra_2_2_0_part2.sql");
            log.debug("migrate_ambra_2_2_0_part2.sql completed");
          } catch (IOException ex) {
            throw new HibernateException(ex.getMessage(), ex);
          }

          session.createSQLQuery(sqlScript).executeUpdate();

          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_2_0_part3.sql");

          //step 4 also creates a trigger, so we need to execute it the same as with step 2
          try {
            log.debug("migrate_ambra_2_2_0_part4.sql started");
            sqlScript = BootstrapMigratorServiceImpl.getSQLScript("migrate_ambra_2_2_0_part4.sql");
            log.debug("migrate_ambra_2_2_0_part4.sql completed");
          } catch (IOException ex) {
            throw new HibernateException(ex.getMessage(), ex);
          }
          session.createSQLQuery(sqlScript).executeUpdate();


          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_2_0_part5.sql");
          BootstrapMigratorServiceImpl.execSQLScript(session, "migrate_ambra_2_2_0_part6.sql");

          v.setUpdateInProcess(false);
          session.update(v);

          return null;
        }
      });

      log.info("Migration from 210 complete");
    }
  };

  private static final Logger log = LoggerFactory.getLogger(LegacyMigration.class);

  static final int MIN_VERSION = 210;

  private final int version;

  private LegacyMigration(int version) {
    Preconditions.checkArgument(version > MIN_VERSION);
    Preconditions.checkArgument(version < SchemaMigration.THRESHOLD);
    this.version = version;
  }

  @Override
  public int getVersion() {
    return version;
  }

}
