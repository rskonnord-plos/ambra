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

/**
 * Class that will represent comments by users persisted as comment annotations
 * by PlosOne.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Comment.RDF_TYPE)
public class Comment extends Annotation implements ArticleAnnotation {
  public static final String RDF_TYPE = Annotea.W3C_TYPE_NS + "Comment";
  /**
   * Annotation type Namespace URI
   */
  public static final String TYPE_NS = "http://www.w3.org/2000/10/annotationType#";

  @Predicate(uri = Annotea.W3C_NS + "body")
  private URI body;

  /**
   * Creates a new comment object.
   */
  public Comment() {
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
  
  public String getType() {
    return RDF_TYPE;
  }
}
