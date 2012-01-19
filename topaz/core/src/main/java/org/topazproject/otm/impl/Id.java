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
package org.topazproject.otm.impl;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;

/**
 * A unique identifier for an Entity.
 *
 * @author Pradeep Krishnan
  */
class Id {
  private final String         id;
  private final ClassMetadata  cm;
  private final EntityMode     mode;

  /**
   * Creates a new Id object.
   *
   * @param cm entity class
   * @param id entity id
   * @param mode the entity mode
   */
  public Id(ClassMetadata cm, String id, EntityMode mode) throws NullPointerException {
    this.id   = id;
    this.cm   = cm;
    this.mode = mode;
    if (id == null)
      throw new NullPointerException("id cannot be null");
    if (cm == null)
      throw new NullPointerException("cm cannot be null");
  }

  /**
   * Gets the entity id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the ClassMetadata of the entity. Note that this
   * only gives the ClassMetadata known at the time of
   * the Id creation. The actual object instance may be
   * an instance of a sub-class of this.
   *
   * @return the entity Class
   */
  public ClassMetadata getClassMetadata() {
    return cm;
  }

  /*
   * inherited javadoc
   */
  public int hashCode() {
    return id.hashCode();
  }

  /*
   * inherited javadoc
   */
  public boolean equals(Object other) {
    if (!(other instanceof Id))
      return false;

    Id o = (Id) other;

    return id.equals(o.id) && (cm.isAssignableFrom(o.cm, mode) || o.cm.isAssignableFrom(cm, mode));
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return id;
  }
}
