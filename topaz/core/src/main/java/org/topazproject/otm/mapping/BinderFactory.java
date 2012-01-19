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
package org.topazproject.otm.mapping;

import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;

/**
 * A binder factory that can create a PropertyBinder for a given property.
 *
 * @author Pradeep Krishnan
 */
public interface BinderFactory {
  /**
   * The property name for which this factory is created.
   *
   * @return name of this {@link org.topazpropject.otm.metadata.PropertyDefinition}
   */
  String getPropertyName();

  /**
   * The entity mode for which this factory can create the binder.
   *
   * @return the entity mode
   */
  EntityMode getEntityMode();

  /**
   * The factory method to create a PropertyBinder.
   *
   * @param sf the session factory
   *
   * @return the newly created PropertyBinder
   *
   * @throws OtmException on an error
   */
  PropertyBinder createBinder(SessionFactory sf) throws OtmException;
}
