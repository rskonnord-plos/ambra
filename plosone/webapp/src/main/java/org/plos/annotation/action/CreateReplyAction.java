/* $HeadURL::                                                                            $
 * $Id:CreateReplyAction.java 722 2006-10-02 16:42:45Z viru $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.action;

import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.util.ProfanityCheckingService;

import java.util.List;

/**
 * Action for creating a reply.
 */
public class CreateReplyAction extends AnnotationActionSupport {
  private String replyId;
  private String root;
  private String inReplyTo;
  private String commentTitle;
  private String mimeType = "text/plain";
  private String comment;

  private ProfanityCheckingService profanityCheckingService;

  private static final Log log = LogFactory.getLog(CreateReplyAction.class);

  public String execute() throws Exception {
    try {
      final List<String> profaneWordsInTitle = profanityCheckingService.validate(commentTitle);
      final List<String> profaneWordsInBody = profanityCheckingService.validate(comment);

      if (profaneWordsInBody.isEmpty() && profaneWordsInTitle.isEmpty()) {
        replyId = getAnnotationService().createReply(root, inReplyTo, commentTitle, mimeType, comment);
      } else {
        addProfaneMessages(profaneWordsInBody, "comment", "comment");
        addProfaneMessages(profaneWordsInTitle, "commentTitle", "title");
        return INPUT;
      }
    } catch (final ApplicationException e) {
      log.error("Could not create reply to root: " + root + " and inReplyTo: " + inReplyTo, e);
      addActionError("Reply creation failed with error message: " + e.getMessage());
      return ERROR;
    }
    addActionMessage("Reply created with id:" + replyId);

    return SUCCESS;
  }

  public String getReplyId() {
    return replyId;
  }

  public void setRoot(final String root) {
    this.root = root;
  }

  public void setInReplyTo(final String inReplyTo) {
    this.inReplyTo = inReplyTo;
  }

  public void setCommentTitle(final String commentTitle) {
    this.commentTitle = commentTitle;
  }

  public void setMimeType(final String mimeType) {
    this.mimeType = mimeType;
  }

  public void setComment(final String comment) {
    this.comment = comment;
  }

  @RequiredStringValidator(message = "The annotation id to which it applies is required")
  public String getRoot() {
    return root;
  }

  @RequiredStringValidator(message = "The annotation/reply id to which it applies is required")
  public String getInReplyTo() {
    return inReplyTo;
  }

  @RequiredStringValidator(message = "A title is required")
  public String getCommentTitle() {
  return commentTitle;
  }

  @RequiredStringValidator(message = "A reply is required")
  public String getComment() {
  return comment;
  }
  
  public void setProfanityCheckingService(final ProfanityCheckingService profanityCheckingService) {
    this.profanityCheckingService = profanityCheckingService;
  }

}
