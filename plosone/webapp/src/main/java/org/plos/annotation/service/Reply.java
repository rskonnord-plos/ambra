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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.ApplicationException;

import org.plos.user.service.UserService;
import org.plos.user.PlosOneUser;

import org.plos.util.DateParser;
import org.plos.util.InvalidDateException;

import org.topazproject.ws.annotation.ReplyInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Plosone wrapper around the ReplyInfo from topaz service. It provides
 * - A way to escape title/body text when returning the result to the web layer
 * - a separation from any topaz changes
 */
public abstract class Reply extends BaseAnnotation {
  private final ReplyInfo reply;
  private Collection<Reply> replies = new ArrayList<Reply>();
  private String creatorName;
  private UserService userService;  

  private static final Log log = LogFactory.getLog(Reply.class);

  
  public Reply(final ReplyInfo reply) {
    this.reply = reply;
  }
 
  
  /**
   * Constructor that takes in a UserService object in addition to ReplyInfo in order
   * to retrieve the username.
   * 
   * @param reply
   * @param userSvc
   */
  public Reply(final ReplyInfo reply, UserService userSvc) {
    this.reply = reply;
    this.userService = userSvc;
  }
 
  
  /**
   * Get created date.
   * @return created as java.util.Date.
   */
  public Date getCreatedAsDate() {
    try {
      return DateParser.parse(reply.getCreated());
    } catch (InvalidDateException ide) {
    }
    return null;
  }
  
  /**
   * Get created.
   *
   * @return created as String.
   */
  public String getCreated() {
    return reply.getCreated();
  }

  /**
   * Set created.
   *
   * @param created the value to set.
   */
  public void setCreated(final String created) {
    reply.setCreated(created);
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
   * Set creator.
   *
   * @param creator the value to set.
   */
  public void setCreator(final String creator) {
    reply.setCreator(creator);
  }

  /**
   * Get id.
   *
   * @return id as String.
   */
  public String getId() {
    return reply.getId();
  }

  /**
   * Set id.
   *
   * @param id the value to set.
   */
  public void setId(final String id) {
    reply.setId(id);
  }

  /**
   * Get inReplyTo.
   *
   * @return inReplyTo as String.
   */
  public String getInReplyTo() {
    return reply.getInReplyTo();
  }

  /**
   * Set inReplyTo.
   *
   * @param inReplyTo the value to set.
   */
  public void setInReplyTo(final String inReplyTo) {
    reply.setInReplyTo(inReplyTo);
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
   * Set mediator.
   *
   * @param mediator the value to set.
   */
  public void setMediator(final String mediator) {
    reply.setMediator(mediator);
  }

  /**
   * Get root.
   *
   * @return root as String.
   */
  public String getRoot() {
    return reply.getRoot();
  }

  /**
   * Set root.
   *
   * @param root the value to set.
   */
  public void setRoot(final String root) {
    reply.setRoot(root);
  }

  /**
   * Get state.
   *
   * @return state as int.
   */
  public int getState() {
    return reply.getState();
  }

  /**
   * Set state.
   *
   * @param state the value to set.
   */
  public void setState(final int state) {
    reply.setState(state);
  }

  /**
   * Get title.
   *
   * @return title as String.
   */
  public String getCommentTitle() {
    return escapeText(reply.getTitle());
  }

  /**
   * Set title.
   *
   * @param title the value to set.
   */
  public void setCommentTitle(final String title) {
    reply.setTitle(title);
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
   * Set type.
   *
   * @param type the value to set.
   */
  public void setType(final String type) {
    reply.setType(type);
  }

  /**
   * Add a (child) reply to this reply
   * @param reply reply
   */
  public void addReply(final Reply reply) {
    replies.add(reply);
  }

  /**
   * @return the replies to this reply
   */
  public Reply[] getReplies() {
    return replies.toArray(new Reply[replies.size()]);
  }

  /** see BaseAnnotation#isPublic */
  public boolean isPublic() {
    throw new UnsupportedOperationException("A reply is always public, so please don't call this");
  }

  /**
   * @return Returns the creatorName.
   */
  public String getCreatorName() {
    if (creatorName == null) {
      try {
        log.debug("getting User Object for id: " + getCreator());
        creatorName = userService.getUsernameByTopazId(getCreator());
      } catch (ApplicationException ae) {
        creatorName = getCreator();
      }
    }
    return creatorName;
  }

  /**
   * @param creatorName The creatorName to set.
   */
  public void setCreatorName(String creatorName) {
    this.creatorName = creatorName;
  }

  /**
   * @return Returns the userService.
   */
  protected UserService getUserService() {
    return userService;
  }

  /**
   * @param userService The userService to set.
   */
  public void setUserService(UserService userService) {
    this.userService = userService;
  }
}
