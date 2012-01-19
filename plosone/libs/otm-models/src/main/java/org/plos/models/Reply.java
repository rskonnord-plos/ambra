/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * Reply meta-data.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Reply.NS + "Reply")
@UriPrefix(Reply.NS)
public class Reply extends Annotea {
  /**
   * Thread Namespace
   */
  public static final String NS = "http://www.w3.org/2001/03/thread#";
  @Id
  @GeneratedValue(uriPrefix = "info:doi/10.1371/reply/")
  private URI                                                          id;
  private URI                                                          root;
  private URI                                                          inReplyTo;
  @Predicate(uri = Annotea.NS + "body")
  private URI                                                          body;

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

  /**
   * 
  DOCUMENT ME!
   *
   * @return Returns the body.
   */
  public URI getBody() {
    return body;
  }

  /**
   * 
  DOCUMENT ME!
   *
   * @param body The body to set.
   */
  public void setBody(URI body) {
    this.body = body;
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
