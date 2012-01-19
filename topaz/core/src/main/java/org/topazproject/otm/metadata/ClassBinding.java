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
package org.topazproject.otm.metadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.mapping.PropertyBinder;
import org.topazproject.otm.mapping.BinderFactory;
import org.topazproject.otm.mapping.EntityBinder;

/**
 * Holds the binders for a Class and its properties held by a SessionFactory.
 *
 * @author Pradeep Krishnan
 */
public abstract class ClassBinding {
  private final ClassDefinition                             def;
  private final Map<EntityMode, EntityBinder>               entityBinders =
    Collections.synchronizedMap(new HashMap<EntityMode, EntityBinder>());
  private final Map<String, Set<BinderFactory>> propBinders   =
    new HashMap<String, Set<BinderFactory>>();

  /**
   * Creates a new ClassBinding object.
   *
   * @param def   The class definition.
   */
  protected ClassBinding(ClassDefinition def) {
    this.def = def;
  }

  /**
   * Gets the name of the class that this bindings apply to.
   *
   * @return bindings as a String
   */
  public String getName() {
    return def.getName();
  }

  /**
   * Gets the Class definition.
   *
   * @return gets the Class definition that this bindings apply to.
   */
  public ClassDefinition getClassDefinition() {
    return def;
  }

  /**
   * Bind this definition.
   *
   * @param mode the entity mode of the binder
   * @param binder the binder
   *
   * @throws OtmException on a duplicate binding
   */
  public void bind(EntityMode mode, EntityBinder binder)
            throws OtmException {
    EntityBinder b = entityBinders.get(mode);

    if (b != null)
      throw new OtmException("Duplicate binding : <" + getName() + ", " + mode + ">");

    entityBinders.put(mode, binder);
  }

  /**
   * Adds a binder factory for a property.
   *
   * @param factory the binder factory to add
   *
   * @throws OtmException on a duplicate binding
   */
  public void addBinderFactory(BinderFactory factory) throws OtmException {
    final String       prop = factory.getPropertyName();
    final EntityMode   mode = factory.getEntityMode();

    Set<BinderFactory> bfs  = propBinders.get(prop);

    if (bfs == null) {
      bfs = new HashSet<BinderFactory>();
      propBinders.put(prop, bfs);
    }

    for (BinderFactory bf : bfs)
      if (mode.equals(bf.getEntityMode()))
        throw new OtmException("Duplicate binding : <" + prop + ", " + mode + "> in '" + getName()
                               + "'");

    bfs.add(factory);
  }

  /**
   * Gets all the bound properties.
   *
   * @return the set of properties bound to this definition.
   */
  public Set<String> getProperties() {
    return propBinders.keySet();
  }

  /**
   * Gets the binders for this definition.
   *
   * @return map of binders
   */
  public Map<EntityMode, EntityBinder> getBinders() {
    return entityBinders;
  }

  /**
   * Resolves the binders for a property definition.
   *
   * @param prop the property
   * @param sf the session factory
   *
   * @return map of binders for this property
   *
   * @throws OtmException if the property is undefined
   */
  public Map<EntityMode, PropertyBinder> resolveBinders(String prop, SessionFactory sf)
                                         throws OtmException {
    Set<BinderFactory> bfs = propBinders.get(prop);

    if (bfs == null)
      throw new OtmException("No such property '" + prop + "' in '" + getName() + "'");

    Map<EntityMode, PropertyBinder> propertyBinders = new HashMap<EntityMode, PropertyBinder>();

    for (BinderFactory bf : bfs)
      propertyBinders.put(bf.getEntityMode(), bf.createBinder(sf));

    return Collections.synchronizedMap(propertyBinders);
  }
}
