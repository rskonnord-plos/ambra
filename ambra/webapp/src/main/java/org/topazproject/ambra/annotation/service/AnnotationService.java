/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import static org.topazproject.ambra.annotation.service.BaseAnnotation.FLAG_MASK;
import static org.topazproject.ambra.annotation.service.BaseAnnotation.PUBLIC_MASK;


import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.Constants;
import org.topazproject.ambra.annotation.Commentary;
import org.topazproject.ambra.annotation.FlagUtil;
import org.topazproject.ambra.models.Annotation;
import org.topazproject.ambra.models.Annotea;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.models.Correction;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.MinorCorrection;
import org.topazproject.ambra.models.Rating;
import org.topazproject.ambra.models.Reply;
import org.topazproject.ambra.permission.service.PermissionsService;
import org.topazproject.ambra.rating.service.RatingsService;
import org.topazproject.ambra.user.AmbraUser;
import org.topazproject.ambra.util.FileUtils;

/**
 * Used for both annotation and reply services.
 * Provides the Create/Read/Delete annotation operations .
 */
public class AnnotationService {
  /* TODO: Remove this entire layer of WebAnnotation and AnnotationService and reference the
   * ArticleAnnotationService directly!
   */
  private ArticleAnnotationService articleAnnotationService;
  private ReplyService replyService;
  private RatingsService ratingsService;

  private static final Log log = LogFactory.getLog(AnnotationService.class);
  private AnnotationConverter converter;
  private PermissionsService permissionsService;
  public static final String WEB_TYPE_RATING = "Rating";
  public static final String WEB_TYPE_COMMENT = "Comment";
  public static final String WEB_TYPE_NOTE = "Note";
  public static final String WEB_TYPE_FORMAL_CORRECTION = "FormalCorrection";
  public static final String WEB_TYPE_MINOR_CORRECTION = "MinorCorrection";
  public static final String WEB_TYPE_REPLY = "Reply";
  private static final Set<Class<? extends ArticleAnnotation>> CORRECTION_SET =
                                                new HashSet<Class<? extends ArticleAnnotation>>();
  private static final Set<Class<? extends ArticleAnnotation>> COMMENT_SET =
                                                new HashSet<Class<? extends ArticleAnnotation>>();
  static {
    CORRECTION_SET.add(Correction.class);
    COMMENT_SET.add(Comment.class);
  }

