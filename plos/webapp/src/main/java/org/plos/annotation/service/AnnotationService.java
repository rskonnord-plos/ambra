/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service;

import static org.plos.annotation.service.BaseAnnotation.FLAG_MASK;
import static org.plos.annotation.service.BaseAnnotation.PUBLIC_MASK;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.plos.ApplicationException;
import org.plos.Constants;
import org.plos.annotation.Commentary;
import org.plos.annotation.FlagUtil;
import org.plos.models.Annotation;
import org.plos.models.Annotea;
import org.plos.models.Comment;
import org.plos.models.Correction;
import org.plos.models.FormalCorrection;
import org.plos.models.MinorCorrection;
import org.plos.models.Rating;
import org.plos.permission.service.PermissionWebService;
import org.plos.rating.service.RatingInfo;
import org.plos.rating.service.RatingsService;
import org.plos.user.PlosOneUser;
import org.plos.util.FileUtils;

/**
 * Used for both annotation and reply services.
 * Provides the Create/Read/Delete annotation operations .
 */
public class AnnotationService {
  // TODO: Remove this entire layer of WebAnnotation and AnnotationService and reference the AnnotationWebService directly!
  private AnnotationWebService annotationWebService;
  private ReplyWebService replyWebService;
  private RatingsService ratingsService;

  private static final Log log = LogFactory.getLog(AnnotationService.class);
  private AnnotationConverter converter;
  private PermissionWebService permissionWebService;
  public static final String WEB_TYPE_RATING = "Rating";
  public static final String WEB_TYPE_COMMENT = "Comment";
  public static final String WEB_TYPE_NOTE = "Note";
  public static final String WEB_TYPE_FORMAL_CORRECTION = "FormalCorrection";
  public static final String WEB_TYPE_MINOR_CORRECTION = "MinorCorrection";
  public static final String WEB_TYPE_REPLY = "Reply";
  private static final Set<Class<? extends Annotation>> CORRECTION_SET = new HashSet<Class<? extends Annotation>>();
  private static final Set<Class<? extends Annotation>> COMMENT_SET = new HashSet<Class<? extends Annotation>>();
  static {
    CORRECTION_SET.add(Correction.class);
    COMMENT_SET.add(Comment.class);
  }

