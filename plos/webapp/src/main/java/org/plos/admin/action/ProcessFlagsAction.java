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

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.annotation.service.AnnotationService;
import org.plos.annotation.service.AnnotationWebService;
import org.plos.annotation.service.Flag;
import org.plos.annotation.service.ReplyWebService;
import org.plos.annotation.service.ReplyInfo;
import org.plos.models.Comment;
import org.plos.models.FormalCorrection;
import org.plos.models.MinorCorrection;
import org.plos.rating.service.RatingsService;

public class ProcessFlagsAction extends BaseAdminActionSupport {

  private static final Log log = LogFactory.getLog(ProcessFlagsAction.class);
  private String[] commentsToUnflag;
  private String[] commentsToDelete;
  private String[] convertToFormalCorrection;
  private String[] convertToMinorCorrection;
  private String[] convertToNote;
  private AnnotationService annotationService;
  private RatingsService ratingsService;
  private ReplyWebService replyWebService;


  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  /**
   * Set the RatingsService.
   *
   * For Spring wiring.
   *
   * @param ratingService RatingService.
   */
  public void setRatingsService(RatingsService ratingsService) {
    this.ratingsService = ratingsService;
  }

  public void setCommentsToUnflag(String[] comments) {
    commentsToUnflag = comments;
  }

  public void setCommentsToDelete(String[] comments) {
    commentsToDelete = comments;
  }

  public void setConvertToFormalCorrection(String[] convertToFormalCorrection) {
    this.convertToFormalCorrection = convertToFormalCorrection;
  }

  public void setConvertToMinorCorrection(String[] convertToMinorCorrection) {
    this.convertToMinorCorrection = convertToMinorCorrection;
  }

  public void setConvertToNote(String[] convertToNote) {
    this.convertToNote = convertToNote;
  }

  /**
   * Process the checked action items on the Flag Annotations displayed in the admin page. If an error occurs when 
   * processing a checked item, the exception is logged and an action error is added to be displayed on the admin
   * console. We continue to attempt to process the other checked items. The same admin console will be displayed
   * regardless of error, and the errors will be displayed in the console at the top of the page. 
   */
  public String execute() {
    if (commentsToUnflag != null){
      for (String toUnFlag : commentsToUnflag){
        if (log.isDebugEnabled()){
          log.debug("Found comment to unflag: " + toUnFlag);
        }
        String[] tokens = toUnFlag.split("_");
        // token[0] = the flag ID
        // token[1] = the target ID
        // token[2] = the targetType (a string identifier)
        try {
          deleteFlag(tokens[0], tokens[1], tokens[2]);
        } catch(Exception e) {
          String errorMessage = "Failed to delete flag id='" + tokens[0] + "'" + 
          "for annotation id='" + tokens[1] + "'";
          addActionError(errorMessage + " Exception: " +e.getMessage());
          log.error(errorMessage, e);
        }
      }
    }

    if (commentsToDelete != null) {
      for (String toDelete : commentsToDelete) {
        if (log.isDebugEnabled()) {
          log.debug("Found comment to delete: " + toDelete);
        }
        String[] tokens = toDelete.split("_");
        try {
          deleteTarget(tokens[0], tokens[1], tokens[2]);
        } catch (Exception e) {
          String errorMessage = "Failed to delete annotation id='" + tokens[1] + "to Formal Correction annotation."; 
          addActionError(errorMessage + " Exception: " +e.getMessage());
          log.error(errorMessage, e);
        }
      }
    }
    
    
    if (convertToFormalCorrection != null) {
      for (String paramStr : convertToFormalCorrection) {
        if (log.isDebugEnabled()) {
          log.debug("Converting to Formal Correction: "+paramStr);
        }
        String[] tokens = paramStr.split("_");
        try {
          annotationService.convertArticleAnnotationToType(tokens[1], FormalCorrection.class);
          annotationService.deleteFlag(tokens[0]);
        } catch(Exception e) {
          String errorMessage = "Failed to convert annotation id='" + tokens[1] + "to Formal Correction annotation."; 
          addActionError(errorMessage + " Exception: " +e.getMessage());
          log.error(errorMessage, e);
        }
      }
    }
    
    if (convertToMinorCorrection != null) {
      for (String paramStr : convertToMinorCorrection) {
        if (log.isDebugEnabled()) {
          log.debug("Converting to Minor Correction: "+paramStr);
        }
        String[] tokens = paramStr.split("_");
        try {
          annotationService.convertArticleAnnotationToType(tokens[1], MinorCorrection.class);
          annotationService.deleteFlag(tokens[0]);
        } catch(Exception e) {
          String errorMessage = "Failed to convert annotation id='" + tokens[1] + "to Minor Correction annotation."; 
          addActionError(errorMessage + " Exception: " +e.getMessage());
          log.error(errorMessage, e);
        }  
      }
    }
    
    if (convertToNote != null) {
      for (String paramStr : convertToNote) {
        if (log.isDebugEnabled()) {
          log.debug("Converting to Note: "+paramStr);
        }
        String[] tokens = paramStr.split("_");
        try {
          annotationService.convertArticleAnnotationToType(tokens[1], Comment.class);
          annotationService.deleteFlag(tokens[0]);
        } catch(Exception e) {
          String errorMessage = "Failed to convert annotation id='" + tokens[1] + "to Note annotation."; 
          addActionError(errorMessage + " Exception: " +e.getMessage());
          log.error(errorMessage, e);
        }
      }
    }
    
    return base();
  }

