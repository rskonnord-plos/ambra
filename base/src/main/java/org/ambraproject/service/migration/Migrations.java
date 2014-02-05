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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Collection;
import java.util.SortedMap;

/**
 * Static class for declaring non-legacy migrations.
 */
class Migrations {
  private Migrations() {
    throw new AssertionError("Not instantiable");
  }

  private static final ImmutableCollection<SchemaMigration> MIGRATIONS = ImmutableList.copyOf(new SchemaMigration[]{

      // New migrations may be added to the end of this array.
      //
      // For migrations that only require running one or more SQL scripts, add a line like this:
      //      new ScriptMigration(1001, "migration_1001_part1.sql", "migration_1001_part2.sql"),
      //
      // For migrations that require more complex procedural logic on a Session, add an object like this:
      //      new SchemaMigration(1001) {
      //        @Override protected void execute(Session session) throws SQLException {
      //          // Do stuff
      //        }
      //      },
    new ScriptMigration(1001, "migrate_ambra_1001.sql")
  });

  /**
   * Get all migrations (legacy and non-legacy) necessary to migrate the database schema from its earliest version to
   * the present version.
   *
   * @return the ordered list of all migrations
   */
  static ImmutableList<Migration> getAllMigrations() {
    Collection<Migration> migrations = Lists.newArrayList();
    migrations.addAll(Arrays.asList(LegacyMigration.values()));
    migrations.addAll(MIGRATIONS);
    return sort(migrations);
  }

  /**
   * Sort migrations by version, checking that each version is unique and conforms to convention.
   *
   * @param migrations distinct migration objects
   * @return the migrations, sorted by version
   * @throws RuntimeException if a migration violates the numbering convention
   */
  private static ImmutableList<Migration> sort(Iterable<? extends Migration> migrations) {
    SortedMap<Integer, Migration> map = Maps.newTreeMap();
    for (Migration migration : migrations) {
      int version = migration.getVersion();

      // Validate that version number matches convention
      if (version <= SchemaMigration.THRESHOLD && !(migration instanceof LegacyMigration)) {
        String message = String.format("Illegal migration version: %d; non-legacy migrations must have version > %d",
            version, SchemaMigration.THRESHOLD);
        throw new RuntimeException(message);
      }

      if (map.put(version, migration) != null) {
        throw new RuntimeException("Collision on migration version: " + version);
      }
    }
    return ImmutableList.copyOf(map.values());
  }

}
