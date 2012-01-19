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
package org.topazproject.otm.serializer;

/**
 * A serializer for xsd:boolean type.
 *
 * @author Pradeep Krishnan
 */
public class XsdBooleanSerializer implements Serializer<Boolean> {
  /*
   * inherited javadoc
   */
  public String serialize(Boolean o) throws Exception {
    return o.toString();
  }

  /*
   * inherited javadoc
   */
  public Boolean deserialize(String o, Class c) throws Exception {
    if ("1".equals(o) || "true".equals(o))
      return Boolean.TRUE;

    if ("0".equals(o) || "false".equals(o))
      return Boolean.FALSE;

    throw new IllegalArgumentException("invalid xsd:boolean '" + o + "'");
  }
}
