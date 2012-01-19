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

package org.topazproject.ambra.admin.service;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.topazproject.ambra.annotation.service.WebAnnotation.FLAG_MASK;
import static org.topazproject.ambra.annotation.service.WebAnnotation.PUBLIC_MASK;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.annotation.service.AnnotationService;
import org.topazproject.ambra.annotation.service.ArticleAnnotationService;
import org.topazproject.ambra.annotation.service.Flag;
import org.topazproject.ambra.annotation.service.ReplyService;
import org.topazproject.ambra.models.Annotation;
import org.topazproject.ambra.models.Rating;
import org.topazproject.ambra.models.Reply;
import org.topazproject.ambra.user.service.UserService;
import org.topazproject.otm.Session;

/**
 * @author alan
 * Manage documents on server. Ingest and access ingested documents.
 */
public class FlagManagementService {

  private static final Log log = LogFactory.getLog(FlagManagementService.class);

  private Session session;

  private ArticleAnnotationService articleAnnotationService;
  private AnnotationService annotationService;
  private ReplyService replyService;
  private UserService userService;

  @Transactional(readOnly = true)
  public Collection<FlaggedCommentRecord> getFlaggedComments() throws RemoteException, ApplicationException {
    ArrayList<FlaggedCommentRecord> commentrecords = new ArrayList<FlaggedCommentRecord>();
    Annotation[] annotations;
    Reply[] replies;
    Flag flags[] = null;
    String creatorUserName;

    final Rating[] ratings = annotationService.listFlaggedRatings();
    annotations = articleAnnotationService.listAnnotations(null, FLAG_MASK | PUBLIC_MASK);
    replies = replyService.listReplies(null, FLAG_MASK); // Bug - not marked with public flag for now
    if (log.isDebugEnabled()) {
      log.debug("There are " + ratings.length + " ratings with flags");
      log.debug("There are " + annotations.length + " annotations with flags");
      for (Annotation annotation : annotations) {
        log.debug("\t"+ annotation.toString());
      }
      log.debug("There are " + replies.length + " replies with flags");
    }

    for (final Rating rating : ratings) {
      flags = annotationService.listFlags(rating.getId().toString());
      if (log.isDebugEnabled()) {
        log.debug("There are " + flags.length + " flags on rating: " + rating.getId());
      }

      // if Rating is marked as flagged, and no flag Comments could be found, data issue?
      if (flags.length == 0) {
        log.error("Rating " + rating.getId() + " is marked as flagged and no flag Comments exist.");

        // create a "dummy" record so it appears in admin interface
        FlaggedCommentRecord fcr =
          new FlaggedCommentRecord(
              "", // flag.getId(),
              rating.getId().toString(),
              rating.getBody().getCommentTitle(),
              "no Flag Comment exists, Rating indicates it is flagged, data issue?",
              "",
              "",
              "",
              null,
              "",
              AnnotationService.WEB_TYPE_RATING);
        commentrecords.add(fcr);

        // continue w/next Rating
        continue;
      }

      for (final Flag flag : flags) {
        if (flag.isDeleted()) {
          if (log.isDebugEnabled())
            log.debug("Flag: " + flag.getId() + " is deleted - skipping");
          continue;
        }

        try {
          creatorUserName = userService.getUsernameByTopazId(flag.getCreator());
        } catch (ApplicationException ae) { // Bug ?
          creatorUserName = "anonymous";
        }
        FlaggedCommentRecord fcr =
          new FlaggedCommentRecord(
              flag.getId(),
              flag.getAnnotates(),
              rating.getBody().getCommentTitle(),
              flag.getComment(),
              flag.getCreated(),
              creatorUserName,
              flag.getCreator(),
              null,
              flag.getReasonCode(),
              AnnotationService.WEB_TYPE_RATING);
        commentrecords.add(fcr);
      }
    }

    /*
     * Add a FlaggedCommentRecord for each flag reported against each flagged annotation
     */
    for (final Annotation flaggedAnnotation : annotations) {
      flags = annotationService.listFlags((String) flaggedAnnotation.getId().toString());
      if (log.isDebugEnabled())
        log.debug("There are " + flags.length + " flags on annotation: " + flaggedAnnotation.getId());
      for (Flag flag : flags) {
        if (flag.isDeleted()) {
          if (log.isDebugEnabled())
            log.debug("Flag: " + flag.getId() + " is deleted - skipping");
          continue;
        }
        try {
          creatorUserName = userService.getUsernameByTopazId(flag.getCreator());
        } catch (ApplicationException ae) { // Bug ?
          creatorUserName = "anonymous";
        }

        FlaggedCommentRecord fcr =
          new FlaggedCommentRecord(
              flag.getId(),
              flag.getAnnotates(),
              flaggedAnnotation.getTitle(),
              flag.getComment(),
              flag.getCreated(),
              creatorUserName,
              flag.getCreator(),
              null,
              flag.getReasonCode(),
              AnnotationService.getWebType(flaggedAnnotation));
        commentrecords.add(fcr);
      }
    }

    for (Reply reply : replies) {
      flags = annotationService.listFlags(reply.getId().toString());
      if (log.isDebugEnabled())
        log.debug("There are " + flags.length + " flags on reply: " + reply.getId());
      for (Flag flag : flags) {
        if (flag.isDeleted()) {
          if (log.isDebugEnabled())
            log.debug("Flag: " + flag.getId() + " is deleted - skipping");
          continue;
        }
        try {
          creatorUserName = userService.getUsernameByTopazId(flag
              .getCreator());
        } catch (ApplicationException ae) {
          creatorUserName = "anonymous";
        }
        FlaggedCommentRecord fcr =
          new FlaggedCommentRecord(
              flag.getId(),
              reply.getId().toString(),
              reply.getTitle(),
              flag.getComment(),
              flag.getCreated(),
              creatorUserName,
              flag.getCreator(),
              reply.getRoot(),
              flag.getReasonCode(),
              AnnotationService.WEB_TYPE_REPLY);
        commentrecords.add(fcr);
      }
    }
    Collections.sort(commentrecords);
    return commentrecords;
  }

  public void setArticleAnnotationService(
      ArticleAnnotationService articleAnnotationService) {
    this.articleAnnotationService = articleAnnotationService;
  }

  protected ArticleAnnotationService getArticleAnnotationService() {
    return articleAnnotationService;
  }

  public void setReplyService(ReplyService replyService) {
    this.replyService = replyService;
  }

  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
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

  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  public AnnotationService getAnnotationService() {
    return annotationService;
  }
}
