/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
 *    http://plos.org
 *    http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.views;

import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAuthor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CitationView {
  private static final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
  private static final SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
  private static final SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
  private static final String INFO_DOI_PREFIX = "info:doi/";

  private final String doi;
  private final String eLocationId;
  private final String url;
  private final String title;
  private final String journal;
  private final String volume;
  private final String issue;
  private final String summary;
  private final String publisherName;
  private final Date publishedDate;
  //needs to not be named 'authors' else it will conflict with some freemarker variables in the global nav bar
  private final List<ArticleAuthor> authorList;
  private final List<String> collaborativeAuthors;

  private final String month;
  private final String day;
  private final String year;

  private CitationView(String doi,
                       String eLocationId,
                       String url,
                       String title,
                       String journal,
                       String volume,
                       String issue,
                       String summary,
                       String publisherName,
                       Date publishedDate,
                       List<ArticleAuthor> authorList,
                       List<String> collaborativeAuthors,
                       String month,
                       String day,
                       String year){
    this.doi = doi;
    this.eLocationId = eLocationId;
    this.url = url;
    this.title = title;
    this.journal = journal;
    this.volume = volume;
    this.issue = issue;
    this.summary = summary;
    this.publisherName = publisherName;
    this.publishedDate = publishedDate;
    this.authorList = authorList;
    this.collaborativeAuthors = collaborativeAuthors;
    this.month = month;
    this.day = day;
    this.year = year;
  }

  public String getDoi() {
    return doi;
  }

  public String geteLocationId() {
    return eLocationId;
  }

  public String getUrl() {
    return url;
  }

  public String getTitle() {
    return title;
  }

  public String getJournal() {
    return journal;
  }

  public String getVolume() {
    return volume;
  }

  public String getIssue() {
    return issue;
  }

  public String getSummary() {
    return summary;
  }

  public String getPublisherName() {
    return publisherName;
  }

  public Date getPublishedDate() {
    return publishedDate;
  }

  public List<ArticleAuthor> getAuthorList() {
    return authorList;
  }

 public List<String> getCollaborativeAuthors() {
    return collaborativeAuthors;
  }

  public String getMonth() {
    return month;
  }

  public String getDay() {
    return day;
  }

  public String getYear() {
    return year;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Builder() {
      super();
    }

    private String doi;
    private String eLocationId;
    private String url;
    private String title;
    private String journal;
    private String volume;
    private String issue;
    private String summary;
    private String publisherName;
    private Date publishedDate;
    //needs to not be named 'authors' else it will conflict with some freemarker variables in the global nav bar
    private List<ArticleAuthor> authorList;
    private List<String> collaborativeAuthors;


    public Builder setDoi(String doi) {
      this.doi = doi;
      return this;
    }

    public Builder seteLocationId(String eLocationId) {
      this.eLocationId = eLocationId;
      return this;
    }

    public Builder setUrl(String url) {
      this.url = url;
      return this;
    }

    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder setJournal(String journal) {
      this.journal = journal;
      return this;
    }

    public Builder setVolume(String volume) {
      this.volume = volume;
      return this;
    }

    public Builder setIssue(String issue) {
      this.issue = issue;
      return this;
    }

    public Builder setSummary(String summary) {
      this.summary = summary;
      return this;
    }

    public Builder setPublisherName(String publisherName) {
      this.publisherName = publisherName;
      return this;
    }

    public Builder setPublishedDate(Date publishedDate) {
      this.publishedDate = publishedDate;
      return this;
    }

    public Builder setAuthorList(List<ArticleAuthor> authorList) {
      this.authorList = authorList;
      return this;
    }

    public Builder setCollaborativeAuthors(List<String> collaborativeAuthors) {
      this.collaborativeAuthors = collaborativeAuthors;
      return this;
    }


    public CitationView build() {
      return new CitationView(doi.replaceAll(INFO_DOI_PREFIX,""),
              eLocationId,
              url,
              title,
              journal,
              volume,
              issue,
              summary,
              publisherName,
              publishedDate,
              authorList,
              collaborativeAuthors,
              monthFormat.format(publishedDate),
              dayFormat.format(publishedDate),
              yearFormat.format(publishedDate));
    }
  }

}


