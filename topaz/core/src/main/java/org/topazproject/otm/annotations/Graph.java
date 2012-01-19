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
package org.topazproject.otm.annotations;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;

/**
 * An annotation for configuring a single graph within the triple store
 *
 * @author Amit Kapoor
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Graph {
  /** Id for the graph */
  String id();

  /** the URI identifying the graph. */
  String uri();

  /**
   * The URI identifying the type of the graph. NOTE: This returns an empty string if not specified
   * and in that case should be treated as 'null'.
   */
  String type() default "";
}
