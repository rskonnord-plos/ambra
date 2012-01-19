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

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;

/**
 * General base rating class to store a RatingSummaryContent body.
 *
 * @author Stephen Cheng
 */
@Entity(type = Rdf.topaz + "RatingSummaryAnnotation")
public class RatingSummary extends Annotation {
  private static final long serialVersionUID = -8110763767878695617L;
  
  @Predicate(uri = Annotea.W3C_NS + "body")
  private RatingSummaryContent body;

  /**
   * Creates a new RatingSummary object.
   */
  public RatingSummary() {
  }

  /**
   * @return Returns the rating.
   */
  public RatingSummaryContent getBody() {
    return this.body;
  }

  /**
   * @param ratingSummaryContent The rating to set.
   */
  public void setBody(RatingSummaryContent ratingSummaryContent) {
    this.body = ratingSummaryContent;
  }
}
