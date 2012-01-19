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
package org.topazproject.otm.event;

import org.topazproject.otm.Session;
import org.topazproject.otm.mapping.Mapper;

/**
 * Called after loading from the datastore
 *
 * @author Pradeep Krishnan
 */
public interface PostLoadEventListener {
  /**
   * Do any post-load processing. Note that only eager loaded fields
   * are loaded at this point.
   *
   * @param session the session that is reporting this event
   * @param object the object for which the event is being generated
   */
  public void onPostLoad(Session session, Object object);

  /**
   * Do any post-load processing of a delayed load of a lazy loaded field.
   *
   * @param session the session that is reporting this event
   * @param object the object for which the event is being generated
   * @param field the field for which the event is being generated
   */
  public void onPostLoad(Session session, Object object, Mapper field);
}
