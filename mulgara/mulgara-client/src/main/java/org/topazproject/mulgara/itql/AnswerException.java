/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.mulgara.itql;

/**
 * Represents a problem parsing the response from the query or building the answer.
 * 
 * @author Ronald Tschalär
 */
public class AnswerException extends Exception {
  /** 
   * Create a new instance with the given error message. 
   * 
   * @param msg the error message
   */
  public AnswerException(String msg) {
    super(msg);
  }

  /** 
   * Create a new instance with the given error message and underlying exception. 
   * 
   * @param msg   the error message
   * @param cause the cause for this exception
   */
  public AnswerException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /** 
   * Create a new instance from the underlying exception. 
   * 
   * @param cause the cause for this exception
   */
  public AnswerException(Throwable cause) {
    super(cause);
  }
}
