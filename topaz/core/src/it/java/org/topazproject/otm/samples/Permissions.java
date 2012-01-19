/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.topazproject.otm.samples;

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
