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
 * Configuration for a Graph in the triple-store
 *
 * @author Pradeep Krishnan
 */
public class GraphConfig {
  private String id;
  private URI    uri;
  private URI    type;

  /**
   * Creates a new default type of Graph object.
   * The <value>uri</value> parameter should be the same as the TripleStore URL,
   * with "#graphname" appended.
   *
   * @param id the id of this graph. Use this to refer to the graph in OTM.
   * @param uri the graph uri.
   */
  public GraphConfig(String id, URI uri) {
    this.id     = id;
    this.uri    = uri;
    this.type   = null;
  }

  /**
   * Creates a new Graph object.
   * The <value>uri</value> parameter should be the same as the TripleStore URL,
   * with "#graphname" appended.
   *
   * @param id the id of this graph. Use this to refer to the graph in OTM.
   * @param uri the graph uri.
   * @param type the graph type. <code>null</code> for the default graph type.
   */
  public GraphConfig(String id, URI uri, URI type) {
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

  public String toString() {
    return "GraphConfig[name=" + getId() + ", uri=" + getUri() + ", type=" + getType() + "]";
  }
}
