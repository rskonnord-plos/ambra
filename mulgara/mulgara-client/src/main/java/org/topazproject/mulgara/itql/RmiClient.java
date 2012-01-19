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

import org.mulgara.connection.ConnectionException;
import org.mulgara.connection.SessionConnection;

/** 
 * A mulgara client using RMI.
 *
 * @author Ronald Tschalär
 */
public class RmiClient extends TIClient {
  /** 
   * Create a new instance pointed at the given database.
   * 
   * @param database  the url of the database
   * @param icf       the client-factory instance creating this
   * @throws ConnectionException if an error occurred setting up the connector
   */
  public RmiClient(URI database, ItqlClientFactory icf) throws ConnectionException {
    super(new SessionConnection(database));
  }
}
