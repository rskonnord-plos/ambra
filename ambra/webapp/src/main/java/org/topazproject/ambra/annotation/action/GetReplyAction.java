/* $HeadURL::                                                                            $
 * $Id:GetReplyAction.java 722 2006-10-02 16:42:45Z viru $
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

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.annotation.service.WebReply;

/**
 * Used to fetch a reply given an id.
 */
public class GetReplyAction extends AnnotationActionSupport {
  private String replyId;
  private WebReply reply;

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

  public WebReply getReply() {
    return reply;
  }
}
