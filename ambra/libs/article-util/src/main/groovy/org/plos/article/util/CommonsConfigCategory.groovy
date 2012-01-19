/* $HeadURL::                                                                                    $
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

package org.plos.article.util

import org.apache.commons.configuration.Configuration

/**
 * A category to allow easier traversal of commons-configuration objects. Specifically, it add
 * a support for the '.' operator. If multiple items exist for a given member, a list of
 * Configuration is returned; if only one exists a Configuration is returned. Also, use '@foo'
 * to access attribute 'foo'. Example:
 * <pre>
 *   config.ambra.services.foo.'@bar'
 * </pre>
 *
 * @author Ronald Tschalär
 */
class CommonsConfigCategory {
  static Object get(Configuration config, String key) {
    if (key.startsWith('@') || config.containsKey(key))
      return config.getString(key.startsWith('@') ? "[${key}]" : key)

    List res = []
    for (int idx = 0; !config.subset("${key}(${idx})").isEmpty(); idx++)
      res.add(config.subset("${key}(${idx})"))

    return res.size() == 1 ? res[0] : res
  }

  static Object get(List<Configuration> configList, String key) {
    List res = configList.collect{ get(it, key) }.flatten()
    return res.size() == 1 ? res[0] : res
  }
}
