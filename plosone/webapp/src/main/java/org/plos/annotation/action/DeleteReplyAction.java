/* $HeadURL::                                                                            $
 * $Id:DeleteReplyAction.java 722 2006-10-02 16:42:45Z viru $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;

/**
 * Action class to delete a given reply.
 */
public class DeleteReplyAction extends AnnotationActionSupport {
  private String id;
  private String root;
  private String inReplyTo;

  private static final Log log = LogFactory.getLog(DeleteReplyAction.class);

  /**
   * Delete a reply given a reply id
   * @return operation return code
   * @throws Exception Exception
   */
  public String deleteReplyWithId() throws Exception {
    try {
      getAnnotationService().deleteReply(id);
    } catch (final ApplicationException e) {
      log.error("Could not delete reply: " + id, e);
      addActionError("Reply deletion failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Delete a reply given a root and inReplyTo
   * @return operation return code
   * @throws Exception Exception
   */
  public String deleteReplyWithRootAndReplyTo() throws Exception {
    try {
      getAnnotationService().deleteReply(root, inReplyTo);
    } catch (final ApplicationException e) {
      log.error("Could not delete reply with root: " + root + " replyTo: " + inReplyTo, e);
      addActionError("Reply deletion failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setRoot(final String root) {
    this.root = root;
  }

  public void setInReplyTo(final String inReplyTo) {
    this.inReplyTo = inReplyTo;
  }

  public String getId() {
    return id;
  }

  public String getRoot() {
    return root;
  }

  public String getInReplyTo() {
    return inReplyTo;
  }
}
