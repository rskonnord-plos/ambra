/* $HeadURL::                                                                            $
 * $Id:CreateReplyAction.java 722 2006-10-02 16:42:45Z viru $
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
package org.topazproject.ambra.annotation.action;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import org.topazproject.ambra.action.BaseSessionAwareActionSupport;
import org.topazproject.ambra.annotation.service.ReplyService;
import org.topazproject.ambra.util.ProfanityCheckingService;
import org.topazproject.ambra.user.AmbraUser;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

/**
 * Action for creating a reply.
 */
@SuppressWarnings("serial")
public class CreateReplyAction extends BaseSessionAwareActionSupport {
  private String replyId;
  private String root;
  private String inReplyTo;
  private String commentTitle;
  private String mimeType = "text/plain";
  private String comment;

  private ProfanityCheckingService profanityCheckingService;
  protected ReplyService replyService;

  private static final Log log = LogFactory.getLog(CreateReplyAction.class);

  @Transactional(rollbackFor = { Throwable.class })
  @Override
  public String execute() throws Exception {
    try {
      final List<String> profaneWordsInTitle = profanityCheckingService.validate(commentTitle);
      final List<String> profaneWordsInBody = profanityCheckingService.validate(comment);

      if (profaneWordsInBody.isEmpty() && profaneWordsInTitle.isEmpty()) {
        replyId = replyService.createReply(root, inReplyTo, commentTitle, mimeType, comment,
                                           getCurrentUser());
      } else {
        addProfaneMessages(profaneWordsInBody, "comment", "comment");
        addProfaneMessages(profaneWordsInTitle, "commentTitle", "title");
        return INPUT;
      }
    } catch (Exception e) {
      log.error("Could not create reply to root: " + root + " and inReplyTo: " + inReplyTo, e);
      addActionError("Reply creation failed with error message: " + e.getMessage());
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
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

  @Required
  public void setReplyService(final ReplyService replyService) {
    this.replyService = replyService;
  }
}