  /**
   * Create an annotation.
   * @param target target that an annotation is being created for
   * @param context context
   * @param olderAnnotation olderAnnotation that the new one will supersede
   * @param title title
   * @param mimeType mimeType
   * @param body body
   * @param isPublic isPublic
   * @throws org.plos.ApplicationException ApplicationException
   * @return unique identifier for the newly created annotation
   */
  public String createAnnotation(final String target, final String context, final String olderAnnotation,
      final String title, final String mimeType, final String body, final boolean isPublic) throws ApplicationException {

    if (log.isDebugEnabled()) {
      StringBuilder debugMsg = new StringBuilder("creating annotation for target: ");
      debugMsg.append(target).append("; context: ").append(context).append("; supercedes: ");
      debugMsg.append(olderAnnotation).append("; title: " ).append(title).append("; mimeType: ");
      debugMsg.append(mimeType).append("; body: ").append(body).append("; isPublic: ").append(isPublic);
      log.debug(debugMsg);
    }

    try {
      final String annotationId = annotationWebService.createAnnotation(mimeType, target, context, olderAnnotation, title, body);

      if (log.isDebugEnabled()) {
        final PlosOneUser user = PlosOneUser.getCurrentUser();
        log.debug("Annotation created with ID: " + annotationId + " for user: " + ((user == null) ? "null" : user.getUserId()) + " for IP: " +
                  ServletActionContext.getRequest().getRemoteAddr());
      }

      if (isPublic) {
        setAnnotationPublic(annotationId);
      }
      return annotationId;
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (UnsupportedEncodingException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Create a reply
   * @param root root
   * @param inReplyTo inReplyTo
   * @param title title
   * @param mimeType mimeType
   * @param body body
   * @throws ApplicationException ApplicationException
   * @return unique identifier for the newly created reply
   */
  public String createReply(final String root, final String inReplyTo, final String title, final String mimeType, final String body) throws ApplicationException {
    try {
      String id = replyWebService.createReply(mimeType, root, inReplyTo, title, body, this);
      setReplyPublic(id);
      return id;
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    } catch (UnsupportedEncodingException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Create a flag against an annotation or a reply
   * @param target target that a flag is being created for
   * @param reasonCode reasonCode
   * @param body body
   * @param mimeType mimeType @throws org.plos.ApplicationException ApplicationException
   * @param isAnnotation true if annotation, false if reply
   * @return unique identifier for the newly created flag
   * @throws org.plos.ApplicationException ApplicationException
   */
  public String createFlag(final String target, final String reasonCode, final String body, final String mimeType, final boolean isAnnotation) throws ApplicationException {
    try {
      final String flagBody = FlagUtil.createFlagBody(reasonCode, body);
      final String flagId = annotationWebService.createFlagAnnotation(mimeType, target, flagBody, reasonCode);
      if (isAnnotation) {
        annotationWebService.setFlagged(target);
      } else {
        replyWebService.setFlagged(target);
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
   * @throws org.plos.ApplicationException
   */
  public String createRatingFlag(final String target, final String reasonCode, final String body, final String mimeType) throws ApplicationException {
    try {
      final String flagBody = FlagUtil.createFlagBody(reasonCode, body);
      final String flagId = annotationWebService.createFlagAnnotation(mimeType, target, flagBody, reasonCode);
      ratingsService.setFlagged(target);
      return flagId;
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Unflag the given annotation
   * @param annotationId annotationId
   * @throws ApplicationException ApplicationException
   */
  public void unflagAnnotation(final String annotationId) throws ApplicationException {
    try {
      annotationWebService.unflagAnnotation(annotationId);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Unflag the given reply
   * @param replyId replyId
   * @throws ApplicationException ApplicationException
   */
  public void unflagReply(final String replyId) throws ApplicationException {
    try {
      replyWebService.unflagReply(replyId);
    } catch (RemoteException e) {
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
   * @param annotationId annotationId
   * @param deletePreceding deletePreceding
   * @throws ApplicationException ApplicationException
   */
  public void deletePrivateAnnotation(final String annotationId, final boolean deletePreceding) throws ApplicationException {
    try {
      annotationWebService.deletePrivateAnnotation(annotationId, deletePreceding);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Delete the given annotation along with/without the one it supercedes
   * @param annotationId annotationId
   * @throws ApplicationException ApplicationException
   */
  public void deletePublicAnnotation(final String annotationId) throws ApplicationException {
    try {
      annotationWebService.deletePublicAnnotation(annotationId);
      //TODO: Set the access permissions for administrator only
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Delete replies with a given root and base reply
   * @param root root
   * @param inReplyTo inReplyTo
   * @throws ApplicationException ApplicationException
   */
  public void deleteReply(final String root, final String inReplyTo) throws ApplicationException {
    try {
      replyWebService.deleteReplies(root, inReplyTo);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Delete the given flag
   * @param flagId flagId
   * @throws org.plos.ApplicationException ApplicationException
   */
  public void deleteFlag(final String flagId) throws ApplicationException {
    try {
      annotationWebService.deleteFlag(flagId);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * delete reply with id
   * @param replyId replyId of the reply
   * @throws ApplicationException ApplicationException
   */
  public void deleteReply(final String replyId) throws ApplicationException {
    try {
      replyWebService.deleteReply(replyId);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @param target target of the annotation
   * @throws ApplicationException ApplicationException
   * @return a list of annotations
   */
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
  public WebAnnotation[] listComments(String target) throws ApplicationException {
    return listAnnotations(target, COMMENT_SET);
  }
  
  /**
   * Retrieve all AnnotationInfo instances that annotate the given target DOI. If annotationClassTypes is null, then all 
   * annotation types are retrieved. If annotationClassTypes is not null, only the Annotation class types in the 
   * annotationClassTypes Set are returned. 
   * 
   * Each Class in annotationClassTypes should extend Annotation. E.G. Comment.class or FormalCorrection.class
   *
   * @param target target doi that the listed annotations annotate
   * @param annotationClassTypes a set of Annotation class types to filter the results
   *
   * @return a list of annotations
   *
   * @throws RemoteException RemoteException
   */
  public WebAnnotation[] listAnnotations(String target, Set<Class<? extends Annotation>> annotationTypeClasses) throws ApplicationException {
    // TODO: Remove this entire layer of WebAnnotation and AnnotationService and reference the AnnotationWebService directly!
    
    /** Placing the caching here rather than in AnnotationWebService because this
     *  produces the objects for the Commentary view.  The Article HTML is cached
     *  which calls the AnnotationWebService listAnnotations directly, so that call
     *  won't happen too much.
     */
    WebAnnotation[] allAnnotations;
    AnnotationInfo[] annotations;
    try {
      annotations = annotationWebService.listAnnotations(target, annotationTypeClasses);
    } catch (RemoteException re){
      log.error(re);
      throw new ApplicationException(re);
    }
    allAnnotations = converter.convert(annotations);
    return allAnnotations;
  }

  /**
   * @param target target of the annotation
   * @throws ApplicationException ApplicationException
   * @return a list of undeleted flags
   */
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
   * @param root the discussion thread this resource is part of
   * @param inReplyTo the resource whose replies are to be listed
   * @throws ApplicationException ApplicationException
   * @return a list of replies
   */
  public Reply[] listReplies(final String root, final String inReplyTo) throws ApplicationException {
    try {
      final ReplyInfo[] replies = replyWebService.listReplies(root, inReplyTo);
      return converter.convert(replies);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Get a list of all replies
   * @param root the discussion thread this resource is part of
   * @param inReplyTo the resource whose replies are to be listed
   * @throws ApplicationException ApplicationException
   * @return a list of all replies
   */
  public Reply[] listAllReplies(final String root, final String inReplyTo)throws ApplicationException {
    return listAllReplies(root, inReplyTo, null);
  }

  /**
   * Get a list of all replies in a flat array
   * @param root the discussion thread this resource is part of
   * @param inReplyTo the resource whose replies are to be listed
   * @throws ApplicationException ApplicationException
   * @return a list of all replies
   */
  public ReplyInfo[] listAllRepliesFlattened(final String root, final String inReplyTo)throws ApplicationException {
    try {
      return replyWebService.listAllReplies(root, inReplyTo);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Get a list of all replies
   * @param root the discussion thread this resource is part of
   * @param inReplyTo the resource whose replies are to be listed
   * @param com commentary
   * @throws ApplicationException ApplicationException
   * @return a list of all replies
   */
  public Reply[] listAllReplies(final String root, final String inReplyTo, final Commentary com)
                                throws ApplicationException {
    try {
      final ReplyInfo[] replies = replyWebService.listAllReplies(root, inReplyTo);
      return converter.convert(replies, com);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * @param annotationId annotationId
   * @throws ApplicationException ApplicationException
   * @return Annotation
   */
  public WebAnnotation getAnnotation(final String annotationId) throws ApplicationException {
    try {
      final AnnotationInfo annotation = annotationWebService.getAnnotation(annotationId);
      return converter.convert(annotation);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Get reply
   * @param replyId replyId
   * @return the reply object
   * @throws ApplicationException ApplicationException
   */
  public Reply getReply(final String replyId) throws ApplicationException {
    try {
      final ReplyInfo reply = replyWebService.getReplyInfo(replyId);
      return converter.convert(reply);
    } catch (RemoteException e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Obtain a list of flagged ratings
   *
   * @return a list of flagged ratings
   * @throws ApplicationException
   */
  public RatingInfo[] listFlaggedRatings() throws ApplicationException {

    return ratingsService.listRatings(null, FLAG_MASK | PUBLIC_MASK);
  }

  public void setAnnotationWebService(final AnnotationWebService annotationWebService) {
    this.annotationWebService = annotationWebService;
  }

  public void setReplyWebService(final ReplyWebService replyWebService) {
    this.replyWebService = replyWebService;
  }

  public void setRatingsService(final RatingsService ratingsService) {
    this.ratingsService = ratingsService;
  }

  /**
   * Get the bodyUrl of the annotation.
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
   * @param annotationDoi annotationDoi
   * @throws ApplicationException ApplicationException
   */
  public void setAnnotationPublic(final String annotationDoi) throws ApplicationException {
    final String[] everyone = new String[]{Constants.Permission.ALL_PRINCIPALS};
    try {
      permissionWebService.grant(
              annotationDoi,
              new String[]{
                      AnnotationsPEP.GET_ANNOTATION_INFO}, everyone);

      permissionWebService.revoke(
              annotationDoi,
              new String[]{
                      AnnotationsPEP.DELETE_ANNOTATION,
                      AnnotationsPEP.SUPERSEDE});

      annotationWebService.setPublic(annotationDoi);

    } catch (final Exception e) {
      throw new ApplicationException(e);
    }
  }

  public void setReplyPublic(final String id) throws ApplicationException {
    final String[] everyone = new String[]{Constants.Permission.ALL_PRINCIPALS};
    try {
      permissionWebService.grant(
              id,
              new String[]{
                      RepliesPEP.GET_REPLY_INFO}, everyone);

      permissionWebService.revoke(
              id,
              new String[]{
                      RepliesPEP.DELETE_REPLY});

    } catch (final Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Set the PermissionWebService
   * @param permissionWebService permissionWebService
   */

  /**
   * Set the PermissionWebService
   * @param permissionWebService permissionWebService
   */
  public void setPermissionWebService(final PermissionWebService permissionWebService) {
    this.permissionWebService = permissionWebService;
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
  public String convertArticleAnnotationToType(String targetId, Class newAnnotationClassType) 
  throws Exception {
    String newAnnotationId = annotationWebService.
      convertArticleAnnotationToType(targetId, newAnnotationClassType);
    setAnnotationPublic(newAnnotationId);
    return newAnnotationId;
  }

  /**
   * Returns the PubApp type name for the given Annotea object. 
   * @param ann
   * @return
   */
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
    if (ann.getType().equals(org.plos.models.Reply.RDF_TYPE)) {
      return AnnotationService.WEB_TYPE_REPLY;
    }
    if (ann.getType().equals(Comment.RDF_TYPE)) {
      if (((Annotation)ann).getContext() != null) {
        return AnnotationService.WEB_TYPE_NOTE;
      } else {
        return AnnotationService.WEB_TYPE_COMMENT;
      }
    }
    
    log.error("Unable to determine annotation WEB_TYPE. Annotation ID='"+ann.getId()+"' ann.getType() = '"+ann.getType()+"'");
    return null;
  }
}
