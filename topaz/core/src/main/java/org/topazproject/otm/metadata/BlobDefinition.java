/* $HeadURL::                                                                            $
 * $Blob: ClassMetadata.java 4960 2008-03-12 17:13:54Z pradeep $
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.metadata;

/**
 * The definition for a Blob field.
 *
 * @author Pradeep Krishnan
 */
public class BlobDefinition extends PropertyDefinition {
  /**
   * Creates a new BlobDefinition object.
   *
   * @param name   The name of this definition.
   */
  public BlobDefinition(String name) {
    super(name);
  }
}
