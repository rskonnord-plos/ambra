/* $HeadURL::                                                                            $
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
package org.topazproject.ambra.cache;

import javax.transaction.TransactionManager;

import org.topazproject.otm.SessionFactory;

/**
 * A factory class for CacheManagers that share the same TransactionManager as OTM.
 *
 * @author Pradeep Krishnan
 */
public class CacheManagerFactory {
  private SessionFactory sessionFactory;

  /**
   * Creates a new cache manager factory.
   *
   * @param factory the session factory
   */
  public CacheManagerFactory(SessionFactory factory) {
    this.sessionFactory = factory;
  }

  /**
   * Creates a new CacheManager
   *
   * @param lockWaitSeconds number of seconds to wait to acquire a lock
   *
   * @return the newly created CacheManager instance
   */
  public CacheManager createCacheManager(long lockWaitSeconds) {
    return new CacheManager(this, lockWaitSeconds);
  }

  /**
   * Gets the transaction manager.
   *
   * @return gets the transaction manager
   */
  public TransactionManager getTransactionManager() {
    return sessionFactory.getTransactionManager();
  }
}
