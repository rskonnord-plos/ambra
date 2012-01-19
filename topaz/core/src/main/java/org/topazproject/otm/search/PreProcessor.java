/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2008 by Topaz, Inc.
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

package org.topazproject.otm.search;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.metadata.SearchableDefinition;

/**
 * Defines a pre-processor for preparing values before indexing. A typical
 * example is to strip tags and undesired items from xml or html documents.
 *
 * @author Ronald Tschalär
 */
public interface PreProcessor {
  /**
   * Process the value and return the resulting value to be indexed.
   *
   * @param o     the object involved whose property is to be indexed
   * @param def   the searchable definition for the property
   * @param value the property value to be preprocessed before indexing
   * @return the processed value
   * @throws OtmException if an error occurs while processing the input
   */
  public String process(Object o, SearchableDefinition def, String value) throws OtmException;
}
