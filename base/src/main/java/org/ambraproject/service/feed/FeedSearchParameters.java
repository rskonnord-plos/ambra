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
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.net.URI;

/**
 * The <code>class FeedSearchParameters</code> serves two function:
 * <ul>
 * <li> It provides the data model used by the action.
 * <li> It relays these input parameters to AmbraFeedResult.
 * </ul>
 *
 * @see       FeedService
 * @see       FeedService.FEED_TYPES
 * @see       org.ambraproject.struts.AmbraFeedResult
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
}




