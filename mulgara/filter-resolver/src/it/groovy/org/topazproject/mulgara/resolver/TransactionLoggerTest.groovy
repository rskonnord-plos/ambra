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

/**
 * Basic tests on the transaction-logger filter-handler.
 *
 * @author Ronald Tschalär
 */
class TransactionLoggerTest extends AbstractTest {
  def log = new File(System.properties['basedir'], "target${File.separator}mulgara.backup.log")

  void openDb() {
    System.properties[FilterResolverFactory.CONFIG_FACTORY_CONFIG_PROPERTY] =
                                          "/conf/topaz-factory-config.transaction-logger-test.xml"
    log.delete()

    super.openDb();
  }

  void testLogger() {
    def script = getClass().getResource("/transaction-logger-test.tql")
    script.eachLine { l -> itql.execute(l) }

    Thread.sleep(50);

    def exp = script.getText().replaceAll('\\s*\\bselect .*?;', '')
    assertEquals(exp, log.getText())
  }
}
