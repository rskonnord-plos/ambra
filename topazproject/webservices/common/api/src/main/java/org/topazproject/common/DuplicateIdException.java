/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.common;

/** 
 * Signifies that the an object with the requested id already exists.
 * 
 * @author Eric Brown
 */
public class DuplicateIdException extends Exception {
  private final String id;

  /** 
   * Create a new exception instance. 
   * 
   * @param id  the (duplicate) id
   */
  public DuplicateIdException(String id) {
    this(id, "id = '" + id + "'");
  }

  /** 
   * Create a new exception instance. 
   * 
   * @param id      the (duplicate) id
   * @param message the exception message
   */
  public DuplicateIdException(String id, String message) {
    super(message);
    this.id = id;
  }

  /** 
   * @return the (duplicate) id
   */
  public String getId() {
    return id;
  }

  /** 
   * This is just to get JAX-RPC to include the message in the fault.
   *
   * @return the message, or null
   */
  public String getMessage() {
    return super.getMessage();
  }
}
