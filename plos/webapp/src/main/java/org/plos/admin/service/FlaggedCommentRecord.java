/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.service;

import org.plos.annotation.service.AnnotationService;

public class FlaggedCommentRecord implements Comparable<FlaggedCommentRecord> {

  private String root;
  private String creator;
  private String created;
  private String reasonCode;
  private String flagComment;
  private String targetTitle;
  private String flagId;
  private String target;
  private String creatorid;
  private String targetType;

  public FlaggedCommentRecord(String flagId, String target, String targetTitle, String flagComment,
      String created, String creator, String creatorid, String root,
      String reasonCode, String targetType) {
    this.target = target;
    this.targetTitle = targetTitle;
    this.root = root;
    this.creator = creator;
    this.created = created;
    this.flagComment = flagComment;
    this.reasonCode = reasonCode;
    this.flagId = flagId;
    this.creatorid = creatorid;
    this.targetType = targetType;
  }

  public String getTargetDisplayURL() {

    if (getIsAnnotation()) {
      return "viewAnnotation.action?annotationId=" + target;
    } else if (getIsRating()) {
      return "viewRating.action?ratingId=" + target;
    } else if (getIsReply()) {
      return "viewReply.action?replyId=" + target;
    }

    // not possible
    return "";
  }

  /**
   * Get the type, Class name, of the target.
   *
   * @return Type of the target.
   */
  public String getTargetType() {
    return targetType;
  }

  public boolean isTargetType(String testType) {
    return targetType.equals(testType);
  }
  
  public boolean isCorrection() {
    return (AnnotationService.WEB_TYPE_FORMAL_CORRECTION.equals(targetType) || 
        AnnotationService.WEB_TYPE_MINOR_CORRECTION.equals(targetType));
  }
  
  public boolean isFormalCorrection() {
    return (AnnotationService.WEB_TYPE_FORMAL_CORRECTION.equals(getTargetType()));
  }

  public boolean isMinorCorrection() {
    return (AnnotationService.WEB_TYPE_MINOR_CORRECTION.equals(getTargetType()));
  }
  
  /**
   * Is this a Flag for an Annotation?  (Actually a Comment.)
   *
   * @return true if Flag for an Annotation, else false.
   */
  public boolean getIsAnnotation() {
    if (AnnotationService.WEB_TYPE_COMMENT.equals(targetType) || 
        AnnotationService.WEB_TYPE_NOTE.equals(targetType) || 
        isCorrection()) {
      return true;
    }
    return false;
  }

  /**
   * Is this a Flag for a Rating?
   *
   * @return true if Flag for a Rating, else false.
   */
  public boolean getIsRating() {

    if (targetType.equals(AnnotationService.WEB_TYPE_RATING)) {
      return true;
    }

    return false;
}

  /**
   * Is this a Flag for a Reply?
   *
   * @return true if Flag for a Reply, else false.
   */
  public boolean getIsReply() {

    if (targetType.equals(AnnotationService.WEB_TYPE_REPLY)) {
      return true;
    }

    return false;
  }

  public String getRoot() {
    return (null == root) ? "" : root;
  }

  public String getCreator() {
    if (null != creator)
      return creator;
    else
      return "Cannot locate user-name";
  }

  public String getFlagComment() {
    return flagComment;
  }

  public String getCreated() {
    return created;
  }

  public String getReasonCode() {
    return reasonCode;
  }

  public String getTargetTitle() {
    return targetTitle;
  }

  public String getFlagId() {
    return flagId;
  }

  public String getTarget() {
    return target;
  }

  public String getCreatorid() {
    return creatorid;
  }

  public int compareTo (FlaggedCommentRecord o) {
    if (created == null) {
      if ((o == null) || (o.getCreated() == null)) {
        return 0;
      } else {
        return -1;
      }
    }
    if ((o == null) || (o.getCreated() == null)) {
      return 1;
    }
    return created.compareTo(o.getCreated());
  }
}
