/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.article.action;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.configuration.Configuration;

import org.plos.action.BaseActionSupport;
import org.plos.article.service.BrowseService;
import org.plos.configuration.ConfigurationStore;
import org.plos.journal.JournalService;
import org.plos.models.Journal;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.util.TransactionHelper;

/**
 * @author stevec
 */
public class BrowseArticlesAction extends BaseActionSupport {

  private static final Log           log  = LogFactory.getLog(BrowseArticlesAction.class);
  private static final Configuration CONF = ConfigurationStore.getInstance().getConfiguration();

  private static final String feedCategoryPrefix =
                                      CONF.getString("pub.feed.categoryPrefix", "feed?category=");

  private static final int    PAGE_SIZE    = 10;
  private static final String DATE_FIELD   = "date";
  private static final String ISSUE_FIELD  = "issue";
  private static final String VOLUME_FIELD = "volume";

  private static final int UNSET      = -1;
  private static final int PAST_MONTH = -2;
  private static final int PAST_3MON  = -3;

  private String field;
  private String catName;
  private int    startPage;
  private int    pageSize = PAGE_SIZE;
  private int    year     = UNSET;
  private int    month    = UNSET;
  private int    day      = UNSET;
  private String issue;

  private Session                         session;
  private JournalService                  journalService;
  private BrowseService                   browseService;
  private SortedMap<String, Integer>      categoryInfos;
  private BrowseService.Years             articleDates;
  private List<BrowseService.ArticleInfo> articleList;
  private int                             totalArticles;
  private BrowseService.IssueInfo         issueInfo;
  private List<BrowseService.VolumeInfo>  volumeInfos;

  public String execute() throws Exception {
    if (DATE_FIELD.equals(getField())) {
      return browseDate();
    } else if (ISSUE_FIELD.equals(getField())) {
      return browseIssue();
    } else if (VOLUME_FIELD.equals(getField())) {
      return browseVolume();
    } else {
      return browseCategory();
    }
  }

  private String browseCategory () {
    categoryInfos = browseService.getCategoryInfos();

    int[] numArt = new int[1];
    articleList = (catName != null) ?
        browseService.getArticlesByCategory(catName, startPage, pageSize, numArt) :
        Collections.<BrowseService.ArticleInfo>emptyList();
    totalArticles = numArt[0];

    return SUCCESS;
  }

  private String browseDate() {
    Calendar startDate = Calendar.getInstance();
    startDate.set(Calendar.HOUR_OF_DAY, 0);
    startDate.set(Calendar.MINUTE,      0);
    startDate.set(Calendar.SECOND,      0);
    startDate.set(Calendar.MILLISECOND, 0);

    Calendar endDate;

    if (getYear() > UNSET && getMonth() > UNSET && getDay () > UNSET) {
      startDate.set(getYear(), getMonth() - 1, getDay());
      endDate = (Calendar) startDate.clone();
      endDate.add(Calendar.DATE, 1);

    } else if (getYear() > UNSET && getMonth() > UNSET) {
      startDate.set(getYear(), getMonth() - 1, 1);
      endDate = (Calendar) startDate.clone();
      endDate.add(Calendar.MONTH, 1);

    } else if (getMonth() == PAST_MONTH) {
      endDate = (Calendar) startDate.clone();
      startDate.add(Calendar.MONTH, -1);
      endDate.add(Calendar.DATE, 1);

    } else if (getMonth() == PAST_3MON) {
      endDate = (Calendar) startDate.clone();
      startDate.add(Calendar.MONTH, -3);
      endDate.add(Calendar.DATE, 1);

    } else {
      endDate = (Calendar) startDate.clone();
      startDate.add(Calendar.DATE, -7);
      endDate.add(Calendar.DATE, 1);
    }

    int[] numArt = new int[1];
    articleList =
        browseService.getArticlesByDate(startDate, endDate, startPage, pageSize, numArt);
    totalArticles = numArt[0];

    articleDates = browseService.getArticleDates();

    return SUCCESS;
  }

