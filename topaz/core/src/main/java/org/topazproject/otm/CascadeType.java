/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm;

import java.util.EnumSet;

/**
 * Enum defining how operations should cascade to associations.
 *
 * @author Pradeep Krishnan
 */
public enum CascadeType {
  saveOrUpdate,
  delete,
  merge,
  refresh,
  evict,
  all {public boolean implies(CascadeType e){
    for (CascadeType c: EnumSet.range(CascadeType.saveOrUpdate, CascadeType.evict))
      if (c.implies(e))
        return true;
    return e.equals(this);
  }},
  deleteOrphan {public boolean implies(CascadeType e){
    return e.equals(this) || CascadeType.delete.implies(e);
  }};

  /**
   * Tests if the given CascadeType is implied by this.
   *
   * @param e the CascadeType to test
   *
   * @return true if this implies <var>e</var>
   */
  public boolean implies(CascadeType e) {
    return e.equals(this);
  }
}
