/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.samples;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.annotations.Inverse;
import org.topazproject.otm.annotations.Ns;
import org.topazproject.otm.annotations.Rdf;

/**
 * Reply meta-data.
 *
 * @author Pradeep Krishnan
 */
public class ReplyThread extends Reply {
  @Inverse
  @Rdf(Reply.NS + "inReplyTo")
  private List<ReplyThread> replies = new ArrayList<ReplyThread>();

  /**
   * Creates a new ReplyThread object.
   */
  public ReplyThread() {
  }

  /**
   * Creates a new ReplyThread object.
   *
   * @param id DOCUMENT ME!
   */
  public ReplyThread(URI id) {
    super(id);
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public List<ReplyThread> getReplies() {
    return replies;
  }

  /**
   * DOCUMENT ME!
   *
   * @param replies DOCUMENT ME!
   */
  public void setReplies(List<ReplyThread> replies) {
    this.replies = replies;
  }

  /**
   * DOCUMENT ME!
   *
   * @param r DOCUMENT ME!
   */
  public void addReply(ReplyThread r) {
    r.setRoot(getRoot());
    r.setInReplyTo(getId());
    replies.add(r);
  }
}
