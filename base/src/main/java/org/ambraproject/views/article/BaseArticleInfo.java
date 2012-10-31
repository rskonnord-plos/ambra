/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2012 by Public Library of Science
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

import org.ambraproject.views.JournalView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseArticleInfo {
  private static final Logger log = LoggerFactory.getLogger(BaseArticleInfo.class);

  public String doi;
  public Date date;
  protected String title;
  public List<String> authors = new ArrayList<String>();
  public Set<ArticleType> articleTypes = new HashSet<ArticleType>();
  public Set<JournalView> journals = new HashSet<JournalView>();
  protected String eIssn;
  protected Set<String> types;

  /**
   * Set the ID of this Article. This is the Article DOI.
   *
   * @param doi Article ID.
   */
  public void setDoi(String doi) {
    this.doi = doi;
  }

  /**
   * Get the id.
   *
   * @return the id.
   */
  public String getDoi() {
    return doi;
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
   * @param date Article date.
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
   * @param articleTitle Title.
   */
  public void setTitle(String articleTitle) {
    title = articleTitle;
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
   * Set authors
   * @param authors authors
   */
  public void setAuthors(List<String> authors) {
    this.authors = authors;
  }

  /**
   * get the journals that this article is cross published in
   * @return a list of journals
   */
  public Set<JournalView> getJournals() {
    return journals;
  }

  /**
   * set the journals that this article is cross published in
   * @param journals a set of journals
   */
  public void setJournals(Set<JournalView> journals) {
    this.journals = journals;
  }

  /**
   * Get published journal name
   * @return full journal name
   */
  public String getPublishedJournal() {
    String publishedJournal = "";

    for (JournalView j : journals) {
      if (this.eIssn.equals(j.geteIssn())) {
        publishedJournal = j.getTitle();
      }
    }

    return publishedJournal;
  }

  public String geteIssn() {
    return eIssn;
  }

  public void seteIssn(String eIssn) {
    this.eIssn = eIssn;
  }


  public Set<String> getTypes() {
    return types;
  }

  public void setTypes(Set<String> types) {
    this.types = types;
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
   * Get the article type, from the {@code types} field of URIs, that corresponds to a "known" article type in {@code
   * ArticleType}'s static map. At most one value is expected to match a "known" type; if more than one does, an error
   * is logged and an arbitrary one is returned. If none is matched, return the default article type.
   *
   * @return the known article type, or the default
   * @throws IllegalStateException if {@code this.getTypes() == null}
   */
  public ArticleType getKnownArticleType() {
    if (types == null) {
      throw new IllegalStateException("types not set");
    }
    ArticleType knownType = null;
    for (String artType : types) {
      URI articleTypeUri = URI.create(artType);
      ArticleType typeForURI = ArticleType.getKnownArticleTypeForURI(articleTypeUri);
      if (typeForURI != null) {
        if (knownType == null) {
          knownType = typeForURI;
        } else if (!knownType.equals(typeForURI) && log.isErrorEnabled()) {
          /*
           * The old behavior was to return the first value matched from the Set iterator.
           * To avoid introducing bugs, continue without changing the value of knownType.
           */
          log.error("Multiple article types ({}, {}) matched from: {}",
              new String[]{knownType.getHeading(), typeForURI.getHeading(), types.toString()});
        }
      }
    }
    return knownType == null ? ArticleType.getDefaultArticleType() : knownType;
  }

  public void setAt(Set<String> at) {
    articleTypes.clear();
    for (String a : at)
      articleTypes.add(ArticleType.getArticleTypeForURI(URI.create(a), true));
  }

  /**
   * Get a displayable version of the Article Type by doing a lookup on the every element
   * of the Set of all Article Type URIs for this Article.
   * Defaults to "Unclassified".  Never throw an exception.
   * <p/>
   * The first successful lookup is used under the assumption that there is only one legit value.
   * This is a terrible assumption but, because of the terrible implementation of article types,
   * there are few other reasonable options.  This method is a miserable hack that should be
   * removed as soon as article types are implemented in a useful manner.
   *
   * @return The first displayable article type from the Set of Article Types for this Article
   */
  public String getArticleTypeForDisplay() {
    String articleTypeForDisplay = "Unclassified";
    try {
      ArticleType articleType = null;
      for (ArticleType artType : getArticleTypes()) {
        if (ArticleType.getKnownArticleTypeForURI(artType.getUri())!= null) {
          articleType = ArticleType.getKnownArticleTypeForURI(artType.getUri());
          break;
        }
      }
      articleTypeForDisplay = (articleType.getHeading());
    } catch (Exception e) {  // Do not rock the boat.
    }
    return articleTypeForDisplay;
  }
}
