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
import java.util.Map;
import java.util.WeakHashMap;

import org.mulgara.connection.Connection;
import org.mulgara.connection.ConnectionException;
import org.mulgara.connection.SessionConnection;

/**
 * A mulgara client using RMI.
 *
 * @author Ronald Tschalär
 */
public class RmiClient extends TIClient {
  private static final Map<RmiClient, Connection> liveConnections =
                                                      new WeakHashMap<RmiClient, Connection>();
  private static final Thread shutdownHook;

  static {
    shutdownHook = new Thread() {
      public void run() {
        // close all session factories
        synchronized (liveConnections) {
          // Note: need to make a copy of the values here so we don't get values removed
          // from out under us; we also need to make sure the copy is "atomic"
          for (Connection c : liveConnections.values().toArray(new Connection[0])) {
            try {
              c.dispose();
            } catch (Exception e) {
              System.err.println("Error closing connection '" + c + "': " + e);
            }
          }
          liveConnections.clear();
        }
      }
    };

    Runtime.getRuntime().addShutdownHook(shutdownHook);
  }

  /**
   * Release all static resources associated with this class. This is meant to be used in an
   * environment where classes are reloaded in order to make sure all references to this class
   * or to related classes are removed. Note that this closes all session-factories, so the
   * caller must ensure they will not be needed anymore.
   */
  public static void releaseResources() {
    Runtime.getRuntime().removeShutdownHook(shutdownHook);
    shutdownHook.run();
  }

  /**
   * Create a new instance pointed at the given database.
   *
   * @param database  the url of the database
   * @param icf       the client-factory instance creating this
   * @throws ConnectionException if an error occurred setting up the connector
   */
  public RmiClient(URI database, ItqlClientFactory icf) throws ConnectionException {
    super(new SessionConnection(database));
    synchronized (liveConnections) {
      liveConnections.put(this, con);
    }
  }
}
