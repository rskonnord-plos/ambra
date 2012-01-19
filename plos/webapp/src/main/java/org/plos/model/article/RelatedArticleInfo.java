/* $HeadURL$
 * $Id$ 
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.model.article;

import java.io.Serializable;
import java.net.URI;

public class RelatedArticleInfo implements Serializable {
  public final URI    uri;
  public final String title;

  /**
   * Create a new related-article-info object.
   *
   * @param uri   the uri of the article
   * @param title the article's title
   */
  public RelatedArticleInfo(URI uri, String title) {
    this.uri   = uri;
    this.title = title;
  }

  /**
   * Get the article uri.
   *
   * @return the article uri.
   */
  public URI getUri() {
    return uri;
  }

  /**
   * Get the title.
   *
   * @return the title.
   */
  public String getTitle() {
    return title;
  }

  public String toString() {
    return "RelatedArticleInfo[uri='" + uri + "', title='" + title + "']";
  }
}