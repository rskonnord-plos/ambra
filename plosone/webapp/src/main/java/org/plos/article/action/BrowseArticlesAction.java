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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.action.BaseActionSupport;
import org.plos.article.service.ArticleInfo;
import org.plos.article.service.BrowseService;

/**
 * @author stevec
 *
 */
public class BrowseArticlesAction extends BaseActionSupport  {

  private static final Log log = LogFactory.getLog(BrowseArticlesAction.class);
  private String field;
  private int catId;
  private int startPage;
  private int pageSize;
  private int year = -1;
  private int month = -1;
  private int day = -1;
  private BrowseService browseService;
  private String[] categoryNames;
  private ArrayList<ArrayList<ArticleInfo>> articlesByCategory;
  private ArrayList<ArrayList<ArrayList<ArrayList<ArticleInfo>>>> articlesByDate;
  private ArrayList<ArrayList<ArrayList<Date>>> articleDates;
  private ArrayList<ArticleInfo> articleList;
  
  private static final int PAGE_SIZE = 10;
  private static final String DATE_FIELD = "date"; 

  

  
  public String execute() throws Exception {
    articlesByCategory = browseService.getArticlesByCategory();
    articlesByDate = browseService.getArticlesByDate();
    articleDates = browseService.getArticleDates();
    categoryNames = browseService.getCategoryNames();
    if (DATE_FIELD.equals(getField())) { 
      return browseDate();
    } else {
      return browseCategory();
    }
  }
  
  private String browseCategory () {
    articleList = articlesByCategory.get(catId);
    return SUCCESS;
  }

  private String browseDate() {
    if (getYear() > -1 && getMonth() > -1 && getDay () > -1) {
      articleList = articlesByDate.get(getYear()).get(getMonth()).get(getDay());
    } else if (getYear() > -1 && getMonth() > -1) {
      articleList = new ArrayList<ArticleInfo>();
      Iterator <ArrayList<ArticleInfo>> iter = articlesByDate.get(getYear()).get(getMonth()).iterator();
      while (iter.hasNext()) {
        articleList.addAll(iter.next());
      }
    } else if (getMonth() == -2) {
      int sizeA = articlesByDate.size();
      int sizeB = articlesByDate.get(sizeA - 1).size();
      articleList = new ArrayList<ArticleInfo>();
      Iterator <ArrayList<ArticleInfo>> iter = articlesByDate.get(sizeA - 1).get(sizeB - 1).iterator();
      while (iter.hasNext()) {
        articleList.addAll(iter.next());
      }
    } else if (getMonth() == -3) {
      int sizeA = articlesByDate.size();
      int sizeB = articlesByDate.get(sizeA - 1).size();
      articleList = new ArrayList<ArticleInfo>();
      int i = 3;
      int indexA = sizeA - 1;
      int indexB = sizeB - 1;
      while (i > 0) {
        if (indexB >= 0) {
          //do nothing
        } else if (indexA > 0) {
          indexA --;
          indexB = articlesByDate.get(indexA).size() - 1;
        } else {
          break;
        }
        Iterator <ArrayList<ArticleInfo>> iter = articlesByDate.get(indexA).get(indexB).iterator();
        ArrayList<ArticleInfo> tempArrayList = new ArrayList<ArticleInfo>();
        while (iter.hasNext()) {
          tempArrayList.addAll(iter.next());
        }
        articleList.addAll(0, tempArrayList);
        indexB--;
        i--;
      }
    } else {
      int sizeA = articlesByDate.size();
      int sizeB = articlesByDate.get(sizeA - 1).size();
      int sizeC = articlesByDate.get(sizeA - 1).get(sizeB - 1).size();
      articleList = articlesByDate.get(sizeA - 1).get(sizeB - 1).get(sizeC - 1);
    }
    return SUCCESS;
  }

  /**
   * return a set of the articleDates broken down by year, month, and day.
   * 
   * @return Collection of dates
   */
  public Collection<ArrayList<ArrayList<Date>>> getArticleDates() {
    return articleDates;
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
   * @return Returns the catId.
   */
  public int getCatId() {
    return catId;
  }

  /**
   * @param catId The catId to set.
   */
  public void setCatId(int catId) {
    this.catId = catId;
  }

  /**
   * @return Returns the categoryNames.
   */
  public String[] getCategoryNames() {
    return categoryNames;
  }

  /**
   * @return Returns the articlesByCategory.
   */
  public ArrayList<ArrayList<ArticleInfo>> getArticlesByCategory() {
    return articlesByCategory;
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
   * @return Returns the articleList.
   */
  public Collection<ArticleInfo> getArticleList() {
    return articleList;
  }

  /**
   * @return Returns the pageSize.
   */
  public int getPageSize() {
    if (pageSize == 0) {
      pageSize = PAGE_SIZE;
    }
    return pageSize;
  }

  /**
   * @param pageSize The pageSize to set.
   */
  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  /**
   * @param browseService The browseService to set.
   */
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }
  
  
}
