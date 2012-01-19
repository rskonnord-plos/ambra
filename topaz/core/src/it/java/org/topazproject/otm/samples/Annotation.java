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

import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;

/**
 * Annotation meta-data.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Annotea.NS + "Annotation")
public abstract class Annotation extends Annotea {
  @Id
  private URI                                                                       id;
  private URI                                                                       annotates;
  private String                                                                    context;
  @Predicate(uri = Rdf.dc_terms + "replaces")
  private Annotation                                                                supersedes;
  @Predicate(uri = Rdf.dc_terms + "isReplacedBy")
  private Annotation                                                                supersededBy;
  @Predicate(uri = Reply.NS + "inReplyTo", inverse = true, notOwned = true, 
             cascade={CascadeType.child})
  private List<ReplyThread>                                                         replies =
    new ArrayList<ReplyThread>();

  /**
   * Creates a new Annotation object.
   */
  public Annotation() {
  }

  /**
   * Creates a new Annotation object.
   */
  public Annotation(URI id) {
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
   * Get annotates.
   *
   * @return annotates as Uri.
   */
  public URI getAnnotates() {
    return annotates;
  }

  /**
   * Set annotates.
   *
   * @param annotates the value to set.
   */
  public void setAnnotates(URI annotates) {
    this.annotates = annotates;
  }

  /**
   * Get context.
   *
   * @return context as String.
   */
  public String getContext() {
    return context;
  }

  /**
   * Set context.
   *
   * @param context the value to set.
   */
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public List<ReplyThread> getReplies() {
    return replies;
  }

  /**
   * DOCUMENT ME!
   *
   * @param replies DOCUMENT ME!
   */
  public void setReplies(List<ReplyThread> replies) {
    this.replies = replies;
  }

  /**
   * DOCUMENT ME!
   *
   * @param r DOCUMENT ME!
   */
  public void addReply(ReplyThread r) {
    r.setRoot(getId());
    r.setInReplyTo(getId());
    replies.add(r);
  }

  /**
   * Get supersedes.
   *
   * @return supersedes.
   */
  public Annotation getSupersedes() {
    return supersedes;
  }

  /**
   * Set supersedes.
   *
   * @param supersedes the value to set.
   */
  public void setSupersedes(Annotation supersedes) {
    this.supersedes = supersedes;
  }

  /**
   * Get supersededBy.
   *
   * @return supersededBy.
   */
  public Annotation getSupersededBy() {
    return supersededBy;
  }

  /**
   * Set supersededBy.
   *
   * @param supersededBy the value to set.
   */
  public void setSupersededBy(Annotation supersededBy) {
    this.supersededBy = supersededBy;
  }
}
