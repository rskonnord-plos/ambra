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
package org.plos.models;

import java.net.URI;

import org.topazproject.otm.FetchType;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Predicate.PropType;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * Reply meta-data.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Reply.RDF_TYPE)
@UriPrefix(Reply.NS)
public class Reply extends Annotea {
  public static final String RDF_TYPE = Reply.NS + "Reply";
  /**
   * Thread Namespace
   */
  public static final String NS = "http://www.w3.org/2001/03/thread#";
  @Id
  @GeneratedValue(uriPrefix = "id:reply/")
  private URI                                                           id;
  @Predicate(type=PropType.OBJECT)
  private String                                                       root;
  @Predicate(type=PropType.OBJECT)
  private String                                                       inReplyTo;
  @Predicate(uri = Annotea.W3C_NS + "body", fetch = FetchType.eager) // XXX: lazy?
  private ReplyBlob                                                    body;

/**
   * Creates a new Reply object.
   */
  public Reply() {
  }

/**
   * Creates a new Reply object.
   *
   * @param id the reply id
   */
  public Reply(URI id) {
    this.id = id;
  }

  /**
   * Get root.
   *
   * @return root as String.
   */
  public String getRoot() {
    return root;
  }

  /**
   * Set root.
   *
   * @param root the value to set.
   */
  public void setRoot(String root) {
    this.root = root;
  }

  /**
   * Get inReplyTo.
   *
   * @return inReplyTo as URI.
   */
  public String getInReplyTo() {
    return inReplyTo;
  }

  /**
   * Set inReplyTo.
   *
   * @param inReplyTo the value to set.
   */
  public void setInReplyTo(String inReplyTo) {
    this.inReplyTo = inReplyTo;
  }

  /**
   * 
  DOCUMENT ME!
   *
   * @return Returns the body.
   */
  public ReplyBlob getBody() {
    return body;
  }

  /**
   * 
  DOCUMENT ME!
   *
   * @param body The body to set.
   */
  public void setBody(ReplyBlob body) {
    this.body = body;
  }

  /**
   * Get id.
   *
   * @return id as URI
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

  public String getType() {
    return RDF_TYPE;
  }

}
