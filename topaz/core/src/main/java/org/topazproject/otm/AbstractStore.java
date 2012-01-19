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

import java.util.Collections;

/**
 * A convenient base class for Stores.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractStore implements Store {
  /**
   * Create or update an entity instance.
   *
   * @param sess the current session
   * @param cm the class metadata of the entity
   * @param id the id of the instance
   * @param instance the instance to update or null
   *
   * @return newly created entity instance
   *
   * @throws OtmException on an error
   */
  protected Object createOrUpdateInstance(Session sess, ClassMetadata cm, String id, Object instance)
                                   throws OtmException {
    if (instance == null)
      instance = cm.getEntityBinder(sess).newInstance();
    cm.getIdField().getBinder(sess).set(instance, Collections.singletonList(id));

    return instance;
  }
}
