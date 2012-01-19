/* $HeadURL::                                                                            $
 * $Embedded: ClassMetadata.java 4960 2008-03-12 17:13:54Z pradeep $
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
package org.topazproject.otm.metadata;

/**
 * The definition for a property that embeds an entity.
 *
 * @author Pradeep Krishnan
 */
public class EmbeddedDefinition extends PropertyDefinition {
  private final String embedded;

  /**
   * Creates a new EmbeddedDefinition object.
   *
   * @param name   The name of this definition.
   * @param embedded The entity that is being embedded
   */
  public EmbeddedDefinition(String name, String embedded) {
    super(name);
    this.embedded = embedded;
  }

  /**
   * Gets the embedded entity.
   *
   * @return embedded as String.
   */
  public String getEmbedded() {
    return embedded;
  }
}
