/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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
package org.topazproject.ambra.model.article;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.topazproject.ambra.model.UserProfileInfo;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.Retraction;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Projection;
import org.topazproject.otm.annotations.View;

/**
 * The info about a single article that the UI needs.
 */
@View(query=
        "select a.id id, dc.date date, dc.title title, ci, " +
        "(select a.articleType from Article aa) at, " +
        "(select aa2.id rid, aa2.dublinCore.title rtitle from Article aa2 " +
        "   where aa2 = a.relatedArticles.article) relatedArticles, " +
        "(select fc from FormalCorrection fc where fc.annotates = a.id) corrections, " +
        "(select r from Retraction r where r.annotates = a.id) retractions " +
        "from Article a, CitationInfo ci " +
        "where a.id = :id and dc := a.dublinCore and ci.id = dc.bibliographicCitation.id;")
public class ArticleInfo implements Serializable {

  private static final long serialVersionUID = 356L;

  public URI                     id;
  public Date                    date;
  private String                 title;
  public Set<RelatedArticleInfo> relatedArticles = new HashSet<RelatedArticleInfo>();
  public List<String>            authors = new ArrayList<String>();
  public Set<ArticleType>        articleTypes = new HashSet<ArticleType>();
  public Set<FormalCorrection>   corrections = new HashSet<FormalCorrection>();
  public Set<Retraction>         retractions = new HashSet<Retraction>();
  private transient String unformattedTitle;

  /**
   * Set the ID of this Article. This is the Article DOI. 
   * 
   * @param id
   */
  @Id
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
  @Projection("date")
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
  @Projection("title")
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
   * Get the related articles.
   *
   * @return the related articles.
   */
  public Set<RelatedArticleInfo> getRelatedArticles() {
    return relatedArticles;
  }

  @Projection("ci")
  public void setCi(CitationInfo ci) {
    authors.clear();
    for (UserProfileInfo upi : ci.getAuthors()) {
      authors.add(upi.getRealName());
    }
  }

  @Projection("at")
  public void setAt(Set<URI> at) {
    articleTypes.clear();
    for (URI a : at)
      articleTypes.add(ArticleType.getArticleTypeForURI(a, true));
  }

  @Projection("relatedArticles")
  public void setRelatedArticles(Set<RelatedArticleInfo> relatedArticles) {
    this.relatedArticles = relatedArticles;
  }

  public Set<FormalCorrection> getCorrections() {
    return corrections;
  }

  @Projection("corrections")
  public void setCorrections(Set<FormalCorrection> corrections) {
    this.corrections = corrections;
  }

  public Set<Retraction> getRetractions() {
    return retractions;
  }

  @Projection("retractions")
  public void setRetractions(Set<Retraction> retractions) {
    this.retractions = retractions;
  }
}
