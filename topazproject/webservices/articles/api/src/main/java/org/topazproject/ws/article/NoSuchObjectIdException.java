/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.article;

import org.topazproject.common.NoSuchIdException;

/** 
 * Signifies that the requested object does not exist. 
 * 
 * @author Ronald Tschalär
 */
public class NoSuchObjectIdException extends NoSuchIdException {
  /** 
   * Create a new exception instance with a default exception message. 
   * 
   * @param id      the (non-existant) id
   */
  public NoSuchObjectIdException(String id) {
    super(id);
  }

  /** 
   * Create a new exception instance. 
   * 
   * @param id      the (non-existant) id
   * @param message the exception message
   */
  public NoSuchObjectIdException(String id, String message) {
    super(id, message);
  }
}
