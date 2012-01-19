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

import java.util.Map;

import org.topazproject.otm.EntityMode;
import org.topazproject.otm.Session;
import org.topazproject.otm.metadata.PropertyDefinition;

/**
 * Defines a Mapper for a property .
 *
 * @author Pradeep Krishnan
 */
public interface Mapper {
  /**
   * Get the PropertyBinder for this field.
   *
   * @param session the session
   *
   * @return the binder for this field
   */
  public PropertyBinder getBinder(Session session);

  /**
   * Get the PropertyBinder for this field.
   *
   * @param mode the entity mode
   *
   * @return the binder for this field
   */
  public PropertyBinder getBinder(EntityMode mode);

  /**
   * Gets the name of the field.
   *
   * @return the name
   */
  public String getName();

  /**
   * Gets all defined binders.
   *
   * @return the name
   */
  public Map<EntityMode, PropertyBinder> getBinders();

  /**
   * Gets the property definition for this mapper.
   *
   * @return the name
   */
  public PropertyDefinition getDefinition();
}
