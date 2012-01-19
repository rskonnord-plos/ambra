/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service;

import org.plos.ApplicationException;
import org.plos.Constants;
import org.plos.util.TextUtils;

/**
 * Base class for Annotation and reply.
 * For now it does not bring together all the common attributes as I still prefer delegation for now.
 * Further uses of these classes on the web layer should clarify the requirements and drive any changes
 * if required.
 */
public abstract class BaseAnnotation {
  /** An integer constant to indicate a unique value for the  */
  public static final int PUBLIC_MASK = Constants.StateMask.PUBLIC;
  public static final int FLAG_MASK = Constants.StateMask.FLAG;
  public static final int DELETE_MASK = Constants.StateMask.DELETE;
  private static final int TRUNCATED_COMMENT_LENGTH = 256;

  /**
   * @return the escaped comment.
   * @throws org.plos.ApplicationException ApplicationException
   */
  public String getComment() throws ApplicationException {
    return escapeText(getOriginalBodyContent());
  }

  /**
   * @return the url linked and escaped comment.
   * @throws org.plos.ApplicationException ApplicationException
   */
  public String getCommentWithUrlLinking() throws ApplicationException {
    return TextUtils.hyperlinkEnclosedWithPTags(getComment());
  }


  /**
   * @return the url linked and escaped comment with a limit of 256 characters.
   * @throws org.plos.ApplicationException ApplicationException
   */
  public String getEscapedTruncatedComment() throws ApplicationException {
    String comment = getComment();
    if (comment.length() > TRUNCATED_COMMENT_LENGTH) {
      return TextUtils.hyperlinkEnclosedWithPTags(comment.substring(0, TRUNCATED_COMMENT_LENGTH) + "...");
    } else {
      return TextUtils.hyperlinkEnclosedWithPTags(comment);
    }
  }

  /**
   * @return the original content of the annotation body
   * @throws ApplicationException ApplicationException
   */
  protected abstract String getOriginalBodyContent() throws ApplicationException;

  /**
   * Escape text so as to avoid any java scripting maliciousness when rendering it on a web page
   * @param text text
   * @return the escaped text
   */
  protected String escapeText(final String text) {
    return TextUtils.escapeHtml(text);
  }

  /**
   * Is the Annotation public?
   * @return true if the annotation/reply is public, false if private
   */
  public boolean isPublic() {
    return (getState() & PUBLIC_MASK) == PUBLIC_MASK;
  }

  /**
   * Get state.
   * @return state as int.
   */
  public abstract int getState();

  /**
   * Is the annotation flagged?
   * @return true if the annotation is flagged, false otherwise
   */
  public boolean isFlagged() {
    return (getState() & FLAG_MASK) == FLAG_MASK;
  }

  /**
   * Is the annotation deleted?
   * @return true if the annotation has been deleted.
   */
  public boolean isDeleted() {
    return (getState() & DELETE_MASK) == DELETE_MASK;
  }

}
