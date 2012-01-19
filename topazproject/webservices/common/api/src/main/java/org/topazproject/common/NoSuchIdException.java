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
 * Signifies that the requested object does not exist. 
 * 
 * @author Eric Brown
 */
public class NoSuchIdException extends Exception {
  private final String id;

  /** 
   * Create a new exception instance. 
   * 
   * @param id  the (non-existant) id
   */
  public NoSuchIdException(String id) {
    this(id, "id = '" + id + "'");
  }

  /** 
   * Create a new exception instance. 
   * 
   * @param id      the (non-existant) id
   * @param message the exception message
   */
  public NoSuchIdException(String id, String message) {
    super(message);
    this.id = id;
  }

  /** 
   * @return the (non-existant) id
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
