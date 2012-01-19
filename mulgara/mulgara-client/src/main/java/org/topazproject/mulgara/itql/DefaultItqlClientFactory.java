/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.mulgara.itql;

import java.net.URI;

/**
 * The default implementation {@link ItqlClientFactory ItqlClientFactory}.
 *
 * @author Ronald Tschalär
 */
public class DefaultItqlClientFactory implements ItqlClientFactory {
  private static final String MEM_CONF = "mulgara-mem-config.xml";
  private static final String DSK_CONF = "mulgara-emb-config.xml";

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
      return new EmbeddedClient(uri, getDbDir(uri), getClass().getResource(DSK_CONF), this);
    if (scheme.equals("mem"))
      return new EmbeddedClient(uri, getDbDir(uri), getClass().getResource(MEM_CONF), this);

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
}
