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

/**
 * A convenient base class for all mappers.
 *
 * @author Pradeep krishnan
 */
public abstract class AbstractMapper implements Mapper {
  private final Map<EntityMode, PropertyBinder> propertyBinders;

  /**
   * Creates a new AbstractMapper object.
   *
   * @param propertyBinders the propertyBinders for this property
   */
  public AbstractMapper(Map<EntityMode, PropertyBinder> propertyBinders) {
    this.propertyBinders = propertyBinders;
  }

  /*
   * inherited javadoc
   */
  public PropertyBinder getBinder(Session session) {
    return getBinder(session.getEntityMode());
  }

  /*
   * inherited javadoc
   */
  public PropertyBinder getBinder(EntityMode mode) {
    return propertyBinders.get(mode);
  }

  /*
   * inherited javadoc
   */
  public String getName() {
    return getDefinition().getLocalName();
  }

  /*
   * inherited javadoc
   */
  public Map<EntityMode, PropertyBinder> getBinders() {
    return propertyBinders;
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return getClass().getName() + "[def=" + getDefinition() + ", propertyBinders =" + propertyBinders + "]";
  }
}
