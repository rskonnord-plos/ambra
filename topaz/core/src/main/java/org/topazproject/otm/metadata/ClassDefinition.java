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
  public ClassMetadata buildClassMetadata(SessionFactory sf)
                                   throws OtmException {
    return buildClassMetadata(sf, this);
  }

  /**
   * Build the ClassMetadata for the referee. Bindings of the referee will be used along with
   * the definitions from here to build the metadata instance;
   *
   * @param sf the session factory
   * @param ref the referee
   *
   * @return the newly created class-meta object
   *
   * @throws OtmException on an error
   */
  protected abstract ClassMetadata buildClassMetadata(SessionFactory sf, ClassDefinition ref)
                                               throws OtmException;
}
