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
package org.plos.cache;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Interceptor;
import org.topazproject.otm.Session;

/**
 * Listener for OTM Object changes.
 *
 * @author Pradeep Krishnan
 */
public interface ObjectListener extends Listener {
  /**
   * Fired when an object is added to the cache
   *
   * @param session the session that is reporting this change
   * @param cm the key
   * @param id the value
   * @param object the object that changed
   * @param update the changes or null if previous state is unavailable
   */
  public void objectChanged(Session session, ClassMetadata cm, String id, Object object, 
          Interceptor.Updates updates);

  /**
   * Fired when an object is removed from the cache.
   *
   * @param session the session that is reporting this change
   * @param cm the cache
   * @param id the key
   * @param object the object that got deleted
   */
  public void objectRemoved(Session session, ClassMetadata cm, String id, Object object);
}
