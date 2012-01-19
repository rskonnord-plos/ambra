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

import java.util.Map;

import org.topazproject.otm.EntityMode;
import org.topazproject.otm.Session;

/**
 * A convenient base class for all mappers.
 *
 * @author Pradeep krishnan
 */
public abstract class AbstractMapper implements Mapper {
  private final Map<EntityMode, Binder> binders;

  /**
   * Creates a new AbstractMapper object.
   *
   * @param binders the binders for this property
   */
  public AbstractMapper(Map<EntityMode, Binder> binders) {
    this.binders = binders;
  }

  /*
   * inherited javadoc
   */
  public Binder getBinder(Session session) {
    return getBinder(session.getEntityMode());
  }

  /*
   * inherited javadoc
   */
  public Binder getBinder(EntityMode mode) {
    return binders.get(mode);
  }

  /*
   * inherited javadoc
   */
  public String getName() {
    return getDefinition().getNamespaceSpecific();
  }

  /*
   * inherited javadoc
   */
  public Map<EntityMode, Binder> getBinders() {
    return binders;
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return getClass().getName() + "[def=" + getDefinition() + ", binders =" + binders + "]";
  }
}
