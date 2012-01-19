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
 * When de-serializing an Integer that is stored as some non-integer, be sure to remove the
 * decimal point.
 *
 * @author Eric Brown
 *
 * @param <T> the java type of the class
 */
public class IntegerSerializer<T> extends SimpleSerializer<T> {
  /**
   * Creates a new IntegerSerializer object.
   *
   * @param clazz the class to serialize/deserialize
   */
  public IntegerSerializer(Class<T> clazz) {
    super(clazz);
  }

  /*
   * inherited javadoc
   */
  public T deserialize(String o, Class<T> c) throws Exception {
    int decimal = o.indexOf(".");

    if (decimal != -1)
      o = o.substring(0, decimal); // TODO: Round-off properly?

    return super.deserialize(o, c);
  }
}
