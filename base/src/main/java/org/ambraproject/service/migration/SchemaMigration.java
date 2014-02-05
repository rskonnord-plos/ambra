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
import org.ambraproject.models.Version;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.sql.SQLException;

/**
 * A "non-legacy" migration object. Its version number refers to a <em>schema</em> version and not an Ambra release
 * version, hence the class name.
 *
 * @author Ryan Skonnord
 */
abstract class SchemaMigration implements Migration {
  private static Logger log = LoggerFactory.getLogger(SchemaMigration.class);

  /**
   * Minimum (exclusive) value for post-legacy migrations. Any migration with a version less than 1000 has its version
   * number (undesirably) tied to an Ambra release. Migration version numbers greater than 1000 identify the schema
   * version only and are independent from Ambra release version numbers.
   */
  static final int THRESHOLD = 1000;

  private final int version;

  SchemaMigration(int version) {
    Preconditions.checkArgument(version > THRESHOLD,
        "Migration version numbers below " + THRESHOLD + " are reserved for legacy migrations");
    this.version = version;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getVersion() {
    return version;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void migrate(HibernateTemplate hibernateTemplate) {
    log.info("Migration to {} starting", version);

    hibernateTemplate.execute(new HibernateCallback<Version>() {
      @Override
      public Version doInHibernate(Session session) throws HibernateException, SQLException {
        Version versionObj = new Version();
        versionObj.setName("Schema " + version);
        versionObj.setVersion(version);
        versionObj.setUpdateInProcess(true);
        session.save(versionObj);

        execute(session);

        versionObj.setUpdateInProcess(false);
        session.update(versionObj);

        return versionObj;
      }
    });

    log.info("Migration to {} complete", version);
  }

  /**
   * Execute the migration. This method should affect only the tables being migrated, not the {@code version} table.
   *
   * @param session the Hibernate session
   * @throws SQLException
   */
  protected abstract void execute(Session session) throws SQLException;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SchemaMigration that = (SchemaMigration) o;
    return (version == that.version);
  }

  @Override
  public int hashCode() {
    return version;
  }

}
