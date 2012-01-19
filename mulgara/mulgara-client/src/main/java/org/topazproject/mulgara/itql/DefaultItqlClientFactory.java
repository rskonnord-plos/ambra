/* $HeadURL::                                                                             $
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

package org.topazproject.mulgara.itql;

import java.net.URI;
import java.net.URL;

/**
 * The default implementation {@link ItqlClientFactory ItqlClientFactory}.
 *
 * @author Ronald Tschalär
 */
public class DefaultItqlClientFactory implements ItqlClientFactory {
  private static final String DEF_MEM_CONF = "mulgara-mem-config.xml";
  private static final String DEF_DSK_CONF = "mulgara-emb-config.xml";

  private String dbDir = null;

  /**
   * Create a new itql-client instance. Uri schemes are currently mapped as follows:
   * <dl>
   *   <dt>rmi</dt>
   *   <dd>Use RMI</dd>
   *   <dt>http</dt>
   *   <dd>Use SOAP over http (deprecated - may be changed to use a REST API in the future)</dd>
   *   <dt>soap</dt>
   *   <dd>Use SOAP over http</dd>
   *   <dt>local</dt>
   *   <dd>Create embedded mulgara instance</dd>
   *   <dt>mem</dt>
   *   <dd>Create an in-memory embedded mulgara instance</dd>
   * </dl>
   *
   * @param uri  the server's URI
   * @return the new client
   * @throws Exception
   */
  public ItqlClient createClient(URI uri) throws Exception {
    String scheme = uri.getScheme();
    if (scheme.equals("rmi"))
      return new RmiClient(uri, this);
    if (scheme.equals("http"))
      return new SoapClient(uri);
    if (scheme.equals("soap"))
      return new SoapClient(new URI("http", uri.getSchemeSpecificPart(), uri.getFragment()));
    if (scheme.equals("local"))
      return new EmbeddedClient(uri, getDbDir(uri), getLocalConf(uri), this);
    if (scheme.equals("mem"))
      return new EmbeddedClient(uri, getDbDir(uri), getMemoryConf(uri), this);

    throw new IllegalArgumentException("Unsupported scheme '" + scheme + "' from '" + uri + "'");
  }

  /**
   * Set the database directory to use for all embedded instances. If finer grained control is
   * needed, override {@link #getDbDir(URI) getDbDir} instead.
   *
   * @param dir the directory to use; if null, a (different) temporary directory will be used
   *            for each instance.
   */
  public void setDbDir(String dir) {
    dbDir = dir;
  }

  /**
   * Get the database to use for an embedded instance.
   *
   * @param uri the uri of the database
   * @return the directory to use for the database; if null, a (different) temporary directory will
   *         be used for each instance.
   * @see #setDbDir(String)
   */
  public String getDbDir(URI uri) {
    return dbDir;
  }

  /**
   * Get the mulgara-config to use for a disk-based embedded instance.
   *
   * @param uri the uri of the database
   * @return the URL of the config to use.
   */
  public URL getLocalConf(URI uri) {
    return DefaultItqlClientFactory.class.getResource(DEF_DSK_CONF);
  }

  /**
   * Get the mulgara-config to use for an in-memory embedded instance.
   *
   * @param uri the uri of the database
   * @return the URL of the config to use.
   */
  public URL getMemoryConf(URI uri) {
    return DefaultItqlClientFactory.class.getResource(DEF_MEM_CONF);
  }
}
