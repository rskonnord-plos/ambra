/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm;

import java.net.URI;

/**
 * Configuration for a Model/Graph in the triple-store
 *
 * @author Pradeep Krishnan
 */
public class ModelConfig {
  private String id;
  private URI    uri;
  private URI    type;

  /**
   * Creates a new Model object.
   *
   * @param id the id of this model
   * @param uri the model uri
   * @param type the model type
   */
  public ModelConfig(String id, URI uri, URI type) {
    this.id     = id;
    this.uri    = uri;
    this.type   = type;
  }

  /**
   * Get id.
   *
   * @return id as String
   */
  public String getId() {
    return id;
  }

  /**
   * Get uri.
   *
   * @return uri as URI.
   */
  public URI getUri() {
    return uri;
  }

  /**
   * Get type.
   *
   * @return type as String.
   */
  public URI getType() {
    return type;
  }
}
