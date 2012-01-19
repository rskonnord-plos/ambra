/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/libs/otm-models/src/main/#$
 * $Id: PLoS.java 3138 2007-07-10 19:10:40Z amit $
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
package org.plos.models;

import java.io.Serializable;
import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;

/**
 * Signature for licenses to be used
 *
 * @author Amit Kapoor
 */
@Entity(type = PLoS.creativeCommons + "License", model = "ri")
public abstract class License implements Serializable {
  @Id
  private URI   id;

  /**
   * Creates a new license object.
   */
  protected License() {
  }

  /**
   * Get id.
   *
   * @return id as URI.
   */
  public URI getId() {
    return id;
  }

  /**
   * Set id.
   *
   * @param id the value to set.
   */
  public void setId(URI id) {
    this.id = id;
  }
}
