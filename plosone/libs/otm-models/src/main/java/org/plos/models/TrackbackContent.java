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

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * RatingContent is the body of a Rating.
 * It stores, insight, reliability, style and comment values.
 *
 * @author stevec
 * @author Jeff Suttor
 */
@UriPrefix(Rdf.topaz + "TrackbackContent/")
@Entity(model = "ri", type = Rdf.topaz + "TrackbackContent")
public class TrackbackContent {

  @Id
  @GeneratedValue(uriPrefix = "info:doi/10.1371/trackbackContent/")
  private String id;

  private String title;
  private URL url;
  private String blog_name;
  private String excerpt;

  private static final Log log = LogFactory.getLog(TrackbackContent.class);

  /**
   * Creates a new TrackbackContent object with default values.
   */
  public TrackbackContent() {
    this(null, null, null, null);
  }


  /**
   * Creates a new TrackbackContent object with specified values.
   *
   * @param title
   * @param excerpt
   * @param blog_name
   * @param url
   */
  public TrackbackContent(String title, String excerpt, String blog_name, URL url) {
    this.title = title;
    this.excerpt = excerpt;
    this.blog_name = blog_name;
    this.url = url;
  }

  /**
   * @return Returns the blog_name.
   */
  public String getBlog_name() {
    return blog_name;
  }

  /**
   * @param blog_name The blog_name to set.
   */
  public void setBlog_name(String blog_name) {
    this.blog_name = blog_name;
  }

  /**
   * @return Returns the excerpt.
   */
  public String getExcerpt() {
    return excerpt;
  }

  /**
   * @param excerpt The excerpt to set.
   */
  public void setExcerpt(String excerpt) {
    this.excerpt = excerpt;
  }

  /**
   * @return Returns the id.
   */
  public String getId() {
    return id;
  }

  /**
   * @param id The id to set.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return Returns the title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title The title to set.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return Returns the url.
   */
  public URL getUrl() {
    return url;
  }

  /**
   * @param url The url to set.
   */
  public void setUrl(URL url) {
    this.url = url;
  }
}
