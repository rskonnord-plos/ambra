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

package org.topazproject.otm;

import java.util.EnumSet;

/**
 * Enum defining how operations should cascade to associations.
 *
 * @author Pradeep Krishnan
 */
public enum CascadeType {
  /**
   * CascadeType value is undefined.
   */
  undefined,
  /**
   * Cascades the {@link org.topazproject.otm.Session#saveOrUpdate} to this
   * association.
   */
  saveOrUpdate,
  /**
   * Cascades the {@link org.topazproject.otm.Session#merge} to this
   * association.
   */
  merge,
  /**
   * Cascades the {@link org.topazproject.otm.Session#refresh} to this
   * association.
   */
  refresh,
  /**
   * Cascades the {@link org.topazproject.otm.Session#evict} to this
   * association.
   */
  evict,
  /**
   * Cascades the {@link org.topazproject.otm.Session#delete} to this
   * association.
   */
  delete,
  /**
   * This cascade option specifies that when an associated object becomes
   * dis-associated with this instance, it should be considered an orphan and must
   * be deleted. This can happen when an assignment or
   * {@link org.topazproject.otm.Session#merge} replaces the associated instance
   * with another.
   */
  deleteOrphan,
  /**
   * A convenience grouping of cascade operations when the assocition represents a
   * 'peer'. This is the set of {@link #saveOrUpdate}, {@link #merge}, {@link #refresh},
   * and {@link #evict}.
   */
  peer {public boolean implies(CascadeType e){
    for (CascadeType c: EnumSet.range(CascadeType.saveOrUpdate, CascadeType.evict))
      if (c.implies(e))
        return true;
    return e.equals(this);
  }},
  /**
   * A convenience grouping of cascade operations when the assocition represents a
   * 'child'. This is the set of {@link #peer}, {@link #delete} and {@link #deleteOrphan}.
   */
  child {public boolean implies(CascadeType e){
    for (CascadeType c: EnumSet.range(CascadeType.delete, CascadeType.peer))
      if (c.implies(e))
        return true;
    return e.equals(this);
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
