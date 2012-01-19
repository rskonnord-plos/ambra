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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.topazproject.otm.search.PreProcessor;

/**
 * Annotation for properties to specify that they should be inserted into lucene.
 *
 * @author Ronald Tschalär
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Searchable {
  /**
   * References another searchable configuration defined elsewhere.
   *
   * @see Predicate#ref
   */
  String ref() default "";

  /**
   * Predicate uri. This is only used when using a store that stores the search indexes
   * in a graph (e.g. using ItqlStore with Mulgara and the LuceneResolver). It defaults
   * to the uri of the {@link Predicate} definition for the property this annotation is
   * defined for, if there is one.
   */
  String uri() default "";

  /**
   * The search index to use. In the case where the search indexes are stored in a graph,
   * the is the name of the graph.
   */
  String index();

  /** Whether the value should be tokenized or not */
  boolean tokenize() default true;

  /** The analyzer to use (only valid if {@link #tokenize} is true) */
  String analyzer() default "";

  /** The boost to assign to this field */
  int boost() default Integer.MAX_VALUE;

  /** The pre-processor to use; by default the value is indexed as is. */
  Class<? extends PreProcessor> preProcessor() default PreProcessor.class;
}
