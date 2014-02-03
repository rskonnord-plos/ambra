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
import com.google.common.collect.ImmutableList;
import org.hibernate.Session;

import java.sql.SQLException;

/**
 * A simple migration that applies one or more resource files as SQL scripts.
 *
 * @author Ryan Skonnord
 */
class ScriptMigration extends SchemaMigration {

  private final ImmutableList<String> scriptPaths;

  /**
   * @param version     the schema version number
   * @param scriptPaths resource filenames of the SQL scrips to apply, in order
   */
  ScriptMigration(int version, String... scriptPaths) {
    super(version);
    this.scriptPaths = ImmutableList.copyOf(scriptPaths);
    Preconditions.checkArgument(!this.scriptPaths.isEmpty(), "Empty list of migration scripts");
  }

  @Override
  protected void execute(Session session) throws SQLException {
    for (String scriptPath : scriptPaths) {
      BootstrapMigratorServiceImpl.execSQLScript(session, scriptPath);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ScriptMigration that = (ScriptMigration) o;

    if (!scriptPaths.equals(that.scriptPaths)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + scriptPaths.hashCode();
    return result;
  }

}
