/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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
package org.topazproject.otm;

import javax.transaction.xa.XAResource;

/**
 * A connection impl that stores the current session.
 *
 * @author Pradeep Krishnan
  */
public abstract class AbstractConnection implements Connection {
  private final Session sess;

  /**
   * Create a new connection.
   *
   * @param sess the session this connection belongs to
   */
  protected AbstractConnection(Session sess) {
    this.sess = sess;
  }

  /**
   * Get the owning session.
   *
   * @return the session
   */
  public Session getSession() {
    return sess;
  }

  /**
   * Enlist the given xa-resource with the session's transaction.
   *
   * @param xaRes the xa-resource to enlist
   * @throws OtmException if an error occurred enlisting <var>xaRes</var>
   */
  protected void enlistResource(XAResource xaRes) throws OtmException {
    try {
      sess.getTransaction().getJtaTransaction().enlistResource(xaRes);
    } catch (Exception e) {
      throw new OtmException("Error enlisting resource", e);
    }
  }
}