  /**
   * @param root
   * @param target
   * @param targetType Type, Class name, of target
   *
   * Delete the target. Root is either an id (in which case
   * target is a reply) or else it is a "" in which case target is an annotation.
   * In either case:
   *         remove flags on this target
   *         get all replies to this target
   *             for each reply
   *                 check to see if reply has flags
   *                     remove each flag
   *         remove all replies in bulk
   *         remove target
   *
   *  Note that because this may be called from a 'batch' job (i.e multiple
   *  rows were checked off in process flags UI) it may be that things have
   *  been deleted by previous actions. So we catch 'non-exsietent-id' exceptions
   *  which may well get thrown and recover and continue (faster than going across
   *  teh web service tro see if the ID is valid then going out again to get its object).
   * @throws ApplicationException
   * @throws ApplicationException
   * @throws NoSuchAnnotationIdException
   * @throws RemoteException
   * @throws NoSuchAnnotationIdException
   * @throws RemoteException
   */
  private void deleteTarget(String root, String target, String targetType) throws ApplicationException, RemoteException {
    ReplyInfo[] replies;
    Flag[] flags = annotationService.listFlags(target);

    if (log.isDebugEnabled()) {
      log.debug("Deleting Target" + target + " Root is " + root);
      log.debug(target + " has " + flags.length + " flags - deleting them");
    }
    for (Flag flag: flags) {
      try {
        deleteFlag(target, flag.getId(), targetType);
      } catch (Exception e) {
        //keep going through the list even if there is an exception
        if (log.isWarnEnabled()) {
          log.warn("Couldn't delete flag: " + flag.getId(), e);
        }
      }
    }

    if (targetType.equals(AnnotationService.WEB_TYPE_REPLY)) {
      replies = annotationService.listAllRepliesFlattened(root, target);
    } else if (
        targetType.equals(AnnotationService.WEB_TYPE_COMMENT) || 
        targetType.equals(AnnotationService.WEB_TYPE_NOTE) ||
        targetType.equals(AnnotationService.WEB_TYPE_MINOR_CORRECTION) ||
        targetType.equals(AnnotationService.WEB_TYPE_FORMAL_CORRECTION)) {
      replies = annotationService.listAllRepliesFlattened(target, target);
    } else {
      // Flag type doesn't have Replies
      replies = new ReplyInfo[0];
    }

    if (log.isDebugEnabled()) {
      log.debug(target + " has " + replies.length + " replies. Removing their flags");
    }
    for (ReplyInfo reply : replies) {
      Flag[] replyFlags = annotationService.listFlags(reply.getId());
      if (log.isDebugEnabled()) {
        log.debug("Reply " + reply.getId() + " has " + replyFlags.length + " flags");
      }
      for (Flag flag: replyFlags) {
        if (flag.isDeleted())
          continue;
        deleteFlag(reply.getId(), flag.getId(), AnnotationService.WEB_TYPE_REPLY);
      }
    }

    if (targetType.equals(AnnotationService.WEB_TYPE_REPLY)) {
      replyWebService.deleteReplies(target); // Bulk delete
      //annotationService.deleteReply(target);
      if (log.isDebugEnabled()) {
        log.debug("Deleted reply: " + target);
      }
    } else if (
        targetType.equals(AnnotationService.WEB_TYPE_COMMENT) || 
        targetType.equals(AnnotationService.WEB_TYPE_NOTE) ||
        targetType.equals(AnnotationService.WEB_TYPE_MINOR_CORRECTION) ||
        targetType.equals(AnnotationService.WEB_TYPE_FORMAL_CORRECTION)) {
      replyWebService.deleteReplies(target, target);
      annotationService.deletePublicAnnotation(target);
      if (log.isDebugEnabled()) {
        log.debug("Deleted annotation: " + target);
      }
    } else if (targetType.equals(AnnotationService.WEB_TYPE_RATING)) {
      ratingsService.deleteRating(target);
      if (log.isDebugEnabled()) {
        log.debug("Deleted Rating: " + target);
      }
    }
  }

  // TODO: It is redundant (and error prone) to presume the caller of this action has provided the correct
  // target or targetType. This information should be retrieved from the flag by the service. 
  private void deleteFlag(String target, String flag, String targetType) throws ApplicationException {
    // Delete flag
    annotationService.deleteFlag(flag);
    // TODO: The Action shouldn't have to worry about this kind of housekeeping. Move this to the service! 
    // Deal with 'flagged' status
    Flag[] flags = annotationService.listFlags(target);
    if (log.isDebugEnabled()) {
      log.debug("Deleting flag: " + flag + " on target: " + target);
      log.debug("Checking for flags on target: " + target + ". There are " + flags.length + " flags remaining");
    }
    if (flags.length == 0) {
      if (log.isDebugEnabled()) {
        log.debug("Setting status to unflagged");
      }
      if (targetType.equals(AnnotationService.WEB_TYPE_NOTE) || 
          targetType.equals(AnnotationService.WEB_TYPE_COMMENT) ||
          targetType.equals(AnnotationService.WEB_TYPE_MINOR_CORRECTION) || 
          targetType.equals(AnnotationService.WEB_TYPE_FORMAL_CORRECTION)) {
        annotationService.unflagAnnotation(target);
      } else if (targetType.equals(AnnotationService.WEB_TYPE_RATING)) {
        ratingsService.unflagRating(target);
      } else if (targetType.equals(AnnotationService.WEB_TYPE_REPLY)) {
        annotationService.unflagReply(target);
      } else {
        String msg = target + " cannot be unFlagged - not annotation, rating or reply";
        log.error(msg);
        throw new ApplicationException(msg);
      }
    } else {
      log.debug("Flags exist. Target will remain marked as flagged");
    }
  }

  public void setReplyWebService(ReplyWebService replyWebService) {
    this.replyWebService = replyWebService;
  }

}
