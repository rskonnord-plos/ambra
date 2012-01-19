/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.annotation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.annotation.service.WebAnnotation;
import org.plos.annotation.service.Reply;
import org.plos.util.DateParser;
import org.plos.util.InvalidDateException;

import java.util.Comparator;
import java.util.Date;

/**
 * Simple wrapper class around an Annotation and the associated list of replies.
 * Implements the compartor interface to sort by reverse chronological order.
 * 
 * @author Stephen Cheng
 *
 */
public class Commentary implements Comparator<Commentary> {
  private WebAnnotation annotation;
  private int numReplies;
  private String lastModified;
  private Reply[]replies;
  private static final Log log = LogFactory.getLog(Commentary.class);

  public Commentary() {
  }

  /**
   * @return Returns the annotation.
   */
  public WebAnnotation getAnnotation() {
    return annotation;
  }

  /**
   * @param annotation The annotation to set.
   */
  public void setAnnotation(WebAnnotation annotation) {
    this.annotation = annotation;
  }

  /**
   * @return Returns the replies.
   */
  public Reply[] getReplies() {
    return replies;
  }

  /**
   * @param replies The replies to set.
   */
  public void setReplies(Reply[] replies) {
    this.replies = replies;
  }

  /**
   * This comparator does a reverse sort based on the last reply to the annotation.  If not replies
   * are present, the annotation time is used.
   * 
   * @param a
   * @param b
   * @return a number less than 0 if <var>a</var> less than <var>b</var>, a number greater than 0 if <var>a</var> greater than <var>b</var>, or 0 if <var>a</var> equals <var>b</var>
   */
  public int compare (Commentary a, Commentary b){
    String dateA, dateB;
    if (a.getNumReplies() == 0) {
      dateA = a.getAnnotation().getCreated();
    } else {
      dateA = a.getLastModified();
    }
    if (b.getNumReplies() == 0) {
      dateB = b.getAnnotation().getCreated();
    } else {
      dateB = b.getLastModified();
    }
    return dateB.compareTo(dateA);
  }

  /**
   * @return Returns the lastModified.
   */
  public String getLastModified() {
    return lastModified;
  }

  /**
   * @param lastModified The lastModified to set.
   */
  public void setLastModified(String lastModified) {
    this.lastModified = lastModified;
  }

  public Date getLastModifiedAsDate() {
    String theDate;

    if (lastModified == null) {
      theDate = annotation.getCreated();;
    } else {
      theDate = lastModified;
    }
    try {
      if (log.isDebugEnabled()) {
        log.debug("parsing date for reply: " + this.annotation.getId() +
                  "; dateString is: " + theDate);
      }
      return DateParser.parse (theDate);
    } catch (InvalidDateException ide) {
      log.error("Could not parse date for commnetary: " + this.annotation.getId() +
                "; dateString is: " + theDate, ide);
    }
    return null;
  }

  /**
   * @return Returns the numReplies.
   */
  public int getNumReplies() {
    return numReplies;
  }

  /**
   * @param numReplies The numReplies to set.
   */
  public void setNumReplies(int numReplies) {
    this.numReplies = numReplies;
  }
}
