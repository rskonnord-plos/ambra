/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.annotation;

/**
 * The Reply meta-data.
 *
 * @author Pradeep Krishnan
 */
public class ReplyInfo {
  private String id;
  private String type;
  private String root;
  private String inReplyTo;
  private String title;
  private String creator;
  private String created;
  private String body;
  private String mediator;
  private int    state;

  /**
   * Get id.
   *
   * @return id as String.
   */
  public String getId() {
    return id;
  }

  /**
   * Set id.
   *
   * @param id the value to set.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get type.
   *
   * @return type as String.
   */
  public String getType() {
    return type;
  }

  /**
   * Set type.
   *
   * @param type the value to set.
   */
  public void setType(String type) {
    this.type = type;
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
   * @return inReplyTo as String.
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
   * Get title.
   *
   * @return title as String.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set title.
   *
   * @param title the value to set.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Get creator.
   *
   * @return creator as String.
   */
  public String getCreator() {
    return creator;
  }

  /**
   * Set creator.
   *
   * @param creator the value to set.
   */
  public void setCreator(String creator) {
    this.creator = creator;
  }

  /**
   * Get created.
   *
   * @return created as String.
   */
  public String getCreated() {
    return created;
  }

  /**
   * Set created.
   *
   * @param created the value to set.
   */
  public void setCreated(String created) {
    this.created = created;
  }

  /**
   * Get body.
   *
   * @return body as String.
   */
  public String getBody() {
    return body;
  }

  /**
   * Set body.
   *
   * @param body the value to set.
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * Get mediator.
   *
   * @return mediator as String.
   */
  public String getMediator() {
    return mediator;
  }

  /**
   * Set mediator.
   *
   * @param mediator the value to set.
   */
  public void setMediator(String mediator) {
    this.mediator = mediator;
  }

  /**
   * Get state.
   *
   * @return state as int.
   */
  public int getState() {
    return state;
  }

  /**
   * Set state.
   *
   * @param state the value to set.
   */
  public void setState(int state) {
    this.state = state;
  }
}
