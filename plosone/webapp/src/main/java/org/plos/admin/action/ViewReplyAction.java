/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.action;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.annotation.service.ReplyWebService;
import org.plos.annotation.service.ReplyInfo;

public class ViewReplyAction extends BaseAdminActionSupport {
  
  private String replyId;
  private ReplyInfo replyInfo;
  private ReplyWebService replyWebService;
  
  private static final Log log = LogFactory.getLog(ViewReplyAction.class);
  
  
  public String execute() throws Exception {
    replyInfo = replyWebService.getReplyInfo(replyId);
    return SUCCESS;
  }
  
  public ReplyInfo getReplyInfo() {
    return replyInfo;
  }
  
  
  public void setReplyId(String annotationId) {
    this.replyId = annotationId;
  }
  
  public void setReplyWebService(ReplyWebService replyWebService) {
    this.replyWebService = replyWebService;
  }
}
