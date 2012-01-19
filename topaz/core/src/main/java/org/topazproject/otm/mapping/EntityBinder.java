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
