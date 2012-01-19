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
 * A propert definition that references another property definition.
 *
 * @author Pradeep Krishnan
 */
public class PropertyReferenceDefinition extends PropertyDefinition implements Reference {
  private final String ref;

  /**
   * Creates a PropertyReferenceDefinition object.
   *
   * @param name   The name of this definition.
   * @param ref    The name that is being referenced.
   */
  public PropertyReferenceDefinition(String name, String ref) {
    super(name);
    this.ref = ref;
  }

  /**
   * Gets the referred definition.
   *
   * @return the referred definition
   */
  public String getReferred() {
    return ref;
  }
}
