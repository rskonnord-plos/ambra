/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

/**
 * Class that will represent comments by users persisted as comment annotations
 * by PlosOne.
 *
 * @author Pradeep Krishnan
 */
public class Comment extends Annotation {
  /**
   * Annotation type Namespace URI
   */
  public static final String TYPE_NS = "http://www.w3.org/2000/10/annotationType#";

  @Predicate(uri = Annotea.NS + "body")
  private URI body;

  /**
   * Creates a new comment object.
   */
  public Comment() {
  }

  /**
   * Creates a new comment object.
   *
   * @param id annotation id
   */
  public Comment(URI id) {
    super(id);
  }

  /**
   * @return Returns the body of the comment
   */
  public URI getBody() {
    return body;
  }

  /**
   * @param body The body of the comment to set
   */
  public void setBody(URI body) {
    this.body = body;
  }
}
