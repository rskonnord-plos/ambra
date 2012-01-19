/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.util;

/**
 * Signifies that the requested object does not exist.
 *
 * @author Ronald Tschalär
 * @author Eric Brown
 */
public class NoSuchObjectIdException extends Exception {
  private final String id;

  /**
   * Create a new exception instance with a default exception message.
   *
   * @param id      the (non-existant) id
   */
  public NoSuchObjectIdException(String id) {
    this(id, "", null);
  }

  /**
   * Create a new exception instance.
   *
   * @param id      the (non-existant) id
   * @param message the exception message
   */
  public NoSuchObjectIdException(String id, String message) {
    this(id, message, null);
  }

  /**
   * Create a new exception instance.
   *
   * @param id      the (non-existant) id
   * @param message the exception message
   * @param cause   the exception cause
   */
  public NoSuchObjectIdException(String id, String message, Throwable cause) {
    super("(id=" + id + ")" + message, cause);
    this.id = id;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }
}
