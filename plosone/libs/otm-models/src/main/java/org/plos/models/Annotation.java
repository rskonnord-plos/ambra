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

import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

/**
 * Class that will represent annotations persisted by PlosOne.
 *
 * @author Pradeep Krishnan
 */
public class Annotation extends AbstractAnnotation {
  /**
   * Annotation type Namespace URI
   */
  public static final String TYPE_NS = "http://www.w3.org/2000/10/annotationType#";
  @Predicate(uri = Annotea.NS + "body")
  private URI body;

  /**
   * Creates a new Annotation object.
   */
  public Annotation() {
  }

  /**
   * Creates a new PlosAnnotation object.
   *
   * @param id annotation id
   */
  public Annotation(URI id) {
    super(id);
  }

  /**
   * @return Returns the body.
   */
  public URI getBody() {
    return body;
  }

  /**
   * @param body The body to set.
   */
  public void setBody(URI body) {
    this.body = body;
  }
}
