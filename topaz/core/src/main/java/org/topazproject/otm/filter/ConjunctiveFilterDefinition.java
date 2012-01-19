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
 * This represents a filter-definition that is the result of a conjunction ('and') of other
 * filter-definitions. I.e. the resulting filter is the result of logically and'ing the filters
 * from the component filter-definitions.
 *
 * @author Ronald Tschalär
 */
public class ConjunctiveFilterDefinition extends JunctionFilterDefinition {
  /**
   * Create a new conjunctive-filter-definition.
   *
   * @param filterName    the name of the filter
   * @param filteredClass the class being filtered; either an alias or a fully-qualified class name
   */
  public ConjunctiveFilterDefinition(String filterName, String filteredClass) {
    super(filterName, filteredClass);
  }

  public ConjunctiveFilter createFilter(Session sess) throws OtmException {
    return new ConjunctiveFilter(this, sess);
  }

  public static class ConjunctiveFilter extends JunctionFilter {
    private ConjunctiveFilter(JunctionFilterDefinition jfd, Session sess) throws OtmException {
      super(jfd, sess);
    }
  }
}
