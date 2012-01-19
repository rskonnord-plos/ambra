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
import org.plos.util.DateParser;
import org.plos.util.InvalidDateException;
import org.topazproject.ws.annotation.AnnotationInfo;

import java.util.Date;

/**
 * Plosone wrapper around the AnnotationsInfo from topaz service. It provides
 * - A way to escape title/body text when returning the result to the web layer
 * - a separation from any topaz changes
 */
public abstract class Annotation extends BaseAnnotation {
  private final AnnotationInfo annotation;
  private UserService userService;
  private String creatorName;
  
  private static final Log log = LogFactory.getLog(Annotation.class);
  
  /**
   * Get the target(probably a uri) that it annotates
   * @return target
   */
  public String getAnnotates() {
    return annotation.getAnnotates();
  }

  /**
   * Set the target (probably a uri) that it annotates.
   * @param annotates annotates
   */
  public void setAnnotates(final String annotates) {
    annotation.setAnnotates(annotates);
  }

  /**
   * Get context.
   * @return context as String.
   */
  public String getContext() {
    return annotation.getContext();
  }

  /**
   * Set context.
   * @param context the value to set.
   */
  public void setContext(final String context) {
    annotation.setContext(context);
  }
  
  
  /**
   * Get created date.
   * @return created as java.util.Date.
   */
  public Date getCreatedAsDate() {
    try {
      return DateParser.parse(annotation.getCreated());
    } catch (InvalidDateException ide) {
      log.error("Could not parse date for reply: " + this.getId() + 
                "; dateString is: " + annotation.getCreated(), ide);
    }
    return null;
  }

  /**
   * Get created.
   * @return created as String.
   */
  public String getCreated() {
    return annotation.getCreated();
  }

  /**
   * Set created.
   * @param created the value to set.
   */
  public void setCreated(final String created) {
    annotation.setCreated(created);
  }

  /**
   * Get creator.
   * @return creator as String.
   */
  public String getCreator() {
    return annotation.getCreator();
  }

  /**
   * Set creator.
   * @param creator the value to set.
   */
  public void setCreator(final String creator) {
    annotation.setCreator(creator);
  }

  /**
   * @return Returns the creatorName.
   */
  public String getCreatorName() {
    if (creatorName == null) {
      final String creator = getCreator();
      try {
        log.debug("getting User Object for id: " + creator);
        creatorName = userService.getUsernameByTopazId(creator);
      } catch (ApplicationException ae) {
        creatorName = creator;
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
   * Get id.
   * @return id as String.
   */
  public String getId() {
    return annotation.getId();
  }

  /**
   * Set id.
   * @param id the value to set.
   */
  public void setId(final String id) {
    annotation.setId(id);
  }

  /**
   * Get mediator.
   * @return mediator as String.
   */
  public String getMediator() {
    return annotation.getMediator();
  }

  /**
   * Set mediator.
   * @param mediator the value to set.
   */
  public void setMediator(final String mediator) {
    annotation.setMediator(mediator);
  }

  /**
   * Get state.
   * @return state as int.
   */
  public int getState() {
    return annotation.getState();
  }

  /**
   * Set state.
   * @param state the value to set.
   */
  public void setState(final int state) {
    annotation.setState(state);
  }

  /**
   * Get supersededBy.
   * @return supersededBy as String.
   */
  public String getSupersededBy() {
    return annotation.getSupersededBy();
  }

  /**
   * Set supersededBy.
   * @param supersededBy the value to set.
   */
  public void setSupersededBy(final String supersededBy) {
    annotation.setSupersededBy(supersededBy);
  }

  /**
   * Get supersedes.
   * @return supersedes as String.
   */
  public String getSupersedes() {
    return annotation.getSupersedes();
  }

  /**
   * Set supersedes.
   * @param supersedes the value to set.
   */
  public void setSupersedes(final String supersedes) {
    annotation.setSupersedes(supersedes);
  }

 /**
  * Escaped text of title.
  * @return title as String.
  */
  public String getCommentTitle() {
    return escapeText(annotation.getTitle());
  }

  /**
   * Set commentTitle.
   * @param commentTitle the value to set.
   */
  public void setCommentTitle(final String commentTitle) {
    annotation.setTitle(commentTitle);
  }

  /**
   * Get annotation type.
   * @return annotation type as String.
   */
  public String getType() {
    return annotation.getType();
  }

  /**
   * Set annotation type.
   * @param type the value to set.
   */
  public void setType(final String type) {
    annotation.setType(type);
  }

  public Annotation(final AnnotationInfo annotation) {
    this.annotation = annotation;
  }

  /**
   * Constructor that takes in an UserService object in addition to the annotation in order
   * to do username lookups.
   * 
   * @param annotation
   * @param userSvc
   */
  public Annotation(final AnnotationInfo annotation, final UserService userSvc) {
    this.annotation = annotation;
    this.userService = userSvc;
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