  private String browseIssue() {

    // was issued specified, or use Journal.currentIssue?
    if (issue == null || issue.length() == 0) {
    // JournalService, OTM usage wants to be in a Transaction
    issue = TransactionHelper.doInTx(session,
      new TransactionHelper.Action<String>() {

        public String run(Transaction tx) {

          // get the Journal's currentIssue
          final Journal journal = journalService.getJournal();
          if (journal == null) { return null; }
          final URI currentIssue = journal.getCurrentIssue();
          if (currentIssue == null) { return null; }
          return currentIssue.toString();
        }
      });
    }

    // if still no issue, create an IssueInfo
    if (issue == null || issue.length() == 0) {
      issueInfo = new BrowseService.IssueInfo(null, "No current Issue is defined for this Journal",
        null, null, null, null, null, null, null);

      return SUCCESS;
    }

    // look up Issue
    issueInfo = browseService.getIssueInfo(URI.create(issue));
    if (issueInfo == null) { return ERROR; }

    return SUCCESS;
  }

  private String browseVolume() {

    // look up VolumeInfos
    volumeInfos = browseService.getVolumeInfos();

    return SUCCESS;
  }

  /**
   * @return Returns the field.
   */
  public String getField() {
    if (field == null) {
      field = "";
    }
    return field;
  }

  /**
   * @param field The field to set.
   */
  public void setField(String field) {
    this.field = field;
  }

  /**
   * @return Returns the start.
   */
  public int getStartPage() {
    return startPage;
  }

  /**
   * @param startPage The start to set.
   */
  public void setStartPage(int startPage) {
    this.startPage = startPage;
  }

  /**
   * @return Returns the category name.
   */
  public String getCatName() {
    return (catName != null) ? catName : "";
  }

  /**
   * @param catName The category name to set.
   */
  public void setCatName(String catName) {
    this.catName = catName;
  }

  /**
   * @return Returns the day.
   */
  public int getDay() {
    return day;
  }

  /**
   * @param day The day to set.
   */
  public void setDay(int day) {
    this.day = day;
  }

  /**
   * @return Returns the month.
   */
  public int getMonth() {
    return month;
  }

  /**
   * @param month The month to set.
   */
  public void setMonth(int month) {
    this.month = month;
  }

  /**
   * @return Returns the year.
   */
  public int getYear() {
    return year;
  }

  /**
   * @param year The year to set.
   */
  public void setYear(int year) {
    this.year = year;
  }

  /**
   * @return Returns the pageSize.
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * @param pageSize The pageSize to set.
   */
  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  /**
   * @param issue The issue for ToC view.
   */
  public void setIssue(String issue) {
    this.issue = issue;
  }

  /**
   * return a set of the articleDates broken down by year, month, and day.
   *
   * @return Collection of dates
   */
  public BrowseService.Years getArticleDates() {
    return articleDates;
  }

  /**
   * @return Returns the category infos (currently just number of articles per category) for all
   *         categories
   */
  public SortedMap<String, Integer> getCategoryInfos() {
    return categoryInfos;
  }

  /**
   * @return Returns the article list for this page.
   */
  public Collection<BrowseService.ArticleInfo> getArticleList() {
    return articleList;
  }

  /**
   * @return Returns the total number of articles in the category or date-range
   */
  public int getTotalArticles() {
    return totalArticles;
  }

  /**
   * @return the IssueInfo.
   */
  public BrowseService.IssueInfo getIssueInfo() {
    return issueInfo;
  }

  /**
   * @return the VolumeInfos.
   */
  public List<BrowseService.VolumeInfo> getVolumeInfos() {
    return volumeInfos;
  }

  /**
   * Set the OTM Session.
   *
   * @param session The OTM Session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * @param browseService The browseService to set.
   */
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }

  /**
   * Sets the JournalService.
   *
   * @param journalService The JournalService to set.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  @Override
  public String getRssName() {
    return (catName != null) ? catName : super.getRssName();
  }

  private static String canonicalCategoryPath(String categoryName) {
    return URLEncoder.encode(categoryName);
  }

  @Override
  public String getRssPath() {
    return (catName != null) ? feedBasePath + feedCategoryPrefix + canonicalCategoryPath(catName) :
                               super.getRssPath();
  }
}
