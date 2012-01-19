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
 * A class definition that references another class definition.
 *
 * @author Pradeep Krishnan
 */
public class ClassReferenceDefinition extends ClassDefinition implements Reference {
  private final String ref;

  /**
   * Creates a ClassReferenceDefinition object.
   *
   * @param name   The name of this definition.
   * @param ref    The name that is being referenced.
   */
  public ClassReferenceDefinition(String name, String ref) {
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

  /*
   * inherited javadoc
   */
  protected ClassMetadata buildClassMetadata(SessionFactory sf, ClassDefinition referee)
                                      throws OtmException {
    Definition def = sf.getDefinition(ref);

    if (def instanceof ClassDefinition)
      return ((ClassDefinition) def).buildClassMetadata(sf, referee);

    throw new OtmException("No such definition: " + ref + " referenced from " + getName());
  }
}
