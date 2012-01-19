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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.plos.article.service.BrowseService;

/**
 * The info about a single article that the UI needs.
 */
public class ArticleInfo implements Serializable {
  public URI                     id;
  public Set<ArticleType>        articleTypes = new HashSet<ArticleType>();
  public Date                    date;
  public String                  title;
  public List<String>            authors = new ArrayList<String>();
  public Set<RelatedArticleInfo> relatedArticles = new HashSet<RelatedArticleInfo>();

  /**
   * Get the id.
   *
   * @return the id.
   */
  public URI getId() {
    return id;
  }

  /**
   * Get the Article types.
   *
   * @return the Article types.
   */
  public Set<ArticleType> getArticleTypes() {
    return articleTypes;
  }

  /**
   * Get the date.
   *
   * @return the date.
   */
  public Date getDate() {
    return date;
  }

  /**
   * Get the title.
   *
   * @return the title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Get the authors.
   *
   * @return the authors.
   */
  public List<String> getAuthors() {
    return authors;
  }

  /**
   * Get the related articles.
   *
   * @return the related articles.
   */
  public Set<RelatedArticleInfo> getRelatedArticles() {
    return relatedArticles;
  }
}