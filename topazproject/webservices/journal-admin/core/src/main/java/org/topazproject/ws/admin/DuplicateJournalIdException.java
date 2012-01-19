/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.admin;

// TODO: Subclass DuplicateIdException -- At the moment breaks journal-admin integraiton-tests
//import org.topazproject.common.DuplicateIdException;

/** 
 * Signifies that an object with the requested id already exists.
 * 
 * @author Ronald Tschalär
 */
public class DuplicateJournalIdException extends Exception {
  private final String id;

  /** 
   * Create a new exception instance with a default exception message. 
   * 
   * @param id  the (duplicate) id
   */
  public DuplicateJournalIdException(String id) {
    this.id = id;
  }

  /** 
   * Create a new exception instance. 
   * 
   * @param id      the (duplicate) id
   * @param message the exception message
   */
  public DuplicateJournalIdException(String id, String message) {
    super(message);
    this.id = id;
  }

  /** 
   * @return the (duplicate) id
   */
  public String getId() {
    return id;
  }
}
