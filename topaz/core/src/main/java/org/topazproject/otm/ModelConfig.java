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
