/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
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

package org.topazproject.mulgara.resolver;

import java.util.logging.Level;

import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.server.SessionFactory;
import org.mulgara.server.driver.SessionFactoryFinder;

/**
 * Base class for the tests.
 *
 * @author Ronald Tschalär
 */
abstract class AbstractTest extends GroovyTestCase {
  protected static final String MULGARA    = 'local:///topazproject'
  protected static final String TEST_GRAPH = "<${MULGARA}#filter:graph=test>"
  protected static final String REAL_GRAPH = "<${MULGARA}#test>"
  protected static final String RSLV_TYPE  = "<${FilterResolver.GRAPH_TYPE}>"

  protected final SessionFactory sf = SessionFactoryFinder.newSessionFactory(MULGARA.toURI())

  protected ItqlInterpreterBean itql

  protected void openDb() {
    sf.setDirectory(new File(System.getProperty('basedir'), "target${File.separator}mulgara-db"))
    sf.newSession().close()     // force initialization of resolver and filter-handler

    itql = new ItqlInterpreterBean(sf.newSession(), sf.getSecurityDomain())
    itql.setAliasMap([topaz:'http://rdf.topazproject.org/RDF/'.toURI()])
  }

  protected void closeDb() {
    itql.close()
    sf.close()
  }

  void setUp() {
    openDb()
  }

  void tearDown() {
    closeDb();
  }

  protected void resetDb() {
    try {
      itql.executeUpdate("drop ${TEST_GRAPH};")
    } catch (Exception e) {
      log.log(Level.FINE, "Error dropping ${TEST_GRAPH} (probably because it doesn't exist)", e)
    }
    try {
      itql.executeUpdate("drop ${REAL_GRAPH};")
    } catch (Exception e) {
      log.log(Level.FINE, "Error dropping ${REAL_GRAPH} (probably because it doesn't exist)", e)
    }

    itql.executeUpdate("create ${TEST_GRAPH} ${RSLV_TYPE};")
  }
}
