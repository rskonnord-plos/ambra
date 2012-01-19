/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service;

import java.net.URI;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.SimpleTimeZone;

import org.plos.models.Reply;

/**
 * The Reply meta-data.
 *
 * @author Pradeep Krishnan
 */
public class ReplyInfo {
  private Reply reply;
  private FedoraHelper fedora;
  private static SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

  static {
    fmt.setTimeZone(new SimpleTimeZone(0, "UTC"));
  }
  /**
   * Creates a new ReplyInfo object.
   */
  public ReplyInfo(Reply reply, FedoraHelper fedora) {
    this.reply      = reply;
    this.fedora   = fedora;
  }
  /**
   * Get id.
   *
   * @return id as String.
   */
  public String getId() {
    return reply.getId().toString();
  }

  /**
   * Get type.
   *
   * @return type as String.
   */
  public String getType() {
    return reply.getType();
  }

  /**
   * Get root.
   *
   * @return root as String.
   */
  public String getRoot() {
    URI u = reply.getRoot();
    return (u == null) ? null : u.toString();
  }

  /**
   * Get inReplyTo.
   *
   * @return inReplyTo as String.
   */
  public String getInReplyTo() {
    URI u = reply.getInReplyTo();
    return (u == null) ? null : u.toString();
  }

  /**
   * Get title.
   *
   * @return title as String.
   */
  public String getTitle() {
    return reply.getTitle();
  }

  /**
   * Get creator.
   *
   * @return creator as String.
   */
  public String getCreator() {
    return reply.getCreator();
  }

  /**
   * Get created.
   *
   * @return created as String.
   */
  public String getCreated() {
    Date d = reply.getCreated();

    synchronized (fmt) {
      return (d == null) ? null : fmt.format(d);
    }
  }

  /**
   * Get body.
   *
   * @return body as String.
   */
  public String getBody() {
    URI u = reply.getBody();

    return (u == null) ? null : fedora.getBodyURL(u.toString());
  }

  /**
   * Get mediator.
   *
   * @return mediator as String.
   */
  public String getMediator() {
    return reply.getMediator();
  }

  /**
   * Get state.
   *
   * @return state as int.
   */
  public int getState() {
    return reply.getState();
  }

}
