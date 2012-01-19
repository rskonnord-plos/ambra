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
package org.topazproject.ambra.migration;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topazproject.ambra.configuration.ConfigurationStore;


/**
 * Does migrations on startup.
 *
 * @author Pradeep Krishnan
 */
public class AmbraBootstrapMigrator implements BootstrapMigrator {
  private static Logger log = LoggerFactory.getLogger(AmbraBootstrapMigrator.class);

  /**
   * Apply all migrations.
   *
   * @throws Exception on an error
   */
  public void migrate() throws Exception {

    try {
      Configuration conf = ConfigurationStore.getInstance().getConfiguration();
    } finally {

    }
  }
}
