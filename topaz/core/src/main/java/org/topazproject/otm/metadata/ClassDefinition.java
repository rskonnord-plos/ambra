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

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;

/**
 * The base class for all class meta definitions.
 *
 * @author Pradeep Krishnan
 */
public abstract class ClassDefinition extends Definition {
  /**
   * Creates a new ClassDefinition object.
   *
   * @param name   The name of this definition.
   */
  public ClassDefinition(String name) {
    super(name);
  }

  /**
   * Builds a new class metadata instance for this definition.
   *
   * @param sf the session factory instance
   *
   * @return the newly created class-meta object
   *
   * @throws OtmException on an error
   */
  public abstract ClassMetadata buildClassMetadata(SessionFactory sf)
                                   throws OtmException;
}
