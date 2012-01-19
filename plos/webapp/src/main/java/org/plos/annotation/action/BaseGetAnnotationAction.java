/* $HeadURL::                                                                            $
 * $Id:GetAnnotationAction.java 722 2006-10-02 16:42:45Z viru $
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.action;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.annotation.service.WebAnnotation;

/**
 * Used to fetch an annotation given an id.
 *
 */
public abstract class BaseGetAnnotationAction extends AnnotationActionSupport {
  private String annotationId;
  private WebAnnotation annotation;
  private String creatorUserName;

  private static final Log log = LogFactory.getLog(BaseGetAnnotationAction.class);

  public String execute() throws Exception {
    try {
      annotation = getAnnotationService().getAnnotation(annotationId);
      try {
        creatorUserName = getUserService().getUsernameByTopazId(annotation.getCreator());
      } catch (ApplicationException ae){
        log.debug("Couldn't retrieve username: " + annotationId, ae);
        //Temporarily here to allow for anonymous annotations
        creatorUserName = "anonymous";
      }
      if (log.isDebugEnabled()){
        StringBuilder message = new StringBuilder("CreatorUserName for annotationId ");
        log.debug(message.append(annotationId).append(": ").append(creatorUserName));
      }
    } catch (final ApplicationException e) {
      log.error("Could not retreive annotation with id: " + annotationId, e);
      addActionError("Annotation fetching failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Set the annotationId for the annotation to fetch
   * @param annotationId annotationId
   */
  public void setAnnotationId(final String annotationId) {
    this.annotationId = annotationId;
  }

  @RequiredStringValidator(message = "Annotation Id is a required field")
  public String getAnnotationId() {
    return annotationId;
  }

  public WebAnnotation getAnnotation() {
    return annotation;
  }

  /**
   * @return Returns the creatorUserName.
   */
  public String getCreatorUserName() {
    return creatorUserName;
  }

  /**
   * @param creatorUserName The creatorUserName to set.
   */
  public void setCreatorUserName(String creatorUserName) {
    this.creatorUserName = creatorUserName;
  }

}
