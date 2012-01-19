/* $HeadURL::                                                                                     $
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
package org.topazproject.ambra.models;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;

/**
 * Model for Ambra articles.
 *
 * @author Eric Brown
 * @author Amit Kapoor
 */
@Entity(types = {"topaz:Article"}, graph = "ri")
public class Article extends ObjectInfo {
  private static final long serialVersionUID = 2688354354725491666L;

  /** Article state of "Active" */
  public static final int STATE_ACTIVE   = 0;
  /** Article state of "Unpublished" */
  public static final int STATE_UNPUBLISHED = 1;
  /** Article state of "Disabled" */
  public static final int STATE_DISABLED = 2;

  /** Active article states */
  public static final int[] ACTIVE_STATES = { STATE_ACTIVE };

  private List<ObjectInfo>    parts = new ArrayList<ObjectInfo>();
  private Set<URI>            articleType;
  private Set<RelatedArticle> relatedArticles;
  private Set<Category>       categories = new HashSet<Category>();
  private int                 state;
  private String              archiveName;
  private List<ArticleContributor> authors;
  private List<ArticleContributor> editors;

  /**
   * @return the different parts of the article 
   */
  public List<ObjectInfo> getParts() {
    return parts;
  }

  /** 
   * @param parts the different parts of the article 
   */
  @Predicate(uri = "dcterms:hasPart", collectionType = CollectionType.RDFSEQ,
             cascade = { CascadeType.child })
  public void setParts(List<ObjectInfo> parts) {
    this.parts = parts;
  }

  /**
   * Get the list of categories for the article
   *
   * @return the categories
   */
  public Set<Category> getCategories() {
    return categories;
  }

  /**
   * A single Ambra article can have multiple types associated with it. For
   * example, it could have a PMC article type and also a Ambra specific article
   * type. This setter allows the application to set the different types this
   * article conforms to.
   *
   * @param articleType the different types this article conforms to.
   */
  @Predicate(uri = "rdf:type")
  public void setArticleType(Set<URI> articleType) {
    this.articleType = articleType;
  }

  /**
   * Return the different types of the article
   *
   * @return the article types
   */
  public Set<URI> getArticleType() {
    return articleType;
  }

  /**
   * Set the list of categories for the article.
   *
   * TODO: This needs to be changed.
   *
   * @param categories the categories to article belongs to
   */
  @Predicate(uri = "topaz:hasCategory", cascade = { CascadeType.child })
  public void setCategories(Set<Category> categories) {
    this.categories = categories;
  }

  /**
   * Get the list of related articles.
   *
   * @return the list of related articles
   */
  public Set<RelatedArticle> getRelatedArticles() {
    return relatedArticles;
  }

  /**
   * Set the list of related articles.
   *
   * @param relatedArticles the set of related articles.
   */
  @Predicate(uri = "plos:relatedArticle", cascade = { CascadeType.child })
  public void setRelatedArticles(Set<RelatedArticle> relatedArticles) {
    this.relatedArticles = relatedArticles;
  }

  /**
   * Get the state of this article.
   *
   * @return the article state
   */
  public int getState() {
    return state;
  }

  /**
   * Set the state of this article.
   *
   * @param state the article state to set
   */
  @Predicate(uri = "topaz:articleState")
  public void setState(int state) {
    this.state = state;
  }

  /**
   * Get archive name
   * @return archive name
   */
  public String getArchiveName() {
    return archiveName;
  }

  /**
   * Set archive name
   * @param archiveName archive name
   */
  @Predicate(uri = "topaz:articleArchiveName")
  public void setArchiveName(String archiveName) {
    this.archiveName = archiveName;
  }

  public List<ArticleContributor> getAuthors() {
    return authors;
  }

  public void setAuthors(List<ArticleContributor> authors) {
    this.authors = authors;
  }

  public List<ArticleContributor> getEditors() {
    return editors;
  }

  public void setEditors(List<ArticleContributor> editors) {
    this.editors = editors;
  }
}
