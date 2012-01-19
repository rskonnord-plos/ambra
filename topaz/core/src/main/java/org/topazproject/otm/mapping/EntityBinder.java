/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.mapping;

import javassist.util.proxy.ProxyObject;

import org.topazproject.otm.OtmException;

/**
 * Binds an entity to an {@link org.topazproject.otm.EntityMode} specific implementation. 
 *
 * @author Pradeep Krishnan
 */
public interface EntityBinder {
  /**
   * Constructs a new instance using a no-argument constructor. TODO: support constructor
   * arguments
   *
   * @return the newly created instance
   *
   * @throws OtmException on an error
   */
  public Object newInstance() throws OtmException;

  /**
   * Constructs a proxy instance using a no-argument constructor. TODO: support constructor
   * arguments
   *
   * @return the newly created instance
   *
   * @throws OtmException on an error
   */
  public ProxyObject newProxyInstance() throws OtmException;

  /**
   * Tests if this entity is instantiable and not an abstract super-class.
   *
   * @return true if a new instance of this entity can be constructed
   */
  public boolean isInstantiable();

  /**
   * Tests if the given object is an instance of this entity. Sub-class instances
   * also qualify as instances of this entity.
   *
   * @param o the object to test
   *
   * @return true if the object is an instance of this entity.
   */
  public boolean isInstance(Object o);

  /**
   * Tests if the instances of this binder are assignment compatible with instances of another.
   *
   * @param other the other binder to test
   *
   * @return true if instances of this binder are assignable from instances of the other
   */
  public boolean isAssignableFrom(EntityBinder other);

  /**
   * Gets all unique alias names by which this entity is known. For java classes
   * this is the class name.
   *
   * @return the alternate unique names for this entity published by this Binder.
   */
  public String[] getNames();
}
