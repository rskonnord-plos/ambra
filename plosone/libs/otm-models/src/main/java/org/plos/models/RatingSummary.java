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
 * General base rating class to store a RatingSummaryContent body.
 *
 * @author Stephen Cheng
 */
@Entity(type = Rdf.topaz + "RatingSummaryAnnotation")
public class RatingSummary extends AbstractAnnotation {
  @Predicate(uri = Annotea.NS + "body")
  private RatingSummaryContent body;

  /**
   * Creates a new RatingSummary object.
   */
  public RatingSummary() {
  }

  /**
   * Creates a new RatingSummary object.
   *
   * @param id id for the rating summary annotation
   */
  public RatingSummary(URI id) {
    super(id);
  }

  /**
   * @return Returns the rating.
   */
  public RatingSummaryContent getBody() {
    return this.body;
  }

  /**
   * @param rating The rating to set.
   */
  public void setBody(RatingSummaryContent ratingSummaryContent) {
    this.body = ratingSummaryContent;
  }
}
