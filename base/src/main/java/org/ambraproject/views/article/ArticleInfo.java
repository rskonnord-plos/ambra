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
  private List<ArticleCategory>  orderedCategories;
  private String                 eLocationId;
  private String                 volume;
  private String                 issue;
  private List<AssetView>        articleAssets;
  private List<CitedArticle>     citedArticles;
  private String                 strkImgURI;

  private transient String unformattedTitle = null;

  /**
   * Construct a new ArticleInfo info class
   */
  public ArticleInfo() { }

  /**
   * Construct a new ArticleInfo info class with the passed in DOI
   * @param doi
   */
  public ArticleInfo(String doi) {
    this.doi = doi;
  }

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
   * Set collaborative authors
   * @param collaborativeAuthors
   */
  public void setCollaborativeAuthors(List<String> collaborativeAuthors) {
    this.collaborativeAuthors = collaborativeAuthors;
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

  public List<ArticleCategory> getOrderedCategories() {
    return this.orderedCategories;
  }

  public void setOrderedCategories(List<ArticleCategory> orderedCategories) {
    this.orderedCategories = orderedCategories;
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

  public String getStrkImgURI() {
    return strkImgURI;
  }

  public void setStrkImgURI(String strkImgURI) {
    this.strkImgURI = strkImgURI;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ArticleInfo that = (ArticleInfo) o;

    if (articleAssets != null ? !articleAssets.equals(that.articleAssets) : that.articleAssets != null) return false;
    if (categories != null ? !categories.equals(that.categories) : that.categories != null) return false;
    if (orderedCategories != null ? !orderedCategories.equals(that.orderedCategories) : that.orderedCategories != null) return false;
    if (citedArticles != null ? !citedArticles.equals(that.citedArticles) : that.citedArticles != null) return false;
    if (collaborativeAuthors != null ? !collaborativeAuthors.equals(that.collaborativeAuthors) : that.collaborativeAuthors != null)
      return false;
    if (description != null ? !description.equals(that.description) : that.description != null) return false;
    if (eLocationId != null ? !eLocationId.equals(that.eLocationId) : that.eLocationId != null) return false;
    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (issue != null ? !issue.equals(that.issue) : that.issue != null) return false;
    if (journal != null ? !journal.equals(that.journal) : that.journal != null) return false;
    if (pages != null ? !pages.equals(that.pages) : that.pages != null) return false;
    if (publisher != null ? !publisher.equals(that.publisher) : that.publisher != null) return false;
    if (relatedArticles != null ? !relatedArticles.equals(that.relatedArticles) : that.relatedArticles != null)
      return false;
    if (rights != null ? !rights.equals(that.rights) : that.rights != null) return false;
    if (strkImgURI != null ? !strkImgURI.equals(that.strkImgURI) : that.strkImgURI != null) return false;
    if (unformattedTitle != null ? !unformattedTitle.equals(that.unformattedTitle) : that.unformattedTitle != null)
      return false;
    if (volume != null ? !volume.equals(that.volume) : that.volume != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (relatedArticles != null ? relatedArticles.hashCode() : 0);
    result = 31 * result + (collaborativeAuthors != null ? collaborativeAuthors.hashCode() : 0);
    result = 31 * result + (publisher != null ? publisher.hashCode() : 0);
    result = 31 * result + (rights != null ? rights.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (journal != null ? journal.hashCode() : 0);
    result = 31 * result + (pages != null ? pages.hashCode() : 0);
    result = 31 * result + (categories != null ? categories.hashCode() : 0);
    result = 31 * result + (orderedCategories != null ? orderedCategories.hashCode() : 0);
    result = 31 * result + (eLocationId != null ? eLocationId.hashCode() : 0);
    result = 31 * result + (volume != null ? volume.hashCode() : 0);
    result = 31 * result + (issue != null ? issue.hashCode() : 0);
    result = 31 * result + (articleAssets != null ? articleAssets.hashCode() : 0);
    result = 31 * result + (citedArticles != null ? citedArticles.hashCode() : 0);
    result = 31 * result + (strkImgURI != null ? strkImgURI.hashCode() : 0);
    result = 31 * result + (unformattedTitle != null ? unformattedTitle.hashCode() : 0);
    return result;
  }
}
