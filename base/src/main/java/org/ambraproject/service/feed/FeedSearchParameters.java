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

package org.ambraproject.service.feed;

import org.ambraproject.action.BaseActionSupport;

import java.io.Serializable;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The <code>class FeedSearchParameters</code> serves two function:
 * <ul>
 * <li> It provides the data model used by the action.
 * <li> It relays these input parameters to AmbraFeedResult.
 * </ul>
 *
 * @see       FeedService
 * @see       FeedService.FEED_TYPES
 * @see       org.ambraproject.struts2.AmbraFeedResult
 */

public class FeedSearchParameters implements Serializable  {
  private static final long serialVersionUID = 1L;
  private String journal;
  private Date sDate;
  private Date eDate;

  // Fields set by Struts
  private String startDate;
  private String endDate;
  private String[] categories;
  private String author;
  private boolean relLinks = false;
  private boolean extended = false;
  private String title;
  private String selfLink;
  private int maxResults;
  private URI issueURI;
  private String type;
  private boolean mostViewed = false;
  private String formatting;

  // rss for search
  private String query = "";


  // Used by Find An Article search
  private String        volume                = "";
  private String        eLocationId           = "";
  private String        id                    = ""; // One DOI.  These are indexed as "ID" in Solr
  // Used to create query filters on any search
  private String[]      filterSubjects      = {}; // Only search in these subjects
  private String        filterKeyword         = ""; // Only search in this document part
  private String        filterArticleType     = ""; // Only search for this article type
  private String[]      filterJournals        = {}; // Only search these Journals. If no elements, then default to the current journal
  // Controls results order
  private String        sort                  = "";
  // Used only by Query Builder search
  private String  unformattedQuery      = "";


  final SimpleDateFormat dateFrmt = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * Key Constructor - currently does nothing.
   */
  public FeedSearchParameters() {
    type = FeedService.FEED_TYPES.Article.toString();
  }

  /**
   * The ArticleFeed supports the ModelDriven interface.  The Key class is the data model used by
   * ArticleFeed and validates user input parameters. By the time ArticleFeed.execute is invoked
   * the parameters should be a usable state.
   * <p/>
   * Defined a Maximum number of result = 200 articles.  Both sDate and eDate will not be null by
   * the end of validate.  If sDate &gt; eDate then set sDate = eDate.
   *
   * @param action - the BaseSupportAction allows reporting of field errors. Pass in a reference
   *               incase we want to report them.
   * @see FeedService
   */
  @SuppressWarnings("UnusedDeclaration")
  public void validate(BaseActionSupport action) {
    final int defaultMaxResult = 30;
    final int MAXIMUM_RESULTS = 200;

    try {
      if (startDate != null)
        setSDate(startDate);

      if (endDate != null)
        setEDate(endDate);
    } catch (ParseException e) {
      action.addFieldError("Feed date parsing error.", "endDate or startDate");
    }

    // If start > end then just use end.
    if ((sDate != null) && (eDate != null) && (sDate.after(eDate)))
      sDate = eDate;

    // If there is garbage in the type default to Article
    if (feedType() == FeedService.FEED_TYPES.Invalid)
      type = FeedService.FEED_TYPES.Article.toString();

    // Need a positive non-zero number of results
    if (maxResults <= 0)
      maxResults = defaultMaxResult;
    else if (maxResults > MAXIMUM_RESULTS)   // Don't let them crash our servers.
      maxResults = MAXIMUM_RESULTS;
  }

  /**
   * Determine the feed type by comparing it to each of the
   * enumerated feed types until there is a match or return
   * Invalid.
   *
   * @return FEED_TYPES (the Invalid type signifies that the
   *         type field contains a string that does not match
   *         any of the types)
   */
  public FeedService.FEED_TYPES feedType() {
    FeedService.FEED_TYPES t;
    try {
      t = FeedService.FEED_TYPES.valueOf(type);
    } catch (Exception e) {
      // It's ok just return invalid.
      t = FeedService.FEED_TYPES.Invalid;
    }
    return t;
  }

  public String getJournal() {
    return journal;
  }

  public void setJournal(String journal) {
    this.journal = journal;
  }

  public Date getSDate() {
    return sDate;
  }

  /**
   * Convert the string to a date if possible else leave the startDate null.
   *
   * @param date string date to be converted to Date
   * @throws ParseException date failed to parse
   */
  public void setSDate(String date) throws ParseException {
    this.sDate = dateFrmt.parse(date);
  }

  public void setSDate(Date date) {
    this.sDate = date;
  }

  public Date getEDate() {
    return eDate;
  }

  /**
   * Convert the string to a date if possible else leave the endDate null.
   *
   * @param date string date to be converted to Date
   * @throws ParseException date failed to parse
   */
  public void setEDate(String date) throws ParseException {
    this.eDate = dateFrmt.parse(date);
  }

