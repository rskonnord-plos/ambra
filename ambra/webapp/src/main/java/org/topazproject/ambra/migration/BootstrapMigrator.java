/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;

import java.net.URI;
import java.util.List;

/**
 * Does migrations on startup.
 *
 * @author Pradeep Krishnan
 */
public interface BootstrapMigrator {
  /**
   * Apply all migrations.
   *
   * @throws Exception on an error
   */
  public void migrate() throws Exception;

  public List<String> getGraphs(Session session, URI mulgara) throws OtmException;

  /**
   * Drop obsolete graphs. Ignore the exceptions as graphs might not exist.
   *
   * @param session the Topaz session
   * @param graphs the list of graphs in mulgara
   * @return the number of migrations performed
   * @throws OtmException on an error
   */
  public int dropObsoleteGraphs(Session session, List<String> graphs) throws OtmException;

  /**
   * Add xsd:int to topaz:state.
   *
   * @param sess the otm session to use
   * @param graphs the list of graphs in mulgara
   * @return the number of migrations performed
   * @throws OtmException on an error
   */
  public int addXsdIntToTopazState(Session sess, List<String> graphs) throws OtmException;
}
