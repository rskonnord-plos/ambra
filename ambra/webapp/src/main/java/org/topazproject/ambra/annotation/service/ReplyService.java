/* $HeadURL::                                                                            $
 * $Id$
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
package org.topazproject.ambra.annotation.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.Constants;
import org.topazproject.ambra.models.Reply;
import org.topazproject.ambra.models.ReplyBlob;
import org.topazproject.ambra.models.ReplyThread;
import org.topazproject.ambra.user.AmbraUser;
import org.topazproject.ambra.xacml.AbstractSimplePEP;

import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.criterion.Restrictions;

import com.sun.xacml.PDP;

/**
 * Wrapper over reply web service
 */
public class ReplyService extends BaseAnnotationService {
  private static final Logger log = LoggerFactory.getLogger(ReplyService.class);

  private AnnotationService  annotationService;   // Annotation service Spring injected.
  private RepliesPEP pep;
  private String     defaultType;

  /**
   * Create a new instance of ReplyService.
   *
   */
  public ReplyService() {
  }

  @Required
  public void setRepliesPdp(PDP pdp) {
    this.pep = new RepliesPEP(pdp);
  }

  @Required
  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  /**
   * Create a reply that replies to a message.
   *
   * @param root root of this thread
   * @param inReplyTo the message this is in reply to
   * @param title title of this message
   * @param mimeType mimeType of the reply body
   * @param body the reply body
   *
   * @param user
   * @return a new reply
   *
   * @throws Exception on an error
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String createReply(final String root, final String inReplyTo, final String title,
                            final String mimeType, final String body, final String ciStatement, AmbraUser user)
                     throws Exception {
    pep.checkAccess(RepliesPEP.CREATE_REPLY, URI.create(root));

    if (!root.equals(inReplyTo))
      pep.checkAccess(RepliesPEP.CREATE_REPLY, URI.create(inReplyTo));

    final String contentType = getContentType(mimeType);
    String userId = user.getUserId();
    ReplyBlob blob = new ReplyBlob(contentType);
    blob.setCIStatement(ciStatement);
    blob.setBody(body.getBytes(getEncodingCharset()));

    final Reply r = new ReplyThread();
    r.setMediator(getApplicationId());
    r.setType(getDefaultType());
    r.setRoot(root);
    r.setInReplyTo(inReplyTo);
    r.setTitle(title);
    r.setCreator(userId);
    r.setBody(blob);
    r.setCreated(new Date());

    String  newId = session.saveOrUpdate(r);

    permissionsService.propagatePermissions(newId, new String[] { blob.getId() });
    setPublicPermissions(newId);

    return newId;
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
  @SuppressWarnings("unchecked")
  @Transactional(rollbackFor = { Throwable.class })
  public void deleteReplies(final String root, final String inReplyTo)
                     throws OtmException, SecurityException {
    final List<Reply> all =
      session.createCriteria(Reply.class).add(Restrictions.eq("root", root)).
                                          add(Restrictions.eq("inReplyTo", inReplyTo)).list();

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

    final List<Reply> all  = new ArrayList<Reply>();
    final ReplyThread root = session.get(ReplyThread.class, target);

    if (root != null)
      add(all, root);

    for (Reply r : all) {
      pep.checkAccess(RepliesPEP.DELETE_REPLY, r.getId());
      if (r.getBody() != null)
        permissionsService.cancelPropagatePermissions(r.getId().toString(),
                                                      new String[] { r.getBody().getId() });
      cancelPublicPermissions(r.getId().toString());
    }

    session.delete(root); // ... and cascade
  }

  private void add(List<Reply> all, ReplyThread r) {
    all.add(r);

    for (ReplyThread t : r.getReplies())
      add(all, t);
  }

  private void remove(List<ReplyThread> all, ReplyThread r) {
    all.remove(r);

    for (ReplyThread t : r.getReplies())
      remove(all, t);
  }

  /**
   * Get all replies specified by the list of reply ids.
   *
   * @param replyIds a list of annotations Ids to retrieve.
   *
   * @return the (possibly empty) list of replies.
   *
   */
  @Transactional(readOnly = true)
  public List<Reply> getReplies(List<String> replyIds) {
    List<Reply> replies = new ArrayList<Reply>();

    for (String id : replyIds)  {
      try {
        replies.add(getReply(id));
      } catch (IllegalArgumentException iae) {
        if (log.isDebugEnabled())
          log.debug("Ignored illegal reply id:" + id);
      } catch (SecurityException se) {
        if (log.isDebugEnabled())
          log.debug("Filtering URI " + id + " from Reply list due to PEP SecurityException", se);
      }
    }
    return replies;
  }

