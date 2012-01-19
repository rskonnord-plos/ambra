/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.service;

import java.util.Date;

/**
 * This returns meta-data about an article.
 *
 * @author Eric Brown
 * @version $Id$
 */
public class ArticleInfo {
  private String   uri;
  private String   title;
  private String   description;
  private Date     articleDate;
  private String[] authors;
  private String[] subjects;
  private String[] categories;
  private int      state;

  /**
   * Get the URI.
   *
   * @return the URI.
   */
  public String getUri() {
    return uri;
  }

  /**
   * Set the URI.
   *
   * @param uri the URI.
   */
  public void setUri(String uri) {
    this.uri = uri;
  }

  /**
   * Get the article title.
   *
   * @return the article title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set the article title.
   *
   * @param title the article title.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Get the article description (abstract).
   *
   * @return the article description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the article description.
   *
   * @param description the article description.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Get the article date.
   *
   * @return the article date.
   */
  public Date getArticleDate() {
    return articleDate;
  }

  /**
   * Set the article date.
   *
   * @param articleDate the article date.
   */
  public void setArticleDate(Date articleDate) {
    this.articleDate = articleDate;
  }

  /**
   * Get the article's authors.
   *
   * @return the article's authors.
   */
  public String[] getAuthors() {
    return authors;
  }

  /**
   * Set the article's authors.
   *
   * @param authors the article's authors.
   */
  public void setAuthors(String[] authors) {
    this.authors = authors;
  }

  /**
   * Get the article's subjects. These are strings that represent the combination category
   * and subcategory. This is likely to be deprecated in the future.
   *
   * @return the article's subjects.
   */
  public String[] getSubjects() {
    return subjects;
  }

  /**
   * Set the article's subjects.
   *
   * @param subjects the article's subjects.
   */
  public void setSubjects(String[] subjects) {
    this.subjects = subjects;
  }

  /**
   * Get the article's categories. These are the primary categories for the article.
   * It does not include the sub-categories.
   *
   * @return the article's categories.
   */
  public String[] getCategories() {
    return categories;
  }

  /**
   * Set the article's categories.
   *
   * @param categories the article's categories.
   */
  public void setCategories(String[] categories) {
    this.categories = categories;
  }

  /**
   * Get the article's state.
   *
   * @return the article's state.
   */
  public int getState() {
    return state;
  }

  /**
   * Set the article's state.
   *
   * @param state the article's state.
   */
  public void setState(int state) {
    this.state = state;
  }
}