  /**
   * Create an annotation.
   *
   * @param target target that an annotation is being created for
   * @param context context
   * @param olderAnnotation olderAnnotation that the new one will supersede
   * @param title title
   * @param mimeType mimeType
   * @param body body
   * @param isPublic isPublic
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   * @return unique identifier for the newly created annotation
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String createAnnotation(final String target, final String context,
                                 final String olderAnnotation, final String title,
                                 final String mimeType, final String body, final boolean isPublic)
        throws ApplicationException {

    if (log.isDebugEnabled()) {
      log.debug("creating annotation for target: " + target + "; context: " + context +
                "; supercedes: " + olderAnnotation + "; title: " + title + "; mimeType: " +
                mimeType + "; body: " + body + "; isPublic: " + isPublic);
    }

    try {
      String annotationId = articleAnnotationService.createAnnotation(mimeType, target, context,
                                                                  olderAnnotation, title, body);

      if (log.isDebugEnabled()) {
        final AmbraUser user = AmbraUser.getCurrentUser();
        log.debug("Annotation created with ID: " + annotationId + " for user: " + user +
                  " for IP: " + ServletActionContext.getRequest().getRemoteAddr());
      }

      if (isPublic) {
        setAnnotationPublic(annotationId);
      }
      return annotationId;
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Create a reply
   *
   * @param root root
   * @param inReplyTo inReplyTo
   * @param title title
   * @param mimeType mimeType
   * @param body body
   * @throws ApplicationException ApplicationException
   * @return unique identifier for the newly created reply
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String createReply(final String root, final String inReplyTo, final String title,
                            final String mimeType, final String body)
        throws ApplicationException {
    try {
      String id = replyService.createReply(mimeType, root, inReplyTo, title, body);
      setReplyPublic(id);
      return id;
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Create a flag against an annotation or a reply
   *
   * @param target target that a flag is being created for
   * @param reasonCode reasonCode
   * @param body body
   * @param mimeType mimeType @throws org.topazproject.ambra.ApplicationException ApplicationException
   * @param isAnnotation true if annotation, false if reply
   * @return unique identifier for the newly created flag
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String createFlag(final String target, final String reasonCode, final String body,
                           final String mimeType, final boolean isAnnotation)
        throws ApplicationException {
    try {
      final String flagBody = FlagUtil.createFlagBody(reasonCode, body);
      final String flagId =
                articleAnnotationService.createFlagAnnotation(mimeType, target, flagBody, reasonCode);
      if (isAnnotation) {
        articleAnnotationService.setFlagged(target);
      } else {
        replyService.setFlagged(target);
      }
      return flagId;
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Create a flag against a Rating
   *
   * @param target target that a flag is being created for
   * @param reasonCode corresponds to the reason cited when "flagged" by the user
   * @param body body
   * @param mimeType mime-type
   * @return unique identifier for the newly created flag
   * @throws org.topazproject.ambra.ApplicationException
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String createRatingFlag(final String target, final String reasonCode, final String body,
                                 final String mimeType)
        throws ApplicationException {
    try {
      final String flagBody = FlagUtil.createFlagBody(reasonCode, body);
      final String flagId =
                  articleAnnotationService.createFlagAnnotation(mimeType, target, flagBody, reasonCode);
      ratingsService.setFlagged(target);
      return flagId;
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Unflag the given annotation
   *
   * @param annotationId annotationId
   * @throws ApplicationException ApplicationException
   */
  public void unflagAnnotation(final String annotationId) throws ApplicationException {
    try {
      articleAnnotationService.unflagAnnotation(annotationId);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Unflag the given reply
   *
   * @param replyId replyId
   * @throws ApplicationException ApplicationException
   */
  public void unflagReply(final String replyId) throws ApplicationException {
    try {
      replyService.unflagReply(replyId);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Unflag the given Rating
   *
   * @param ratingId the id of the Rating object for which a flag is to be removed
   * @throws ApplicationException
   */
  public void unflagRating(final String ratingId) throws ApplicationException {
    ratingsService.unflagRating(ratingId);
  }

  /**
   * Delete the given annotation along with/without the one it supercedes
   *
   * @param annotationId annotationId
   * @param deletePreceding deletePreceding
   * @throws ApplicationException ApplicationException
   */
  public void deletePrivateAnnotation(final String annotationId, final boolean deletePreceding)
        throws ApplicationException {
    try {
      articleAnnotationService.deletePrivateAnnotation(annotationId, deletePreceding);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Delete the given annotation along with/without the one it supercedes
   *
   * @param annotationId annotationId
   * @throws ApplicationException ApplicationException
   */
  public void deletePublicAnnotation(final String annotationId) throws ApplicationException {
    try {
      articleAnnotationService.deletePublicAnnotation(annotationId);
      //TODO: Set the access permissions for administrator only
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Delete replies with a given root and base reply
   *
   * @param root root
   * @param inReplyTo inReplyTo
   * @throws ApplicationException ApplicationException
   */
  public void deleteReply(final String root, final String inReplyTo) throws ApplicationException {
    try {
      replyService.deleteReplies(root, inReplyTo);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Delete the given flag
   *
   * @param flagId flagId
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  public void deleteFlag(final String flagId) throws ApplicationException {
    try {
      articleAnnotationService.deleteFlag(flagId);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * delete reply with id
   *
   * @param replyId replyId of the reply
   * @throws ApplicationException ApplicationException
   */
  public void deleteReply(final String replyId) throws ApplicationException {
    try {
      replyService.deleteReply(replyId);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Lists all annotations for the given target DOI. 
   *
   * @param target target of the annotation
   * @throws ApplicationException ApplicationException
   * @return a list of annotations
   */
  @Transactional(readOnly = true)
  public WebAnnotation[] listAnnotations(String target)  throws ApplicationException {
    return listAnnotations(target, null);
  }

  /**
   * Lists all correction annotations for the given target DOI. 
   * 
   * @param target
   * @return
   * @throws ApplicationException
   */
  @Transactional(readOnly = true)
  public WebAnnotation[] listCorrections(String target) throws ApplicationException {
    return listAnnotations(target, CORRECTION_SET);
  }

  /**
   * Lists all comment annotations for the given target DOI. 
   * 
   * @param target
   * @return
   * @throws ApplicationException
   */
  @Transactional(readOnly = true)
  public WebAnnotation[] listComments(String target) throws ApplicationException {
    return listAnnotations(target, COMMENT_SET);
  }

  /**
   * Retrieve all Annotation instances that annotate the given target DOI. If
   * annotationClassTypes is null, then all annotation types are retrieved. If annotationClassTypes
   * is not null, only the Annotation class types in the annotationClassTypes Set are returned. 
   * 
   * Each Class in annotationClassTypes should extend Annotation. E.G. Comment.class or
   * FormalCorrection.class
   *
   * @param target target doi that the listed annotations annotate
   * @param annotationClassTypes a set of Annotation class types to filter the results
   * @return a list of annotations
   */
  @Transactional(readOnly = true)
  public WebAnnotation[] listAnnotations(String target,
                                         Set<Class<? extends ArticleAnnotation>> annotationTypeClasses)
        throws ApplicationException {
    /* TODO: Remove this entire layer of WebAnnotation and AnnotationService and reference the
     * ArticleAnnotationService directly!
     */

    /* Placing the caching here rather than in ArticleAnnotationService because this
     * produces the objects for the Commentary view.  The Article HTML is cached
     * which calls the ArticleAnnotationService listAnnotations directly, so that call
     * won't happen too much.
     */
    WebAnnotation[] allAnnotations;
    ArticleAnnotation[] annotations;
    try {
      annotations = articleAnnotationService.listAnnotations(target, annotationTypeClasses);
    } catch (Exception re){
      throw new ApplicationException(re);
    }
    allAnnotations = converter.convert(annotations);
    return allAnnotations;
  }

  /**
   * List all the flags on all the undeleted annotations on the target.
   *
   * @param target target of the annotation
   * @throws ApplicationException ApplicationException
   * @return a list of undeleted flags
   */
  @Transactional(readOnly = true)
  public Flag[] listFlags(final String target) throws ApplicationException {
    final WebAnnotation[] annotations = listAnnotations(target);
    final Collection<Flag> flagList = new ArrayList<Flag>(annotations.length);
    for (final WebAnnotation annotation : annotations) {
      if (!annotation.isDeleted()) {
        flagList.add(new Flag(annotation));
      }
    }
    return flagList.toArray(new Flag[flagList.size()]);
  }

  public void setConverter(final AnnotationConverter converter) {
    this.converter = converter;
  }

  /**
   * List replies.
   *
   * @param root the discussion thread this resource is part of
   * @param inReplyTo the resource whose replies are to be listed
   * @throws ApplicationException ApplicationException
   * @return a list of replies
   */
  @Transactional(readOnly = true)
  public WebReply[] listReplies(final String root, final String inReplyTo)
        throws ApplicationException {
    try {
      final Reply[] replies = replyService.listReplies(root, inReplyTo);
      return converter.convert(replies);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Get a list of all replies
   *
   * @param root the discussion thread this resource is part of
   * @param inReplyTo the resource whose replies are to be listed
   * @throws ApplicationException ApplicationException
   * @return a list of all replies
   */
  @Transactional(readOnly = true)
  public WebReply[] listAllReplies(final String root, final String inReplyTo)
        throws ApplicationException {
    return listAllReplies(root, inReplyTo, null);
  }

  /**
   * Get a list of all replies in a flat array
   *
   * @param root the discussion thread this resource is part of
   * @param inReplyTo the resource whose replies are to be listed
   * @throws ApplicationException ApplicationException
   * @return a list of all replies
   */
  public Reply[] listAllRepliesFlattened(final String root, final String inReplyTo)
        throws ApplicationException {
    try {
      return replyService.listAllReplies(root, inReplyTo);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Get a list of all replies
   *
   * @param root the discussion thread this resource is part of
   * @param inReplyTo the resource whose replies are to be listed
   * @param com commentary
   * @throws ApplicationException ApplicationException
   * @return a list of all replies
   */
  @Transactional(readOnly = true)
  public WebReply[] listAllReplies(final String root, final String inReplyTo, final Commentary com)
                                throws ApplicationException {
    try {
      final Reply[] replies = replyService.listAllReplies(root, inReplyTo);
      return converter.convert(replies, com);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Get the specified annotation.
   *
   * @param annotationId annotationId
   * @throws ApplicationException ApplicationException
   * @return Annotation
   */
  @Transactional(readOnly = true)
  public WebAnnotation getAnnotation(final String annotationId) throws ApplicationException {
    try {
      final ArticleAnnotation annotation = articleAnnotationService.getAnnotation(annotationId);
      return converter.convert(annotation);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Get the specified reply.
   *
   * @param replyId replyId
   * @return the reply object
   * @throws ApplicationException ApplicationException
   */
  @Transactional(readOnly = true)
  public WebReply getReply(final String replyId) throws ApplicationException {
    try {
      final Reply reply = replyService.getReply(replyId);
      return converter.convert(reply);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Obtain a list of flagged ratings
   *
   * @return a list of flagged ratings
   * @throws ApplicationException
   */
  public Rating[] listFlaggedRatings() throws ApplicationException {
    return ratingsService.listRatings(null, FLAG_MASK | PUBLIC_MASK);
  }

  public void setArticleAnnotationService(final ArticleAnnotationService articleAnnotationService) {
    this.articleAnnotationService = articleAnnotationService;
  }

  public void setReplyService(final ReplyService replyService) {
    this.replyService = replyService;
  }

  public void setRatingsService(final RatingsService ratingsService) {
    this.ratingsService = ratingsService;
  }

  /**
   * Get the bodyUrl of the annotation.
   *
   * @param bodyUrl bodyUrl
   * @return content of the annotation
   * @throws ApplicationException ApplicationException
   */
  public String getBody(final String bodyUrl) throws ApplicationException {
    try {
      return FileUtils.getTextFromUrl(bodyUrl);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Set the annotation as public.
   *
   * @param annotationDoi annotationDoi
   * @throws ApplicationException ApplicationException
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void setAnnotationPublic(final String annotationDoi) throws ApplicationException {
    final String[] everyone = new String[]{Constants.Permission.ALL_PRINCIPALS};
    try {
      permissionsService.grant(
              annotationDoi,
              new String[]{
                      AnnotationsPEP.GET_ANNOTATION_INFO}, everyone);

      permissionsService.revoke(
              annotationDoi,
              new String[]{
                      AnnotationsPEP.DELETE_ANNOTATION,
                      AnnotationsPEP.SUPERSEDE}, everyone);

      articleAnnotationService.setPublic(annotationDoi);

    } catch (final Exception e) {
      throw new ApplicationException(e);
    }
  }

  @Transactional(rollbackFor = { Throwable.class })
  public void setReplyPublic(final String id) throws ApplicationException {
    final String[] everyone = new String[]{Constants.Permission.ALL_PRINCIPALS};
    try {
      permissionsService.grant(
              id,
              new String[]{
                      RepliesPEP.GET_REPLY_INFO}, everyone);

      permissionsService.revoke(
              id,
              new String[]{
                      RepliesPEP.DELETE_REPLY}, everyone);

    } catch (final Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Set the PermissionsService
   * @param permissionsService permissionsService
   */
  public void setPermissionsService(final PermissionsService permissionsService) {
    this.permissionsService = permissionsService;
  }

  /**
   * Convert the annotation with the given DOI to the annotation class type newAnnotationClassType. 
   * The existing annotation and the newAnnotationClassType must both implement ArticleAnnotation. 
   * 
   * @param targetId
   * @param newAnnotationClassType
   * @return
   * @throws Exception
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String convertArticleAnnotationToType(String targetId, Class newAnnotationClassType)
        throws Exception {
    String newAnnotationId = articleAnnotationService.
      convertArticleAnnotationToType(targetId, newAnnotationClassType);
    setAnnotationPublic(newAnnotationId);
    return newAnnotationId;
  }

  /**
   * Returns the PubApp type name for the given Annotea object. 
   * @param ann
   * @return
   */
  @Transactional(readOnly = true)
  public static String getWebType(Annotea ann) {
    if (ann == null || ann.getType() == null){
      return null;
    }

    if (ann.getType().equals(MinorCorrection.RDF_TYPE)) {
      return AnnotationService.WEB_TYPE_MINOR_CORRECTION;
    }
    if (ann.getType().equals(FormalCorrection.RDF_TYPE)) {
      return AnnotationService.WEB_TYPE_FORMAL_CORRECTION;
    }
    if (ann.getType().equals(Rating.RDF_TYPE)) {
      return AnnotationService.WEB_TYPE_RATING;
    }
    if (ann.getType().equals(org.topazproject.ambra.models.Reply.RDF_TYPE)) {
      return AnnotationService.WEB_TYPE_REPLY;
    }
    if (ann.getType().equals(Comment.RDF_TYPE)) {
      if (((Annotation)ann).getContext() != null) {
        return AnnotationService.WEB_TYPE_NOTE;
      } else {
        return AnnotationService.WEB_TYPE_COMMENT;
      }
    }

    log.error("Unable to determine annotation WEB_TYPE. Annotation ID='" + ann.getId() +
              "' ann.getType() = '" + ann.getType() + "'");
    return null;
  }
}
