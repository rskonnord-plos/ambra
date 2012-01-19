/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.annotation;

/**
 * The reply thread starting with this reply.
 *
 * @author Pradeep Krishnan
 */
public class ReplyThread extends ReplyInfo {
  private ReplyThread[] replies;

  /**
   * Get replies.
   *
   * @return replies as ReplyThread[].
   */
  public ReplyThread[] getReplies() {
    return replies;
  }

  /**
   * Set replies.
   *
   * @param replies the value to set.
   */
  public void setReplies(ReplyThread[] replies) {
    this.replies = replies;
  }
}
