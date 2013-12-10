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

  private String month;
  private String day;
  private String year;

  public void buildCitationFromArticle (Article article) {
    this.doi = article.getDoi().replaceAll(INFO_DOI_PREFIX,"");
    this.eLocationId = article.geteLocationId();
    this.url = article.getUrl();
    this.title = article.getTitle();
    this.journal = article.getJournal();
    this.volume = article.getVolume();
    this.issue = article.getIssue();
    this.summary = article.getDescription();
    this.publisherName = article.getPublisherName();
    this.publishedDate = article.getDate();
    this.authorList = article.getAuthors();
    this.collaborativeAuthors = article.getCollaborativeAuthors();

    this.year = yearFormat.format(publishedDate);
    this.month = monthFormat.format(publishedDate);
    this.day = dayFormat.format(publishedDate);
  }

  public String getDoi() {
    return doi;
  }

  public void setDoi(String doi) {
    this.doi = doi;
  }

  public String geteLocationId() {
    return eLocationId;
  }

  public void seteLocationId(String eLocationId) {
    this.eLocationId = eLocationId;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getJournal() {
    return journal;
  }

  public void setJournal(String journal) {
    this.journal = journal;
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

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getPublisherName() {
    return publisherName;
  }

  public void setPublisherName(String publisherName) {
    this.publisherName = publisherName;
  }

  public Date getPublishedDate() {
    return publishedDate;
  }

  public void setPublishedDate(Date publishedDate) {
    this.publishedDate = publishedDate;
  }

  public List<ArticleAuthor> getAuthorList() {
    return authorList;
  }

  public void setAuthorList(List<ArticleAuthor> authorList) {
    this.authorList = authorList;
  }

  public List<String> getCollaborativeAuthors() {
    return collaborativeAuthors;
  }

  public void setCollaborativeAuthors(List<String> collaborativeAuthors) {
    this.collaborativeAuthors = collaborativeAuthors;
  }

  public String getMonth() {
    return month;
  }

  public void setMonth(String month) {
    this.month = month;
  }

  public String getDay() {
    return day;
  }

  public void setDay(String day) {
    this.day = day;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }
}
