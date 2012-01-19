/* $HeadURL::                                                                            $
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

package org.topazproject.ambra.article.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.article.service.BrowseService;
import org.topazproject.ambra.model.article.Years;
import org.topazproject.ambra.search.SearchHit;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author stevec
 */
@SuppressWarnings("serial")
public class BrowseArticlesAction extends BaseActionSupport {
  private static final Logger log  = LoggerFactory.getLogger(BrowseArticlesAction.class);
  private static final String FEED_BASE_PATH = "ambra.services.feed.basePath";
  private static final String FEED_CATEGORY_PREFIX = "ambra.services.feed.categoryPrefix";

  private static final int    PAGE_SIZE    = 10;
  private static final String DATE_FIELD   = "date";

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

  private BrowseService                   browseService;
  private SortedMap<String, Long> categoryInfos;
  private Years             articleDates;
  private List<SearchHit> articleList;
  private long totalArticles;
  private String startDateParam;
  private String endDateParam;

  @Override
  @Transactional(readOnly = true)
  public String execute() throws Exception {
    try {
      browseService.pingSolr();
    } catch (Exception e) {
      log.error("Could not talk to the Solr Server", e);
      return ERROR;
    }

    if (DATE_FIELD.equals(getField())) {
      return browseDate();
    }
    return browseCategory();
  }

  private String browseCategory() {
    categoryInfos = browseService.getArticlesByCategory(this.getCurrentJournal());
    // if the catName is unspecified, use the first catName in the categoryInfos list
    if ((catName == null) && (categoryInfos != null) && (categoryInfos.size() > 0)) {
      catName = categoryInfos.firstKey();
    }

    if (catName != null) {
      long[] numArt = new long[1];
      articleList = browseService.getArticlesByCategory(catName, startPage, pageSize, numArt, this.getCurrentJournal());
      totalArticles = numArt[0];
    } else {
      articleList = Collections.emptyList();
      totalArticles = 0;
    }

    return SUCCESS;
  }

  private String browseDate() {
    Calendar startDate = Calendar.getInstance();
    startDate.set(Calendar.HOUR_OF_DAY, 0);
    startDate.set(Calendar.MINUTE,      0);
    startDate.set(Calendar.SECOND,      0);
    startDate.set(Calendar.MILLISECOND, 0);

    Calendar endDate;

    if (((startDateParam != null) && startDateParam.length() > 0) ||
        ((endDateParam != null) && endDateParam.length() > 0) ) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      try {
        if (startDateParam != null) {
          startDate.setTime(sdf.parse(startDateParam));
        } else {
          startDate.setTimeInMillis(0);
        }
        endDate = Calendar.getInstance();
        if (endDateParam != null) {
          endDate.setTime(sdf.parse(endDateParam));
        }
      } catch (Exception e) {
        log.error("Failed to parse start / end Date params");
        return ERROR;
      }
    } else {
      if (getYear() > UNSET && getMonth() > UNSET && getDay () > UNSET) {
        // user has clicked on a date link
        startDate.set(getYear(), getMonth() - 1, getDay());
        endDate = (Calendar) startDate.clone();

      } else if (getYear() > UNSET && getMonth() > UNSET) {
        // user has clicked on a month link
        startDate.set(getYear(), getMonth() - 1, 1);
        endDate = (Calendar) startDate.clone();
        int lastDay = endDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        endDate.set(Calendar.DAY_OF_MONTH, lastDay);

      } else if (getMonth() == PAST_MONTH) {
        // user has clicked on the past month link
        endDate = (Calendar) startDate.clone();
        startDate.add(Calendar.MONTH, -1);

      } else if (getMonth() == PAST_3MON) {
        // user has clicked on the past 3 months link
        endDate = (Calendar) startDate.clone();
        startDate.add(Calendar.MONTH, -3);

      } else {
        // user has clicked on the past week link
        endDate = (Calendar) startDate.clone();
        startDate.add(Calendar.DATE, -7);
      }
    }
    long[] numArt = new long[1];
    articleList =
        browseService.getArticlesByDate(startDate, endDate, null, startPage, pageSize, numArt, this.getCurrentJournal());
    totalArticles = numArt[0];

    articleDates = browseService.getArticleDates(this.getCurrentJournal());

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
   * return a set of the articleDates broken down by year, month, and day.
   *
   * @return Collection of dates
   */
  public Years getArticleDates() {
    return articleDates;
  }

  /**
   * @return Returns a list of article-counts for each category.
   */
  public SortedMap<String, Long> getCategoryInfos() {
    return categoryInfos;
  }
  
  /**
   * @return Returns the article list for this page.
   */
  public Collection<SearchHit> getArticleList() {
    return articleList;
  }

  /**
   * @return Returns the total number of articles in the category or date-range
   */
  public long getTotalArticles() {
    return totalArticles;
  }

  /**
   * @param browseService The browseService to set.
   */
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }

  @Override
  public String getRssName() {
    return (catName != null) ? catName : super.getRssName();
  }

  private static String canonicalCategoryPath(String categoryName) {
    try {
      return URLEncoder.encode(categoryName, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new Error(e);
    }
  }

  @Override
  public String getRssPath() {
    if (catName != null)
      return configuration.getString(FEED_BASE_PATH, "/article/")
        + configuration.getString(FEED_CATEGORY_PREFIX, "feed?category=")
        + canonicalCategoryPath(catName);
    else
      return super.getRssPath();
  }

  public void setStartDateParam(String startDateParam) {
    this.startDateParam = startDateParam;
  }

  public void setEndDateParam(String endDateParam) {
    this.endDateParam = endDateParam;
  }
}
