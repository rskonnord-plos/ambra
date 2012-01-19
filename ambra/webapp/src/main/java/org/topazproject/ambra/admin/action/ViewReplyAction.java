/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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

package org.topazproject.ambra.admin.action;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.ambra.annotation.service.ReplyService;
import org.topazproject.ambra.models.Reply;

public class ViewReplyAction extends BaseAdminActionSupport {

  private String replyId;
  private Reply reply;
  private ReplyService replyService;

  private static final Log log = LogFactory.getLog(ViewReplyAction.class);


  public String execute() throws Exception {
    reply = replyService.getReply(replyId);
    return SUCCESS;
  }

  public Reply getReply() {
    return reply;
  }

  public void setReplyId(String annotationId) {
    this.replyId = annotationId;
  }

  public void setReplyService(ReplyService replyService) {
    this.replyService = replyService;
  }
}
