/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.samples;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Inverse;
import org.topazproject.otm.annotations.Model;
import org.topazproject.otm.annotations.Rdf;

/**
 * Annotation meta-data.
 *
 * @author Pradeep Krishnan
 */
@Rdf(Annotia.NS + "Annotation")
public abstract class Annotation extends Annotia {
  private URI                                                  annotates;
  private String                                               context;
  @Rdf(Rdf.dc_terms + "replaces")
  private Annotation                                           supersedes;
  @Rdf(Rdf.dc_terms + "isReplacedBy")
  private Annotation                                           supersededBy;
  @Inverse
  @Rdf(Reply.NS + "inReplyTo")
  private List<ReplyThread>                                    replies =
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
    super(id);
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
