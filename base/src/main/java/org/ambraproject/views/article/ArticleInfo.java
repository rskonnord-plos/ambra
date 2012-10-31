/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
package org.ambraproject.views.article;

import org.ambraproject.models.CitedArticle;
import org.ambraproject.views.ArticleCategory;
import org.ambraproject.views.AssetView;
import org.ambraproject.views.UserProfileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The info about a single article that the UI needs.
 */
public class ArticleInfo extends BaseArticleInfo implements Serializable {
  private static final Logger log = LoggerFactory.getLogger(ArticleInfo.class);

  private static final long serialVersionUID = 3823215602197299918L;

  public Long                    id;
  public List<RelatedArticleInfo> relatedArticles = new ArrayList<RelatedArticleInfo>();
  public List<String>            collaborativeAuthors = new ArrayList<String>();
  private String                 publisher;
  private String                 rights;
  private String                 description;
  private String                 journal;
  private String                 pages;
  private Set<ArticleCategory>   categories;
  private String                 eLocationId;
  private String                 volume;
  private String                 issue;
  private List<AssetView>        articleAssets;
  private List<CitedArticle>     citedArticles;

  private transient String unformattedTitle = null;

  /**
   * Get an unformatted version of the Article Title. 
   * @return Unformatted title.
   */
  public String getUnformattedTitle() {
    if ((unformattedTitle == null) && (title != null)) {
      unformattedTitle = title.replaceAll("</?[^>]*>", "");
    }
    return unformattedTitle;
  }

  /**
   * Get article description.
   * @return Description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set article description.
   * @param description Description.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Get the collaborative authors
   * @return collaborative authors
   */
  public List<String> getCollaborativeAuthors() {
    return collaborativeAuthors;
  }

  /**
   * Get the related articles.
   *
   * @return the related articles.
   */
  public List<RelatedArticleInfo> getRelatedArticles() {
    return relatedArticles;
  }

  public void setCi(CitationInfo ci) {
    // get the authors
    authors.clear();
    for (UserProfileInfo upi : ci.getAuthors()) {
      authors.add(upi.getRealName());
    }

    // get the collaborative authors
    collaborativeAuthors.clear();
    for (String collaborativeAuthor : ci.getCollaborativeAuthors()) {
      collaborativeAuthors.add(collaborativeAuthor);
    }
  }

  public void setArticleAssets(List<AssetView> articleAssets) {
    this.articleAssets = articleAssets;
  }

  public void setRelatedArticles(List<RelatedArticleInfo> relatedArticles) {
    this.relatedArticles = relatedArticles;
  }
  
  public List<AssetView> getArticleAssets(){
    return articleAssets;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public String getJournal() {
    return journal;
  }

  public void setJournal(String journal) {
    this.journal = journal;
  }

  public String getRights() {
    return rights;
  }

  public void setRights(String rights) {
    this.rights = rights;
  }

  /**
   * Return a list of all categories and sub categories collapsed into one list.
   *
   * @return
   */
  public List<String> getTransposedCategories()
  {
    //set categories
    List<String> subjects = null;

    if (categories != null) {
      subjects = new ArrayList<String>(categories.size());

      for (ArticleCategory category : categories) {
        if (category.getMainCategory() != null
            && ! subjects.contains(category.getMainCategory())) {
          subjects.add(category.getMainCategory());
        }
        if (category.getSubCategory() != null
            && ! subjects.contains(category.getSubCategory())) {
          subjects.add(category.getSubCategory());
        }
      }
      Collections.sort(subjects);
    }

    return subjects;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPages() {
    return pages;
  }

  public void setPages(String pages) {
    this.pages = pages;
  }

  public Set<ArticleCategory> getCategories() {
    return categories;
  }

  public void setCategories(Set<ArticleCategory> categories) {
    this.categories = categories;
  }

  public String geteLocationId() {
    return eLocationId;
  }

  public void seteLocationId(String eLocationId) {
    this.eLocationId = eLocationId;
  }

  public String getVolume() {
    return volume;
  }

  public void setVolume(String volume) {
    this.volume = volume;
  }

  public String getIssue() {
    return issue;
  }

  public void setIssue(String issue) {
    this.issue = issue;
  }

  public List<CitedArticle> getCitedArticles() {
    return citedArticles;
  }

  public void setCitedArticles(List<CitedArticle> citedArticles) {
    this.citedArticles = citedArticles;
  }
}
