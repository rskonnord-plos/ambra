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

import org.topazproject.common.DuplicateIdException;

/** 
 * Signifies that the authentication id is already in use.
 * 
 * @author Ronald Tschalär
 */
public class DuplicateAuthIdException extends DuplicateIdException {
  /** 
   * Create a new exception instance with a default exception message. 
   * 
   * @param id  the (duplicate) id
   */
  public DuplicateAuthIdException(String id) {
    super(id);
  }

  /** 
   * Create a new exception instance. 
   * 
   * @param id      the (duplicate) id
   * @param message the exception message
   */
  public DuplicateAuthIdException(String id, String message) {
    super(id, message);
  }
}
