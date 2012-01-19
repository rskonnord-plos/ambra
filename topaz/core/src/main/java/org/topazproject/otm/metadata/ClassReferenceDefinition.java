/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
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
