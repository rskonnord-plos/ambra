/* $HeadURL::                                                                            $
 * $Id:CreateAnnotationAction.java 722 2006-10-02 16:42:45Z viru $
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.action;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.plos.ApplicationException;
import org.plos.util.ProfanityCheckingService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Action to create an annotation. It also does profanity validation on the user content.
 */
public class CreateAnnotationAction extends AnnotationActionSupport {
  private String target;
  private String commentTitle;
  private String comment;
  private String mimeType = "text/plain";
  private String annotationId;
  private boolean isPublic = false;
  private String startPath;
  private int startOffset;
  private String endPath;
  private int endOffset;
  private String supercedes;

  private ProfanityCheckingService profanityCheckingService;
  private static final Log log = LogFactory.getLog(CreateAnnotationAction.class);

  /**
   * {@inheritDoc}
   * Also does some profanity check for commentTitle and comment before creating the annotation.
   */
  public String execute() throws Exception {
    return createAnnotation();
  }

  /**
   * Save the discussion
   * @return webwork status
   * @throws Exception Exception
   */
  public String executeSaveDiscussion() throws Exception {
    return createAnnotation();
  }

  private String createAnnotation() {
    if (isInvalid()) return INPUT;

    try {
      final List<String> profaneWordsInTitle = profanityCheckingService.validate(commentTitle);
      final List<String> profaneWordsInBody = profanityCheckingService.validate(comment);

      if (profaneWordsInBody.isEmpty() && profaneWordsInTitle.isEmpty()) {
        annotationId = getAnnotationService().createAnnotation(target, getTargetContext(), supercedes, commentTitle, mimeType, comment, isPublic);
        if (log.isDebugEnabled()) {
          log.debug("CreateAnnotationAction called and annotation created with id: " + annotationId);
        }
      } else {
        addProfaneMessages(profaneWordsInBody, "comment", "comment");
        addProfaneMessages(profaneWordsInTitle, "commentTitle", "title");
        return INPUT;
      }
    } catch (final ApplicationException e) {
      log.error("Could not create annotation", e);
      addActionError("Annotation creation failed with error message: " + e.getMessage());
      return ERROR;
    }
    addActionMessage("Annotation created with id:" + annotationId);
    return SUCCESS;
  }

  private boolean isInvalid() {
    boolean invalid = false;
    if (isPublic && StringUtils.isEmpty(commentTitle)) {
      addFieldError("commentTitle", "A title is required.");
      invalid = true;
    }

    if (StringUtils.isEmpty(comment)) {
      addFieldError("comment", "You must say something in your comment");
      invalid = true;
    }
    return invalid;
  }

  /**
   * Set the target that it annotates.
   * @param target target
   */
  public void setTarget(final String target) {
    this.target = target;
  }

  /**
   * Set the commentTitle of the annotation
   * @param commentTitle commentTitle
   */
  public void setCommentTitle(final String commentTitle) {
    this.commentTitle = commentTitle;
  }

  /**
   * Set the comment of the annotation
   * @param comment comment
   */
  public void setComment(final String comment) {
    this.comment = comment;
  }

  /**
   * Set the mimeType of the annotation
   * @param mimeType mimeType
   */
  public void setMimeType(final String mimeType) {
    this.mimeType = mimeType;
  }

  /**
   * Get the id of the newly created annotation
   * @return annotation id
   */
  public String getAnnotationId() {
    return annotationId;
  }

  /**
   * @return the target
   */
  @RequiredStringValidator(message="You must specify the target that this annotation is applied on")
  public String getTarget() {
    return target;
  }

  /**
   * Returning an xpointer of the following form:
   * 1) string-range(/doc/chapter/title,'')[5]/range-to(string-range(/doc/chapter/para/em,'')[3])
   * 2) string-range(/article[1]/body[1]/sec[1]/p[2],"",194,344)
   * @return the context for the annotation
   * @throws org.plos.ApplicationException ApplicationException
   */
  public String getTargetContext() throws ApplicationException {
    if (StringUtils.isBlank(startPath)) {
      return null;
    }

    try {
      String context;
      if (startPath.equals(endPath)) {
        final int length = endOffset - startOffset;
        if (length < 0) {
          final String errorMessage = "Invalid length: " + length + " of the annotated content";
          addFieldError("endOffset", errorMessage);
          throw new ApplicationException(errorMessage);
        }
        context = createStringRangeFragment(startPath, startOffset, length);
      } else {
        context = createStringRangeFragment(startPath, startOffset) +
                "/range-to(" + createStringRangeFragment(endPath, endOffset) + ")";
      }
      log.debug("xpointer fragment =" + context);
      return target + "#xpointer(" + URLEncoder.encode(context, "UTF-8") + ")";
    } catch (final UnsupportedEncodingException e) {
      log.error(e);
      throw new ApplicationException(e);
    }
  }

  private static String createStringRangeFragment(final String path, final int offset, final int length) {
    return "string-range(" + path + ", '', " + offset + ", " + length + ")[1]";
  }

  private static String createStringRangeFragment(final String path, final int offset) {
    return "string-range(" + path + ", '')[" + offset + "]";
  }

  /**
   * @return the commentTitle
   */
  public String getCommentTitle() {
    return commentTitle;
  }

  /**
   * @return the annotation content
   */
  public String getComment() {
    return comment;
  }

  /**
   * @return the mime type
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Set the profanityCheckingService
   * @param profanityCheckingService profanityCheckingService
   */
  public void setProfanityCheckingService(final ProfanityCheckingService profanityCheckingService) {
    this.profanityCheckingService = profanityCheckingService;
  }

  /** @param isPublic set the visibility of annotation */
  public void setIsPublic(final boolean isPublic) {
    this.isPublic = isPublic;
  }

  /** @return whether the annotation is public */
  public boolean getIsPublic() {
    return isPublic;
  }

  /** @return the end point offset */
  public int getEndOffset() {
    return endOffset;
  }

  /** @param endOffset set the end point offset */
  public void setEndOffset(final int endOffset) {
    this.endOffset = endOffset;
  }

  /** @return return the end point path */
  //@RequiredStringValidator(message="You must specify a value")
  public String getEndPath() {
    return endPath;
  }

  /** @param endPath set the end point path */
  public void setEndPath(final String endPath) {
    this.endPath = endPath;
  }

  /** @return the start point offset */
  public int getStartOffset() {
    return startOffset;
  }

  /** @param startOffset set the start point offset */
  public void setStartOffset(final int startOffset) {
    this.startOffset = startOffset;
  }

  /** @return the start point path */
  //@RequiredStringValidator(message="You must specify a value")
  public String getStartPath() {
    return startPath;
  }

  /** @param startPath set start point path */
  public void setStartPath(final String startPath) {
    this.startPath = startPath;
  }

  /** @return the older annotation that it supersedes */
  public String getSupercedes() {
    return supercedes;
  }

  /** @param supercedes the older annotation that it supersedes */
  public void setSupercedes(final String supercedes) {
    this.supercedes = supercedes;
  }
}
