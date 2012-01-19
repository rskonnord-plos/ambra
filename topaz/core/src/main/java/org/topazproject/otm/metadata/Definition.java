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
package org.topazproject.otm.metadata;

/**
 * The base class for all meta definitions. Definitions all have a name that may optionally
 * contain a namespace portion followed by a ':' and a namespace specific portion. Class names
 * will usually have no namespace defined and are expected to be globally unique. Property names
 * however will usually have  a namespace to disambiguate.
 *
 * @author Pradeep Krishnan
 */
public class Definition {
  private final String name;
  private final String ns;
  private final String specific;

  /**
   * Creates a new Definition object.
   *
   * @param name   The unique name of this definition.
   */
  public Definition(String name) {
    this.name = name;

    String[] sp = name.split(":", 2);
    specific   = sp[sp.length - 1];
    ns         = (sp.length == 1) ? null : sp[0];
  }

  /**
   * Gets the unique name of this definition.
   *
   * @return name as String.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the namespace for this definition.
   *
   * @return name space or null for global
   */
  public String getNamespace() {
    return ns;
  }

  /**
   * Gets the namespace specific name for this definition.
   *
   * @return the specific name
   */
  public String getNamespaceSpecific() {
    return specific;
  }
}
