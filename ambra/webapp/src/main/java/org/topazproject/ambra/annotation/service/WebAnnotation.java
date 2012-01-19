/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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
package org.topazproject.ambra.annotation.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.ambra.models.Annotation;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.Correction;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.MinorCorrection;
import org.topazproject.ambra.user.service.UserService;

import com.googlecode.jsonplugin.annotations.JSON;

import java.net.URI;
import java.util.Date;

/**
 *  View level wrapper for Annotations. It provides
 * - A way to escape title/body text when returning the result to the web layer
 * - a separation from any content model changes
 */
public abstract class WebAnnotation extends BaseAnnotation {
  private final ArticleAnnotation annotation;
  private UserService userService;
  private String creatorName;

  private static final Log log = LogFactory.getLog(WebAnnotation.class);

  /**
   * Get the target(probably a uri) that it annotates
   * @return target
   */
  public String getAnnotates() {
    URI u = annotation.getAnnotates();
    return (u == null) ? null : u.toString();
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
    return annotation.getCreated();
  }

  /**
   * Get created date.
   * @return created as java.util.Date.
   */
  public long getCreatedAsMillis() {
    Date d = getCreatedAsDate();
    return (d != null) ? d.getTime() : 0;
  }

  /**
   * Get created.
   * @return created as String.
   */
  public String getCreated() {
    return annotation.getCreatedAsString();
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
        if (log.isDebugEnabled())
          log.debug("getting User Object for id: " + creator);
        creatorName = userService.getUsernameByTopazId(creator);
      } catch (Exception ae) {
        if (log.isDebugEnabled())
          log.debug("Failed to lookup user name of the Annotation creator.", ae);
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
    return annotation.getId().toString();
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
    Annotation a = annotation.getSupersededBy();
    return (a == null) ? null : a.getId().toString();
  }

  /**
   * Get supersedes.
   * @return supersedes as String.
   */
  public String getSupersedes() {
    Annotation a = annotation.getSupersedes();
    return (a == null) ? null : a.getId().toString();
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
    return annotation instanceof FormalCorrection;
  }
  
  public boolean isMinorCorrection() {
    return annotation instanceof MinorCorrection;
  }

  public boolean isCorrection() {
    return annotation instanceof Correction;
  }
  
  public WebAnnotation(final ArticleAnnotation annotation) {
    this.annotation = annotation;
  }

  /**
   * Constructor that takes in an UserService object in addition to the annotation in order
   * to do username lookups.
   * 
   * @param annotation
   * @param userSvc
   */
  public WebAnnotation(final ArticleAnnotation annotation, final UserService userSvc) {
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
