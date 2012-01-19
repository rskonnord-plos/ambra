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

import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.annotations.Rdf;

/**
 * Reply meta-data.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Reply.NS + "Reply")
@UriPrefix(Reply.NS)
public class Reply extends Annotea {
  public static final String NS = "http://www.w3.org/2001/03/thread#";
  private URI  root;
  private URI  inReplyTo;

  /**
   * Creates a new Reply object.
   */
  public Reply() {
  }

  public Reply(URI id) {
    super(id);
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
