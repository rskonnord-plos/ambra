/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.article;

import org.topazproject.common.DuplicateIdException;

/** 
 * Signifies that an object with the requested id already exists.
 * 
 * @author Ronald Tschalär
 * @version $Id$
 */
public class DuplicateArticleIdException extends DuplicateIdException {
  /** 
   * Create a new exception instance with a default exception message. 
   * 
   * @param id      the (duplicate) id
   */
  public DuplicateArticleIdException(String id) {
    super(id);
  }

  /** 
   * Create a new exception instance. 
   * 
   * @param id      the (duplicate) id
   * @param message the exception message
   */
  public DuplicateArticleIdException(String id, String message) {
    super(id, message);
  }
}
