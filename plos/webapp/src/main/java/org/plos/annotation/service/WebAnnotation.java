/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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

import com.googlecode.jsonplugin.annotations.JSON;

import java.util.Date;

/**
 * Plosone wrapper around the AnnotationsInfo from topaz service. It provides
 * - A way to escape title/body text when returning the result to the web layer
 * - a separation from any topaz changes
 */
public abstract class WebAnnotation extends BaseAnnotation {
  private final AnnotationInfo annotation;
  private UserService userService;
  private String creatorName;
  private Date creationDate;

  private static final Log log = LogFactory.getLog(WebAnnotation.class);

  /**
   * Get the target(probably a uri) that it annotates
   * @return target
   */
  public String getAnnotates() {
    return annotation.getAnnotates();
  }

  /**
   * Get context.
   * @return context as String.
   */
  public String getContext() {
    return annotation.getContext();
  }

  /**
   * Get created date.
   * @return created as java.util.Date.
   */
  @JSON(serialize = false)
  public Date getCreatedAsDate() {
    try {
      if (creationDate == null)
        creationDate = DateParser.parse(annotation.getCreated());
    } catch (InvalidDateException ide) {
      log.error("Could not parse date for reply: " + this.getId() +
                "; dateString is: " + annotation.getCreated(), ide);
    }
    return creationDate;
  }

  /**
   * Get created date.
   * @return created as java.util.Date.
   */
  public long getCreatedAsMillis() {
    if (getCreatedAsDate() != null) {
      return creationDate.getTime();
    }
    return 0;
  }
  
  /**
   * Get created.
   * @return created as String.
   */
  public String getCreated() {
    return annotation.getCreated();
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
  /*
  public void setCreator(final String creator) {
    annotation.setCreator(creator);
  }
  */

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
   * Get id.
   * @return id as String.
   */
  public String getId() {
    return annotation.getId();
  }

  /**
   * Get mediator.
   * @return mediator as String.
   */
  public String getMediator() {
    return annotation.getMediator();
  }


  /**
   * Get state.
   * @return state as int.
   */
  public int getState() {
    return annotation.getState();
  }

  /**
   * Get supersededBy.
   * @return supersededBy as String.
   */
  public String getSupersededBy() {
    return annotation.getSupersededBy();
  }

  /**
   * Get supersedes.
   * @return supersedes as String.
   */
  public String getSupersedes() {
    return annotation.getSupersedes();
  }

 /**
  * Escaped text of title.
  * @return title as String.
  */
  public String getCommentTitle() {
    String title;
    if (isMinorCorrection()) {
      title = "Minor Correction: " + annotation.getTitle();
    }
    else if (isFormalCorrection()) {
      title = "Formal Correction: " + annotation.getTitle();
    }
    else {
      title = annotation.getTitle();
    }
    return escapeText(title);
  }

  /**
   * Get annotation type.
   * @return annotation type as String.
   */
  public String getType() {
    return annotation.getType();
  }
  
  public boolean isFormalCorrection() {
    return AnnotationService.WEB_TYPE_FORMAL_CORRECTION.equals(annotation.getWebType());
  }
  
  public boolean isMinorCorrection() {
    return AnnotationService.WEB_TYPE_MINOR_CORRECTION.equals(annotation.getWebType());
  }

  public boolean isCorrection() {
    return isFormalCorrection() || isMinorCorrection();
  }
  
  public WebAnnotation(final AnnotationInfo annotation) {
    this.annotation = annotation;
  }

  /**
   * Constructor that takes in an UserService object in addition to the annotation in order
   * to do username lookups.
   * 
   * @param annotation
   * @param userSvc
   */
  public WebAnnotation(final AnnotationInfo annotation, final UserService userSvc) {
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
