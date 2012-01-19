/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.metadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.mapping.Binder;
import org.topazproject.otm.mapping.EntityBinder;

/**
 * Holds the binders for a Class and its properties held by a SessionFactory.
 *
 * @author Pradeep Krishnan
 */
public abstract class ClassBindings {
  private final ClassDefinition                      def;
  private final Map<EntityMode, EntityBinder>        entityBinders =
    Collections.synchronizedMap(new HashMap<EntityMode, EntityBinder>());
  private final Map<String, Map<EntityMode, Binder>> propBinders   =
    new HashMap<String, Map<EntityMode, Binder>>();

  /**
   * Creates a new ClassBindings object.
   *
   * @param def   The class definition.
   */
  protected ClassBindings(ClassDefinition def) {
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
   * Adds and binds a property to this class definition.
   *
   * @param prop the property to bind
   * @param mode the mode of the binder
   * @param binder the binder
   *
   * @throws OtmException on a duplicate binding
   */
  public void addAndBindProperty(String prop, EntityMode mode, Binder binder)
                          throws OtmException {
    Map<EntityMode, Binder> binders = propBinders.get(prop);

    if (binders == null)
      propBinders.put(prop, binders = Collections.synchronizedMap(new HashMap<EntityMode, Binder>()));

    Binder b = binders.get(mode);

    if (b != null)
      throw new OtmException("Duplicate binding : <" + prop + ", " + mode + "> in '" + getName()
                             + "'");

    binders.put(mode, binder);
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
   * Gets the binders for a property definition.
   *
   * @param prop the property
   *
   * @return map of binders for this property
   *
   * @throws OtmException if the property is undefined
   */
  public Map<EntityMode, Binder> getBinders(String prop)
                                     throws OtmException {
    Map<EntityMode, Binder> binders = propBinders.get(prop);

    if (binders == null)
      throw new OtmException("No such property '" + prop + "' in '" + getName() + "'");

    return binders;
  }
}
