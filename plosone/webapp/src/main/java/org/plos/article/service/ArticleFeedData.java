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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.plos.models.Category;
/**
 * Article meta data.
 *
 * @author Eric Brown
 */
public class ArticleFeedData {
  String   uri;
  String   title;
  String   description;
  Date     date;
  List     authors = new ArrayList();
  List     subjects = new ArrayList();
  List     categories = new ArrayList();
  int      state;
  
  /**
   * Public constructor.
   */
  public ArticleFeedData(String          uri,
                         String          title,
                         String          description,
                         Date            date,
                         Set<String>     authors,
                         Set<String>     subjects,
                         Set<Category>   categories,
                         int             state) {
  this.uri = uri;
  this.title = title;
  this.description = description;
  this.date = date;
  this.authors.addAll(authors);
  this.subjects.addAll(subjects);
  this.categories.addAll(categories);
  this.state = state;
  }

  /** Return the article's URI */
  public String getUri() { return uri; }
  /** Return the article's title */
  public String getTitle() { return title; }
  /** Return the article's description (the abstract) */
  public String getDescription() { return description; }
  /** Return the article's date */
  public Date getArticleDate() { return date; }
  /** Return the article's authors -- a list of Strings */
  public List getAuthors() { return authors; }
  /** Return the article's subjects (category[/subcategory]) -- a list of Strings */
  public List getSubjects() { return subjects; }
  /** Return the article's main categories (not subcategories) -- a list of Strings */
  public List getCategories() { return categories; }
  /** Return the article's state */
  public int getState() { return state; }
  
  public String toString() {
    return "ArticleData[" + this.uri + ":" + this.date + "]";
  }
}
