/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.topazproject.ambra.article.action;

import java.util.ArrayList;
import java.util.Collections;

import org.topazproject.ambra.model.article.ArticleInfo;
import org.topazproject.ambra.model.article.ArticleInfoMostRecentDateComparator;
import org.topazproject.ambra.model.article.ArticleType;
/**
 * Represents a group of articles for display in the presentation layer. 
 *  
 * @author Alex
 */
public class TOCArticleGroup {
  ArticleType type;
  public ArrayList<ArticleInfo> articles = new ArrayList<ArticleInfo>();
  private String id = null;
  private String heading = null;

  public TOCArticleGroup(ArticleType type) {
    this.type = type;
  }

  /**
   * The heading displayed for this article group. 
   *
   * @return the heading, or the string "Undefined"
   */
  public String getHeading() {
    if (heading != null) {
      return heading;
    } else if (type != null) {
      return type.getHeading();
    } else {
      return "Undefined";
    }
  }

  public void setHeading(String h) {
    heading = h;
  }
  /**
   * An id for the group to be used in html. May be set by the action for each ArticleGroup
   * and should be unique within that group. 
   * 
   * @param i   id
   */
  public void setId(String i) {
    this.id = i;
  }

  public String getId() {
    if (id != null) {
      return id;
    } else {
      return "id_"+getHeading();
    }
  }

  public void addArticle(ArticleInfo article) {
    articles.add(article);
  }

  public ArticleType getArticleType() {
    return type;
  }

  public ArrayList<ArticleInfo> getArticles() {
    return articles;
  }

  public void sortArticles() {
    ArticleInfoMostRecentDateComparator comparator = new ArticleInfoMostRecentDateComparator();
    Collections.sort(articles, comparator);
  }
}
