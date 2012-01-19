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
 * This represents a factory for {@link ItqlClient ItqlClient} instances.
 *
 * @author Ronald Tschalär
 */
public interface ItqlClientFactory {
  /** 
   * Create a new itql-client instance.
   * 
   * @param uri  the server's URI
   * @return the new client
   * @throws Exception on error
   */
  public ItqlClient createClient(URI uri) throws Exception;
}
