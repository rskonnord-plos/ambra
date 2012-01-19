/* $HeadURL::                                                                            $
 * $Id$
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
package org.topazproject.ambra.annotation.service;

import java.io.IOException;

import java.net.URI;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.topazproject.ambra.annotation.service.BaseAnnotation.DELETE_MASK;
import static org.topazproject.ambra.annotation.service.BaseAnnotation.FLAG_MASK;
import static org.topazproject.ambra.annotation.service.BaseAnnotation.PUBLIC_MASK;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.models.Reply;
import org.topazproject.ambra.models.ReplyBlob;
import org.topazproject.ambra.models.ReplyThread;
import org.topazproject.ambra.permission.service.PermissionsService;
import org.topazproject.ambra.user.AmbraUser;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.criterion.Restrictions;

/**
 * Wrapper over reply web service
 */
public class ReplyService extends BaseAnnotationService {
  private static final Log     log         = LogFactory.getLog(ReplyService.class);
  private final RepliesPEP     pep;
  private Session              session;
  private PermissionsService permissionsService;

  /**
   * Create a new instance of ReplyService.
   *
   * @throws IOException on an error in creating the PEP
   */
  public ReplyService() throws IOException {
    try {
      pep                                  = new RepliesPEP();
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      IOException ioe = new IOException("Failed to create PEP");
      ioe.initCause(e);
      throw ioe;
    }
  }

  /**
   * Create a reply that replies to a message.
   *
   * @param mimeType mimeType of the reply body
   * @param root root of this thread
   * @param inReplyTo the message this is in reply to
   * @param title title of this message
   * @param body the reply body
   *
   * @return a new reply
   *
   * @throws Exception on an error
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String createReply(final String mimeType, final String root, final String inReplyTo,
                            final String title, final String body)
                     throws Exception {
    pep.checkAccess(pep.CREATE_REPLY, URI.create(root));

    if (!root.equals(inReplyTo))
      pep.checkAccess(pep.CREATE_REPLY, URI.create(inReplyTo));

    final String contentType = getContentType(mimeType);
    String       user        = AmbraUser.getCurrentUser().getUserId();
    ReplyBlob    blob        = new ReplyBlob(contentType, body.getBytes(getEncodingCharset()));

    final Reply  r           = new ReplyThread();
    r.setMediator(getApplicationId());
    r.setType(getDefaultType());
    r.setRoot(root);
    r.setInReplyTo(inReplyTo);
    r.setTitle(title);

    if (isAnonymous())
      r.setAnonymousCreator(user);
    else
      r.setCreator(user);

    r.setBody(blob);
    r.setCreated(new Date());

    String  newId      = session.saveOrUpdate(r);

    permissionsService.propagatePermissions(newId, new String[] { blob.getId() });

    return newId;
  }

  /**
   * Mark the reply as deleted.
   *
   * @param replyId replyId
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void deleteReply(final String replyId) throws OtmException, SecurityException {
    pep.checkAccess(pep.SET_REPLY_STATE, URI.create(replyId));

    Reply a = session.get(Reply.class, replyId);
    a.setState(DELETE_MASK);
  }

  /**
   * Delete the subtree and not just mark it as deleted.
   *
   * @param root root of the discussion thread
   * @param inReplyTo the messages that replied to this and their children are to be deleted
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void deleteReplies(final String root, final String inReplyTo)
                     throws OtmException, SecurityException {
    final List<Reply> all =
      session.createCriteria(ReplyThread.class).add(Restrictions.eq("root", root))
              .add(Restrictions.walk("replies", inReplyTo)).list();

    for (Reply r : all)
      deleteReplies(r.getId().toString());
  }

  /**
   * Delete the sub tree including this message.
   *
   * @param target the reply to delete
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void deleteReplies(final String target) throws OtmException, SecurityException {
    if (log.isDebugEnabled())
      log.debug("deleting reply and descendants with id: " + target);

    final List<Reply> all  = new ArrayList();
    final ReplyThread root = session.get(ReplyThread.class, target);

    if (root != null)
      add(all, root);

    for (Reply r : all) {
      pep.checkAccess(pep.DELETE_REPLY, r.getId());
      permissionsService.cancelPropagatePermissions(r.getId().toString(),
                                               new String[] { r.getBody().getId() });
    }

    session.delete(root); // ... and cascade
  }

  private void add(List<Reply> all, ReplyThread r) {
    all.add(r);

    for (ReplyThread t : r.getReplies())
      add(all, t);
  }

  /**
   * Gets the reply given its id.
   *
   * @param replyId the replyId
   *
   * @return a reply
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   * @throws IllegalArgumentException if the id does not correspond to a reply
   */
  @Transactional(readOnly = true)
  public Reply getReply(final String replyId)
                         throws OtmException, SecurityException, IllegalArgumentException {
    pep.checkAccess(pep.GET_REPLY_INFO, URI.create(replyId));

    Reply a = session.get(Reply.class, replyId);

    if (a == null)
      throw new IllegalArgumentException("invalid reply id: " + replyId);

    return a;
  }