  public void setEDate(Date date) {
    this.eDate = date;
  }

  public String[] getCategories() {
    return this.categories;
  }

  public void setCategories(String[] categories) {
    this.categories = categories;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public boolean isRelLinks() {
    return relLinks;
  }

  public boolean getRelativeLinks() {
    return relLinks;
  }

  public void setRelativeLinks(boolean relative) {
    this.relLinks = relative;
  }

  public boolean isExtended() {
    return extended;
  }

  public boolean getExtended() {
    return extended;
  }

  public void setExtended(boolean extended) {
    this.extended = extended;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSelfLink() {
    return selfLink;
  }

  public void setSelfLink(String link) {
    this.selfLink = link;
  }

  public String getStartDate() {
    return this.startDate;
  }

  public void setStartDate(String date) {
    this.startDate = date;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String date) {
    this.endDate = date;
  }

  public int getMaxResults() {
    return maxResults;
  }

  public void setMaxResults(int max) {
    this.maxResults = max;
  }

  public void setIssueURI(String issueURI) {
    try {
      this.issueURI = URI.create(issueURI.trim());
    } catch (Exception e) {
      this.issueURI = null;
    }
  }

  public String getIssueURI() {
    return (issueURI != null) ? issueURI.toString() : null;
  }

  // use only to retrieve most viewed *articles*

  public boolean isMostViewed() {
    return mostViewed;
  }

  public void setMostViewed(boolean mostViewed) {
    this.mostViewed = mostViewed;
  }

  public String getFormatting() {
    return formatting;
  }

  public void setFormatting(String formatting) {
    this.formatting = formatting;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    if (query == null || query.trim().length() < 1)
      this.query = "";
    else
      this.query = query.trim();
  }


  public String getUnformattedQuery() {
    return unformattedQuery;
  }

  public void setUnformattedQuery(String unformattedQuery) {
    if (unformattedQuery == null || unformattedQuery.trim().length() < 1)
      this.unformattedQuery = "";
    else
      this.unformattedQuery = unformattedQuery.trim();
  }

  public String getVolume() {
    return volume;
  }

  public void setVolume(String volume) {
    if (volume == null || volume.trim().length() < 1)
      this.volume = "";
    else
      this.volume = volume.trim();
  }

  public String geteLocationId() {
    return eLocationId;
  }

  public void seteLocationId(String eLocationId) {
    if (eLocationId == null || eLocationId.trim().length() < 1)
      this.eLocationId = "";
    else
      this.eLocationId = eLocationId.trim();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    if (id == null || id.trim().length() < 1)
      this.id = "";
    else
      this.id = id.trim();
  }

  public String[] getFilterSubjects() {
    return filterSubjects;
  }

  public void setFilterSubjects(String subjectString) {
    String[] subjects = subjectString.split(",");
    if (subjects == null || subjects.length < 1) {
      this.filterSubjects = new String[]{};
    } else {
      List<String> filterSubjectsList = new ArrayList<String>();
      for (String subject : subjects) {
        if (subject != null && subject.trim().length() > 0)
          filterSubjectsList.add(subject.trim());
      }
      this.filterSubjects = new String[filterSubjectsList.size()];
      filterSubjectsList.toArray(this.filterSubjects);
    }
  }

  public String getFilterKeyword() {
    return filterKeyword;
  }

  public void setFilterKeyword(String filterKeyword) {
    if (filterKeyword == null || filterKeyword.trim().length() < 1)
      this.filterKeyword = "";
    else
      this.filterKeyword = filterKeyword.trim();
  }

  public String getFilterArticleType() {
    return filterArticleType;
  }

  public void setFilterArticleType(String filterArticleType) {
    if (filterArticleType == null || filterArticleType.trim().length() < 1)
      this.filterArticleType = "";
    else
      this.filterArticleType = filterArticleType.trim();
  }

  public String[] getFilterJournals() {
    return filterJournals;
  }

  public void setFilterJournals(String journalString) {
    String[] journals = journalString.split(",");
    if (journals == null || journals.length < 1) {
      this.filterJournals = new String[]{};
    } else {
      List<String> filterJournalsList = new ArrayList<String>();
      for (String journal : journals) {
        if (journal != null && journal.trim().length() > 0)
          filterJournalsList.add(journal.trim());
      }
      this.filterJournals = new String[filterJournalsList.size()];
      filterJournalsList.toArray(this.filterJournals);
    }
  }

  public String getSort() {
    return sort;
  }

  public void setSort(String sort) {
    if (sort == null || sort.trim().length() < 1)
      this.sort = "";
    else
      this.sort = sort.trim();
  }

}




