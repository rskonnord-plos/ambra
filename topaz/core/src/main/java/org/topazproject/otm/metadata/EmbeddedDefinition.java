/* $HeadURL::                                                                            $
 * $Embedded: ClassMetadata.java 4960 2008-03-12 17:13:54Z pradeep $
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.metadata;

/**
 * The definition for a property that embeds an entity.
 *
 * @author Pradeep Krishnan
 */
public class EmbeddedDefinition extends PropertyDefinition {
  private final String embedded;

  /**
   * Creates a new EmbeddedDefinition object.
   *
   * @param name   The name of this definition.
   * @param embedded The entity that is being embedded
   */
  public EmbeddedDefinition(String name, String embedded) {
    super(name);
    this.embedded = embedded;
  }

  /**
   * Gets the embedded entity.
   *
   * @return embedded as String.
   */
  public String getEmbedded() {
    return embedded;
  }
}
