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
