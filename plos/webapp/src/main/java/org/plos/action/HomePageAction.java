/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.action;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.article.service.BrowseService;
import org.plos.model.article.ArticleInfo;

import org.springframework.beans.factory.annotation.Required;

/**
 * @author stevec
 */
public class HomePageAction extends BaseActionSupport {
  private static final Log log = LogFactory.getLog(HomePageAction.class);

  private BrowseService                   browseService;
  private List<ArticleInfo> recentArticles;
  private SortedMap<String, Integer>      categoryInfos;

  /**
   * This execute method always returns SUCCESS
   */
  public String execute() {
    Calendar startDate = Calendar.getInstance();
    startDate.set(Calendar.HOUR,        0);
    startDate.set(Calendar.MINUTE,      0);
    startDate.set(Calendar.SECOND,      0);
    startDate.set(Calendar.MILLISECOND, 0);

    Calendar endDate = (Calendar) startDate.clone();
    startDate.add(Calendar.DATE, -7);
    endDate.add(Calendar.DATE, 1);

    recentArticles = browseService.getArticlesByDate(startDate, endDate, 0, -1, new int[1]);

    categoryInfos = browseService.getCategoryInfos();

    return SUCCESS;
  }

  /**
   * Retrieves the most recently published articles in the last 7 days
   *
   * @return array of ArticleInfo objects
   */
  public List<ArticleInfo> getRecentArticles() {
    return recentArticles;
  }

  /**
   * @return Returns the category infos (currently just number of articles per category) for all
   *         categories
   */
  public SortedMap<String, Integer> getCategoryInfos() {
    return categoryInfos;
  }

  /**
   * Returns an array of numValues ints which are randomly selected between 0 (inclusive)
   * and maxValue(exclusive). If maxValue is less than numValues, will return maxValue items.
   * Guarantees uniqueness of values.
   *
   * @param numValues
   * @param maxValue
   * @return array of random ints
   */
  public int[] randomNumbers(int numValues, int maxValue) {
    if (numValues > maxValue) {
      numValues = maxValue;
    }

    Random rng = new Random(System.currentTimeMillis());
    Set<Integer> intValues = new HashSet<Integer>();
    while (intValues.size() < numValues) {
      Integer oneNum = rng.nextInt(maxValue);
      if (!intValues.contains(oneNum)) {
        intValues.add(oneNum);
      }
    }

    Iterator<Integer> iter = intValues.iterator();
    int[] returnArray = new int[intValues.size()];
    for (int i = 0; iter.hasNext(); i++) {
      returnArray[i] = iter.next();
    }

    return returnArray;
  }

  /**
   * @param browseService The browseService to set.
   */
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }
}
