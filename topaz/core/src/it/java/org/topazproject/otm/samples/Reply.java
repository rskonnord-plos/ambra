/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.topazproject.otm.samples;

import java.net.URI;

import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * Reply meta-data.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Reply.NS + "Reply")
@UriPrefix(Reply.NS)
public class Reply extends Annotea {
  public static final String NS = "http://www.w3.org/2001/03/thread#";
  @Id
  private URI  id;
  private URI  root;
  private URI  inReplyTo;

  /**
   * Creates a new Reply object.
   */
  public Reply() {
  }

  public Reply(URI id) {
    this.id = id;
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

  /**
   * Get root.
   *
   * @return root as URI.
   */
  public URI getRoot() {
    return root;
  }

  /**
   * Set root.
   *
   * @param root the value to set.
   */
  public void setRoot(URI root) {
    this.root = root;
  }

  /**
   * Get inReplyTo.
   *
   * @return inReplyTo as URI.
   */
  public URI getInReplyTo() {
    return inReplyTo;
  }

  /**
   * Set inReplyTo.
   *
   * @param inReplyTo the value to set.
   */
  public void setInReplyTo(URI inReplyTo) {
    this.inReplyTo = inReplyTo;
  }
}
