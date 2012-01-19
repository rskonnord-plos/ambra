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

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Interceptor;
import org.topazproject.otm.Session;

/**
 * An abstract no-op listener implementation.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractObjectListener implements ObjectListener {
  public void objectChanged(Session session, ClassMetadata cm, String id, Object object, 
          Interceptor.Updates updates) {
  }

  public void objectRemoved(Session session, ClassMetadata cm, String id, Object object) {
  }

  public void removing(Session session, ClassMetadata cm, String id, Object object)
    throws Exception {
  }
}
