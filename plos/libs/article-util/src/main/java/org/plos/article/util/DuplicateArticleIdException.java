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
 * Signifies that the article with the requested id already exists.
 *
 * @author Ronald Tschalär
 * @author Eric Brown
 * @version $Id$
 */
public class DuplicateArticleIdException extends Exception {
  private final String id;

  /**
   * Create a new exception instance with a default exception message.
   *
   * @param id      the (duplicate) id
   */
  public DuplicateArticleIdException(String id) {
    this(id, "id = '" + id + "'");
  }

  /**
   * Create a new exception instance.
   *
   * @param id      the (duplicate) id
   * @param message the exception message
   */
  public DuplicateArticleIdException(String id, String message) {
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
