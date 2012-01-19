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

package org.topazproject.otm.filter;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;

/**
 * This represents a filter-definition that is the result of a disjunction ('or') of other
 * filter-definitions. I.e. the resulting filter is the result of logically or'ing the filters
 * from the component filter-definitions.
 *
 * @author Ronald Tschalär
 */
public class DisjunctiveFilterDefinition extends JunctionFilterDefinition {
  /** 
   * Create a new disjunctive-filter-definition. 
   * 
   * @param filterName    the name of the filter
   * @param filteredClass the class being filtered; either an alias or a fully-qualified class name
   */
  public DisjunctiveFilterDefinition(String filterName, String filteredClass) {
    super(filterName, filteredClass);
  }

  public DisjunctiveFilter createFilter(Session sess) throws OtmException {
    return new DisjunctiveFilter(this, sess);
  }

  public static class DisjunctiveFilter extends JunctionFilter {
    private DisjunctiveFilter(JunctionFilterDefinition jfd, Session sess) throws OtmException {
      super(jfd, sess);
    }
  }
}
