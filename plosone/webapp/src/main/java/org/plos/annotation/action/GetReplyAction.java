/* $HeadURL::                                                                            $
 * $Id:GetReplyAction.java 722 2006-10-02 16:42:45Z viru $
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
import org.plos.annotation.service.Reply;

/**
 * Used to fetch a reply given an id.
 */
public class GetReplyAction extends AnnotationActionSupport {
  private String replyId;
  private Reply reply;

  private static final Log log = LogFactory.getLog(GetReplyAction.class);

  public String execute() throws Exception {
    try {
      reply = getAnnotationService().getReply(replyId);
    } catch (final ApplicationException e) {
      log.error("Could not retrieve reply: " + replyId, e);
      addActionError("Reply fetching failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  public void setReplyId(final String replyId) {
    this.replyId = replyId;
  }

  @RequiredStringValidator(message = "Reply id is a required field")
  public String getReplyId() {
    return replyId;
  }

  public Reply getReply() {
    return reply;
  }
}
