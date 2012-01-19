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


/**
 * The info about a single article that the UI needs.
 */
public class ArticleInfo implements Serializable {
  public URI                     id;
  public Set<ArticleType>        articleTypes = new HashSet<ArticleType>();
  public Date                    date;
  private String                 title;
  public List<String>            authors = new ArrayList<String>();
  public Set<RelatedArticleInfo> relatedArticles = new HashSet<RelatedArticleInfo>();
  private String unformattedTitle;

  /**
   * Set the ID of this Article. This is the Article DOI. 
   * 
   * @param id
   */
  public void setId(URI id) {
    this.id = id;
  }
  
  /**
   * Get the id.
   *
   * @return the id.
   */
  public URI getId() {
    return id;
  }

  /**
   * Get the set of all Article types associated with this Article.
   *
   * @return the Article types.
   */
  public Set<ArticleType> getArticleTypes() {
    return articleTypes;
  }

  /**
   * Add an ArticleType corresponding to this article. 
   * 
   * @param at
   */
  public void addArticleType(ArticleType at) {
    articleTypes.add(at);
  }
  
  /**
   * Get the date that this article was published.
   *
   * @return the date.
   */
  public Date getDate() {
    return date;
  }
  
  /**
   * Set the Date that this article was published
   * @param date
   */
  public void setDate(Date date) {
    this.date = date;
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
   * Set the title of this Article.
   *  
   * @param articleTitle
   */
  public void setTitle(String articleTitle) {
    title = articleTitle;
    unformattedTitle = null;
  }
  
  /**
   * Get an unformatted version of the Article Title. 
   * @return
   */
  public String getUnformattedTitle() {
    if ((unformattedTitle == null) && (title != null)) {
      unformattedTitle = title.replaceAll("</?[^>]*>", "");
    }
    return unformattedTitle;
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
   * Add an author's name to the list of authors of this Article
   * 
   * @param authorName
   */
  public void addAuthor(String authorName) {
    authors.add(authorName);
  }

  /**
   * Get the related articles.
   *
   * @return the related articles.
   */
  public Set<RelatedArticleInfo> getRelatedArticles() {
    return relatedArticles;
  }
  
  /**
   * Add a RelatedArticleInfo to the set of related articles. 
   * 
   * @param rai
   */
  public void addRelatedArticle(RelatedArticleInfo rai) {
    relatedArticles.add(rai);
  }
}