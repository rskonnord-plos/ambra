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

import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * A set of changes to move the database schema from one version to the next.
 * <p/>
 * Implementations should be immutable.
 *
 * @author Ryan Skonnord
 */
interface Migration {

  /**
   * Get this migration's schema version number. This value is inserted into the {@code version} table when this
   * migration is applied. Migrations must always be applied in ascending order of their version numbers.
   * <p/>
   * These numbers are tied to Ambra release versions in legacy migrations only. All values greater than 1000 are
   * arbitrary schema identifiers, which are meaningful only relative to each other.
   *
   * @return the schema version number
   * @see org.ambraproject.models.Version
   */
  public abstract int getVersion();

  /**
   * Apply this migration.
   *
   * @param hibernateTemplate the API for applying the migration to the database
   */
  public abstract void migrate(HibernateTemplate hibernateTemplate);

}
