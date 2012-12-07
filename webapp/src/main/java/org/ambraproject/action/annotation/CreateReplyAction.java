/* $HeadURL::                                                                            $
 * $Id:CreateReplyAction.java 722 2006-10-02 16:42:45Z viru $
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
package org.ambraproject.action.annotation;

import org.ambraproject.service.annotation.AnnotationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Action for creating a reply.
 */
public class CreateReplyAction extends DiscussionAction {

  private Long replyId;
  private Long inReplyTo;

  private AnnotationService annotationService;

  private static final Logger log = LoggerFactory.getLogger(CreateReplyAction.class);

  @Override
  protected void create() {
    replyId = annotationService.createReply(getCurrentUser(), inReplyTo, commentTitle, comment, ciStatement);
  }

  @Override
  protected void error(Exception e) {
    log.error("Could not create reply to: " + inReplyTo, e);
    addActionError("Reply creation failed with error message: " + e.getMessage());
  }

  @Override
  protected void success() {
    addActionMessage("Reply created with id:" + replyId);
  }


  public Long getReplyId() {
    return replyId;
  }

  public void setInReplyTo(final Long inReplyTo) {
    this.inReplyTo = inReplyTo;
  }

  @Required
  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
  }
}
