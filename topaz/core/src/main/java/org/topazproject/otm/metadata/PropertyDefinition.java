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
 * The base class for all property definitions.
 *
 * @author Pradeep Krishnan
 */
public class PropertyDefinition extends Definition {
  /**
   * Creates a new PropertyDefinition object.
   *
   * @param name   The name of this definition.
   */
  public PropertyDefinition(String name) {
    super(name);
  }
}