  /**
   * List all messages in reply-to a specific message in a thread.
   *
   * @param root root of this discussion thread
   * @param inReplyTo return all messages that are in reply to this message
   *
   * @return list of replies
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  @Transactional(readOnly = true)
  public Reply[] listReplies(final String root, final String inReplyTo)
                          throws OtmException, SecurityException {
    List<Reply> all =
      session.createCriteria(Reply.class).add(Restrictions.eq("root", root))
              .add(Restrictions.eq("inReplyTo", inReplyTo)).list();

    List<Reply> l   = new ArrayList(all.size());

    for (Reply a : all) {
      try {
        pep.checkAccess(pep.GET_REPLY_INFO, a.getId());
        l.add(a);
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("no permission for viewing reply " + a.getId()
                    + " and therefore removed from list");
      }
    }

    return l.toArray(new  Reply[l.size()]);
  }

  /**
   * Transitively list all replies that are in reply-to a specific message in a thread.
   *
   * @param root root of this discussion thread
   * @param inReplyTo walk down the thread and return all messages that are transitively in reply
   *        to this message
   *
   * @return list of replies
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  @Transactional(readOnly = true)
  public Reply[] listAllReplies(final String root, final String inReplyTo)
                             throws OtmException, SecurityException {
    List<Reply> all;

    if (inReplyTo.equals(root))
      all = session.createCriteria(Reply.class).add(Restrictions.eq("root", root)).list();
    else
      all = session.createCriteria(Reply.class).add(Restrictions.eq("root", root))
              .add(Restrictions.walk("inReplyTo", inReplyTo)).list();

    List<Reply> l   = new ArrayList(all.size());

    for (Reply a : all) {
      try {
        pep.checkAccess(pep.GET_REPLY_INFO, a.getId());
        l.add(a);
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("no permission for viewing reply " + a.getId()
                    + " and therefore removed from list");
      }
    }

    return l.toArray(new Reply[l.size()]);
  }

  /**
   * Unflag a reply
   *
   * @param replyId replyId
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void unflagReply(final String replyId) throws OtmException, SecurityException {
    pep.checkAccess(pep.SET_REPLY_STATE, URI.create(replyId));

    Reply a = session.get(Reply.class, replyId);
    a.setState(a.getState() & ~FLAG_MASK);
  }

  /**
   * Set the reply as flagged
   *
   * @param replyId replyId
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void setFlagged(final String replyId) throws OtmException, SecurityException {
    pep.checkAccess(pep.SET_REPLY_STATE, URI.create(replyId));

    Reply a = session.get(Reply.class, replyId);
    a.setState(a.getState() | FLAG_MASK);
  }

  /**
   * List the set of replies in a specific administrative state.
   *
   * @param mediator if present only those replies that match this mediator are returned
   * @param state the state to filter the list of replies by or 0 to return replies in any
   *        administartive state
   *
   * @return an array of replies; if no matching replies are found, an empty array is returned
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  @Transactional(readOnly = true)
  public Reply[] listReplies(final String mediator, final int state)
                          throws OtmException, SecurityException {
    pep.checkAccess(pep.LIST_REPLIES_IN_STATE, pep.ANY_RESOURCE);

    Criteria c = session.createCriteria(Reply.class);

    if (mediator != null)
      c.add(Restrictions.eq("mediator", mediator));

    if (state == 0)
      c.add(Restrictions.ne("state", "0"));
    else
      c.add(Restrictions.eq("state", "" + state));

    List<Reply> l       = c.list();

    return l.toArray(new Reply[l.size()]);
  }

  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Set the PermissionsService
   *
   * @param permissionsService permissionWebService
   */
  @Required
  public void setPermissionsService(final PermissionsService permissionsService) {
    this.permissionsService = permissionsService;
  }
}
