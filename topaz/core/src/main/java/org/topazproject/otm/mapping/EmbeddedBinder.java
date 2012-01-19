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

/**
 * Binder that binds an embedded entity.
 *
 * @author Pradeep Krishnan
 */
public interface EmbeddedBinder extends Binder {
  /**
   * Promote an embedded field binder up to the same level as this so that it can be added to
   * collections that contain mappers at the same level as this.
   *
   * @param b the binder to promote
   *
   * @return the promoted binder
   */
  public Binder promote(Binder b);
}
