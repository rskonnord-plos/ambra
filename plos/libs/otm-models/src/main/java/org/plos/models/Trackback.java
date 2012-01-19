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
import java.net.URL;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;

/**
 * Represents a trackback on a resource.
 *
 * @author Stephen Cheng
 */
@Entity(type = Rdf.topaz + "TrackbackAnnotation")
public class Trackback extends Annotation {
  @Predicate(uri = Annotea.W3C_NS + "body")
  private TrackbackContent body;

  /**
   * @return Returns the body.
   */
  public TrackbackContent getBody() {
    return body;
  }

  /**
   * @param body The body to set.
   */
  public void setBody(TrackbackContent body) {
    this.body = body;
  }

  /**
   * @return Returns the url.
   */
  public URL getUrl() {
    if (body != null)
      return body.getUrl();
    return null;
  }

  /**
   * @return Returns the title.
   */
  public String getTitle() {
    if (body != null)
      return body.getTitle();
    return "";
  }

  /**
   * @return Returns the excerpt.
   */
  public String getExcerpt() {
    if (body != null)
      return body.getExcerpt();
    return "";
  }

  /**
   * @return Returns the blog_name.
   */
  public String getBlog_name() {
    if (body != null)
      return body.getBlog_name();
    return "";
  }

}
