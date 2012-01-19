/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.impl;

import org.topazproject.otm.ClassMetadata;

/**
 * A unique identifier for an Entity.
 *
 * @author Pradeep Krishnan
  */
class Id {
  private final String         id;
  private final ClassMetadata  cm;

  /**
   * Creates a new Id object.
   *
   * @param cm entity class
   * @param id entity id
   */
  public Id(ClassMetadata cm, String id) throws NullPointerException {
    this.id      = id;
    this.cm   = cm;
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

    return id.equals(o.id) && (cm.isAssignableFrom(o.cm) || o.cm.isAssignableFrom(cm));
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return id;
  }
}
