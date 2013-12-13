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
import java.util.List;

/**
 * Value object that holds the result of a single search item
 */
public class SearchHit implements Serializable {

  private static final long serialVersionUID = 2450207404766168639L;

  private static final int TITLE_LENGTH = 120;

  private final float hitScore;
  private final String uri;
  private final String title;
  private final Date date;
  private final String creator;
  private String firstSecondLastCreator;
  private final List<String> listOfCreators;
  private final String issn;
  private final String journalTitle;
  private final String articleTypeForDisplay;
  private String abstractText;
  private String strikingImage;
  private Boolean hasAssets = Boolean.FALSE;
  private Collection<String> subjects;
  private Collection<String> subjectsPolyhierarchy;

  /**
   * Create a search hit with the values set
   *
   * @param hitScore              Hit score
   * @param uri                   Article ID
   * @param title                 Article title
   * @param creators              Creators
   * @param date                  Article date
   * @param issn                  eIssn of the journal
   * @param journalTitle          Journal title
   * @param articleTypeForDisplay Article type
   * @param strikingImage
   * @param hasAssets
   */
  public SearchHit(Float hitScore, String uri, String title,
                   List<String> creators, Date date, String issn,
                   String journalTitle, String articleTypeForDisplay, String abstractText,
                   Collection<String> subjects, Collection<String> subjectsPolyhierarchy, String strikingImage, boolean hasAssets) {
    if (hitScore == null) {
      this.hitScore = 0f;
    } else {
      this.hitScore = hitScore;
    }
    this.uri = uri;
    this.title = title;
    this.creator = StringUtils.join(creators, ", ");
    this.listOfCreators = creators;
    this.date = date;
    this.issn = issn;
    this.journalTitle = journalTitle;
    this.articleTypeForDisplay = articleTypeForDisplay;
    this.abstractText = abstractText;
    this.subjects = subjects;
    this.subjectsPolyhierarchy = subjectsPolyhierarchy;
    this.strikingImage = strikingImage;
    this.hasAssets = hasAssets;

    //Make a list of first, second, third creators
    if(creators != null) {
      this.firstSecondLastCreator = TextUtils.makeAuthorString(creators.toArray(new String[creators.size()]));
    }
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
   * Get the subjects
   *
   * @return a collection of subjects
   */
  public Collection<String> getSubjects() {
    return subjects;
  }

  /**
   * Get the subjects Polyhierarchy
   *
   * @return
   */
  public Collection<String> getSubjectsPolyhierarchy() {
    return subjectsPolyhierarchy;
  }

  /**
   * Get the creators in a list
   *
   * @return
   */
  public Collection<String> getListOfCreators() {
    return this.listOfCreators;
  }

  public String getFirstSecondLastCreator() {
    return firstSecondLastCreator;
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

    private Float hitScore;
    private String uri;
    private String title;
    private String highlight;
    private Date date;
    private List<String> listOfCreators;
    private Collection<String> subjects;
    private Collection<String> subjectsPolyhierarchy;
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

    public Builder setHitScore(Float hitScore) {
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

    public Builder setListOfCreators(List<String> listOfCreators) {
      this.listOfCreators = listOfCreators;

      return this;
    }

    public Builder setSubjects(Collection<String> subjects) {
      this.subjects = subjects;
      return this;
    }

    public Builder setSubjectsPolyhierarchy(Collection<String> subjectsPolyhierarchy) {
      this.subjectsPolyhierarchy = subjectsPolyhierarchy;
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
          listOfCreators,
          date,
          issn,
          journalTitle,
          articleTypeForDisplay,
          abstractText,
          subjects,
          subjectsPolyhierarchy,
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
        ", date=" + date +
        ", creator='" + creator + '\'' +
        ", listOfCreators=" + listOfCreators +
        ", issn='" + issn + '\'' +
        ", journalTitle='" + journalTitle + '\'' +
        ", articleTypeForDisplay='" + articleTypeForDisplay + '\'' +
        ", abstractText='" + abstractText + '\'' +
        ", strikingImage='" + strikingImage + '\'' +
        ", hasAssets=" + hasAssets +
        ", subjects=" + subjects +
        ", subjectsPolyhierarchy=" + subjectsPolyhierarchy +
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
