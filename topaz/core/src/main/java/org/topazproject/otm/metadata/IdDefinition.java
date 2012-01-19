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

import org.topazproject.otm.id.IdentifierGenerator;

/**
 * The definition for an Id field.
 *
 * @author Pradeep Krishnan
 */
public class IdDefinition extends PropertyDefinition {
  private final IdentifierGenerator gen;

  /**
   * Creates a new IdDefinition object.
   *
   * @param name   The name of this definition.
   * @param gen id generator or null
   */
  public IdDefinition(String name, IdentifierGenerator gen) {
    super(name);
    this.gen = gen;
  }

  /**
   * Gets the generator for this field
   *
   * @return the generator to use for this field (or null if there isn't one)
   */
  public IdentifierGenerator getGenerator() {
    return gen;
  }
}
