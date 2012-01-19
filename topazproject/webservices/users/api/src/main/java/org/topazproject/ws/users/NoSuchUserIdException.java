/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.users;

import org.topazproject.common.NoSuchIdException;

/** 
 * Signifies that the user does not exist. 
 * 
 * @author Ronald Tschalär
 */
public class NoSuchUserIdException extends NoSuchIdException {
  /** 
   * Create a new exception instance with a default exception message. 
   * 
   * @param id  the (non-existant) id
   */
  public NoSuchUserIdException(String id) {
    super(id);
  }

  /** 
   * Create a new exception instance. 
   * 
   * @param id      the (non-existant) id
   * @param message the exception message
   */
  public NoSuchUserIdException(String id, String message) {
    super(id, message);
  }
}
