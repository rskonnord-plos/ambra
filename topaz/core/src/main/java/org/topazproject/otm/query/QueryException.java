/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.query;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.topazproject.otm.OtmException;

/** 
 * Signals an exception while processing an OQL query.
 * 
 * @author Ronald Tschalär
 */
public class QueryException extends OtmException {
  private final List<String> errors;

  /** 
   * Create a new exception instance with a message. 
   * 
   * @param msg the details about the exception
   */
  public QueryException(String msg) {
    super(msg);
    errors = null;
  }

  /** 
   * Create a new chained exception instance with a message. 
   * 
   * @param msg   the details about the exception
   * @param cause the underlying exception that caused this exception
   */
  public QueryException(String msg, Throwable cause) {
    super(msg, cause);
    errors = null;
  }

  /** 
   * Create a exception instance with a message and given list of errors. 
   * 
   * @param msg    the details about the exception
   * @param errors the underlying errors that caused this exception
   */
  public QueryException(String msg, List<String> errors) {
    super(msg);
    this.errors = Collections.unmodifiableList(errors);
  }

  /** 
   * Create a exception instance with a message and given list of errors. 
   * 
   * @param msg    the details about the exception
   * @param errors the underlying errors that caused this exception
   * @param cause  the underlying exception that caused this exception
   */
  public QueryException(String msg, List<String> errors, Throwable cause) {
    super(msg, cause);
    this.errors = Collections.unmodifiableList(errors);
  }

  /** 
   * Get the list of underlying errors that caused this exception. 
   * 
   * @return the errors, or null
   */
  public List<String> getErrors() {
    return errors;
  }

  @Override
  public String toString() {
    String NL = System.getProperty("line.separator");
    return super.toString() + (errors != null ? NL + StringUtils.join(errors, NL) : "");
  }
}
