/*
 * $HeadURL$
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
package org.ambraproject.views;

import org.ambraproject.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * Value object that holds the result of a single search item
 */
public class SearchHit implements Serializable {

  private static final long serialVersionUID = 2450207404766168639L;

  private static final int TITLE_LENGTH = 120;

  private final float hitScore;
  private final String uri;
  private final String title;
  private final String highlight;
  private final Date date;
  private final String creator;
  private final Collection<String> listOfCreators;
  private final String issn;
  private final String journalTitle;
  private final String articleTypeForDisplay;
  private final String abstractText;
  private final String strikingImage;
  private final Boolean hasAssets;

  private SearchHit(Float hitScore, String uri, String title, String highlight,
                    Date date, String creator, Collection<String> listOfCreators, String issn,
                    String journalTitle, String articleTypeForDisplay, String abstractText,
                    String strikingImage, Boolean hasAssets) {

    if (hitScore == null) {
      this.hitScore = 0f;
    } else {
      this.hitScore = hitScore;
    }

    this.uri = uri;
    this.title = title;
    this.highlight = highlight;
    this.date = date;
    this.creator = creator;
    this.listOfCreators = listOfCreators;
    this.issn = issn;
    this.journalTitle = journalTitle;
    this.articleTypeForDisplay = articleTypeForDisplay;
    this.abstractText = abstractText;
    this.strikingImage = strikingImage;
    this.hasAssets = hasAssets;
  }

  /**
   * @return the hit object's uri
   */
  public String getUri() {
    return uri;
  }

  /**
   * Getter for property 'hitScore'.
   *
   * @return Value for property 'hitScore'.
   */
  public float getHitScore() {
    return hitScore;
  }

  /**
   * Getter for property 'creator'.
   *
   * @return Value for property 'creator'.
   */
  public String getCreator() {
    return creator;
  }

  /**
   * Getter for property 'date'.
   *
   * @return Value for property 'date'.
   */
  public Date getDate() {
    return date;
  }

  /**
   * Getter for property 'highlight'.
   *
   * @return Value for property 'highlight'.
   */
  public String getHighlight() {
    return highlight;
  }

  /**
   * Getter for property 'title'.
   *
   * @return Value for property 'title'.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Return truncated article title
   *
   * @return truncated article title
   */
  public String getTruncatedTitle() {
    return TextUtils.truncateTextCloseOpenTag(this.title, TITLE_LENGTH);
  }

  /**
   * Get the issn for the Journal to which this SearchHit belongs.
   *
   * @return The issn for the Journal to which this SearchHit belongs.
   */
  public String getIssn() {
    return issn;
  }

  /**
   * Get the Dublin Core Title for the Journal to which this SearchHit belongs.
   *
   * @return Dublin Core Title for the Journal to which this SearchHit belongs.
   */
  public String getJournalTitle() {
    return journalTitle;
  }

  /**
   * Get the type of the Article as a String ready for display.
   *
   * @return Type of the Article as a String ready for display
   */
  public String getArticleTypeForDisplay() {
    return articleTypeForDisplay;
  }

  /**
   * Get the abstract
   *
   * @return abstract text
   */
  public String getAbstract() {
    return abstractText;
  }

  /**
   * Get the creators in a list
   *
   * @return
   */
  public Collection<String> getListOfCreators() {
    return this.listOfCreators;
  }

  public String getStrikingImage() {
    return strikingImage;
  }

  public Boolean getHasAssets() {
    return hasAssets;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Builder() {
      super();
    }

    private float hitScore;
    private String uri;
    private String title;
    private String highlight;
    private Date date;
    private String creator;
    private Collection<String> listOfCreators;
    private String issn;
    private String journalTitle;
    private String articleTypeForDisplay;
    private String abstractText;
    private String strikingImage;
    private Boolean hasAssets = Boolean.FALSE;

    public Builder setHasAssets(Boolean hasAssets) {
      this.hasAssets = hasAssets;
      return this;
    }

    public Builder setHitScore(float hitScore) {
      this.hitScore = hitScore;
      return this;
    }

    public Builder setUri(String uri) {
      this.uri = uri;
      return this;
    }

    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder setHighlight(String highlight) {
      this.highlight = highlight;
      return this;
    }

    public Builder setDate(Date date) {
      this.date = date;
      return this;
    }

    public Builder setListOfCreators(Collection<String> listOfCreators) {
      this.listOfCreators = listOfCreators;
      this.creator = StringUtils.join(listOfCreators, ", ");
      return this;
    }

    public Builder setIssn(String issn) {
      this.issn = issn;
      return this;
    }

    public Builder setJournalTitle(String journalTitle) {
      this.journalTitle = journalTitle;
      return this;
    }

    public Builder setArticleTypeForDisplay(String articleTypeForDisplay) {
      this.articleTypeForDisplay = articleTypeForDisplay;
      return this;
    }

    public Builder setAbstractText(String abstractText) {
      this.abstractText = abstractText;
      return this;
    }

    public Builder setStrikingImage(String strikingImage) {
      this.strikingImage = strikingImage;
      return this;
    }

    public SearchHit build() {
      return new SearchHit(
        hitScore,
        uri,
        title,
        highlight,
        date,
        creator,
        listOfCreators,
        issn,
        journalTitle,
        articleTypeForDisplay,
        abstractText,
        strikingImage,
        hasAssets);
    }

  }

  @Override
  public String toString() {
    return "SearchHit{" +
      "hitScore=" + hitScore +
      ", uri='" + uri + '\'' +
      ", title='" + title + '\'' +
      ", highlight='" + highlight + '\'' +
      ", date=" + date +
      ", creator='" + creator + '\'' +
      ", issn='" + issn + '\'' +
      ", journalTitle='" + journalTitle + '\'' +
      ", articleTypeForDisplay='" + articleTypeForDisplay + '\'' +
      ", abstract='" + abstractText + '\'' +
      ", strikingImage='" + strikingImage + '\'' +
      ", hasAssets='" + hasAssets + '\'' +
      '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SearchHit searchHit = (SearchHit) o;

    return uri.equals(searchHit.uri);

  }

  @Override
  public int hashCode() {
    return uri.hashCode();
  }

}