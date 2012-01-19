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
package org.topazproject.otm.context;

import java.io.Serializable;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;

/**
 * Defines the contract for implementations which know how to scope the notion of a
 * {@link org.topazproject.otm.SessionFactory#getCurrentSession() current session}.
 * <p/>
 * Implementations should adhere to the following:
 * <ul>
 * <li>contain a constructor accepting a single argument of type
 * {@link org.topazproject.otm.SessionFactory}
 * <li>should be thread safe
 * <li>should be fully serializable
 * </ul>
 * <p/>
 * Implementors should be aware that they are also fully responsible for
 * cleanup of any generated current-sessions.
 * <p/>
 * Note that there will be exactly one instance of the configured
 * CurrentSessionContext implementation per {@link org.topazproject.otm.SessionFactory}.
 *
 * @author Pradeep Krishnan (borrowed from Hibernate)
 */
public interface CurrentSessionContext extends Serializable {
  /**
   * Retrieve the current session according to the scoping defined by this implementation.
   *
   * @return The current session.
   *
   * @throws OtmException Typically indicates an issue locating or creating the current session.
   */
  public Session currentSession() throws OtmException;
}
