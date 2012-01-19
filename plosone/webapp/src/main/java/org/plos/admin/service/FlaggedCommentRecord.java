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
  
  public FlaggedCommentRecord(String flagId, String target, String targetTitle, String flagComment, 
      String created, String creator, String creatorid, String root, 
      String reasonCode) {
    this.target = target;
    this.targetTitle = targetTitle; 
    this.root = root;
    this.creator = creator;
    this.created = created;
    this.flagComment = flagComment;
    this.reasonCode = reasonCode;
    this.flagId = flagId;
    this.creatorid = creatorid;
  }
  
  public String getTargetDisplayURL() {
    if (null == root) { 		// Annotation
      return "viewAnnotation.action?annotationId=" + target;
    } else { // Reply
      return "viewReply.action?replyId=" + target;
    }
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