  /**
   * Get all replies satisfying the criteria.
   *
   * @param startDate  is the date to start searching from. If null, start from begining of time.
   *                   Can be iso8601 formatted or string representation of Date object.
   * @param endDate    is the date to search until. If null, search until present date
   * @param annotType  a filter list of rdf types for the annotations.
   * @param maxResults the maximum number of results to return, or 0 for no limit
   *
   * @return the (possibly empty) list of replies.
   *
   * @throws java.text.ParseException if any of the dates or query could not be parsed
   * @throws java.net.URISyntaxException if an element of annotType cannot be parsed as a URI
   */
  @Transactional(readOnly = true)
  public List<Reply> getReplies(Date startDate, Date endDate, Set<String> annotType, int maxResults)
  throws ParseException, URISyntaxException {
    List<String> replyIds = annotationService.getReplyIds(startDate, endDate, annotType, maxResults);
    return getReplies(replyIds);
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
    pep.checkAccess(RepliesPEP.GET_REPLY_INFO, URI.create(replyId));

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
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  public Reply[] listReplies(final String root, final String inReplyTo)
                          throws OtmException, SecurityException {
    pep.checkAccess(RepliesPEP.LIST_REPLIES, URI.create(inReplyTo));

    List<Reply> all =
      session.createCriteria(Reply.class).add(Restrictions.eq("root", root)).
                                          add(Restrictions.eq("inReplyTo", inReplyTo)).list();

    List<Reply> l = new ArrayList<Reply>(all.size());

    for (Reply a : all) {
      try {
        pep.checkAccess(RepliesPEP.GET_REPLY_INFO, a.getId());
        l.add(a);
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("no permission for viewing reply " + a.getId() +
                    " and therefore removed from list");
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
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  public Reply[] listAllReplies(final String root, final String inReplyTo)
                             throws OtmException, SecurityException {
    pep.checkAccess(RepliesPEP.LIST_ALL_REPLIES, URI.create(inReplyTo));

    List<ReplyThread> all;

    if (inReplyTo.equals(root))
      all = session.createCriteria(ReplyThread.class).add(Restrictions.eq("root", root)).list();
    else
      all = session.createCriteria(ReplyThread.class).add(Restrictions.eq("root", root)).
                                                      add(Restrictions.walk("inReplyTo",
                                                                            inReplyTo)).list();

    List<ReplyThread> l   = new ArrayList<ReplyThread>(all);
    for (ReplyThread a : all) {
      try {
        if (l.contains(a))
          pep.checkAccess(RepliesPEP.GET_REPLY_INFO, a.getId());
      } catch (SecurityException t) {
        remove(l, a);  // remove this thread
        if (log.isDebugEnabled())
          log.debug("no permission for viewing reply " + a.getId() +
                    " and therefore removed from list");
      }
    }

    return l.toArray(new Reply[l.size()]);
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
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  public Reply[] listReplies(final String mediator, final int state)
                          throws OtmException, SecurityException {
    pep.checkAccess(RepliesPEP.LIST_REPLIES_IN_STATE, AbstractSimplePEP.ANY_RESOURCE);

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

  @Transactional(rollbackFor = { Throwable.class })
  public void setPublicPermissions(final String id) throws OtmException, SecurityException {
    final String[] everyone = new String[]{Constants.Permission.ALL_PRINCIPALS};
    permissionsService.grant(id, new String[] { RepliesPEP.GET_REPLY_INFO }, everyone);
    permissionsService.revoke(id, new String[] { RepliesPEP.DELETE_REPLY }, everyone);
  }

  @Transactional(rollbackFor = { Throwable.class })
  public void cancelPublicPermissions(final String id) throws OtmException, SecurityException {
    final String[] everyone = new String[]{Constants.Permission.ALL_PRINCIPALS};
    permissionsService.cancelGrants(id, new String[] { RepliesPEP.GET_REPLY_INFO }, everyone);
    permissionsService.cancelRevokes(id, new String[] { RepliesPEP.DELETE_REPLY }, everyone);
  }

  /**
   * Set the default annotation type.
   * @param defaultType defaultType
   */
  public void setDefaultType(final String defaultType) {
    this.defaultType = defaultType;
  }

  /**
   * @return the default type for the annotation or reply
   */
  public String getDefaultType() {
    return defaultType;
  }
}
