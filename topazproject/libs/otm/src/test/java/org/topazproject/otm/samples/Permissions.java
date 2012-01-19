/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.samples;

import java.net.URI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.PredicateMap;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public class Permissions {
  /**
   * DOCUMENT ME!
   */
  @Id
  public String resource;

  /**
   * DOCUMENT ME!
   */
  @PredicateMap
  public Map<String, List<String>> permissions = new HashMap<String, List<String>>();
}
