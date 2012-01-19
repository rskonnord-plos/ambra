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

import java.util.Date;

/**
 * A type converter to/from a Date data type.
 *
 * @author Pradeep Krishnan
 */
public interface DateBuilder<T> {
  /**
   * Convert the given type to a Date object.
   *
   * @param o the object to convert
   *
   * @return the converted date object
   */
  public Date toDate(T o);

  /**
   * Convert a Date object to the given type.
   *
   * @param d the date object
   *
   * @return the converted type
   */
  public T fromDate(Date d);
}
