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
