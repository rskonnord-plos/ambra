/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.annotations.Predicate;

/**
 * Reply meta-data.
 *
 * @author Pradeep Krishnan
 */
public class ReplyThread extends Reply {
  @Predicate(uri=Reply.NS + "inReplyTo", inverse=true, notOwned=true)
  private List<ReplyThread> replies = new ArrayList<ReplyThread>();

  /**
   * Creates a new ReplyThread object.
   */
  public ReplyThread() {
  }

  /**
   * Creates a new ReplyThread object.
   *
   * @param id reply id
   */
  public ReplyThread(URI id) {
    super(id);
  }

  /**
   *
   * @return the thread of replies
   */
  public List<ReplyThread> getReplies() {
    return replies;
  }

  /**
   *
   * @param replies the thread of replies
   */
  public void setReplies(List<ReplyThread> replies) {
    this.replies = replies;
  }

  /**
   * Add a reply to this.
   *
   * @param r the reply to add
   */
  public void addReply(ReplyThread r) {
    r.setRoot(getRoot());
    r.setInReplyTo(getId());
    replies.add(r);
  }
}
