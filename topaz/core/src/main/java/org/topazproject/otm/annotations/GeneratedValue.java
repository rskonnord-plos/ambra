/* $HeadURL::                                                                                    $
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

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation to support generated values.
 *
 * @author Eric Brown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface GeneratedValue {
  /**
   * The name of the generator class to use. The class must implement
   * {@link org.topazproject.otm.id.IdentifierGenerator} and must have
   * a no-argument Constructor and must be loadable by the Thread-Context
   * ClassLoader. One instance of this class is created per declaration
   * of this annotation and therefore the
   * {@link org.topazproject.otm.id.IdentifierGenerator#generate}
   * must be multi-thread safe.
   */
  String generatorClass() default "org.topazproject.otm.id.GUIDGenerator";

  /**
   * The prefix of the uri we should generate. If not set, it will be computed.
   */
  String uriPrefix() default "";
}
