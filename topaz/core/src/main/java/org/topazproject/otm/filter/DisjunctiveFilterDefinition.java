/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
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
