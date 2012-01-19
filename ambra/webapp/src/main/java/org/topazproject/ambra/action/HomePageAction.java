/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
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

package org.topazproject.ambra.action;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.article.service.BrowseService;
import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.model.article.ArticleInfo;

/**
 * @author stevec
 */
@SuppressWarnings("serial")
public class HomePageAction extends BaseActionSupport {

  private JournalService journalService;
  private BrowseService                   browseService;
  private SortedMap<String, Integer>      categoryInfos;

  private List<ArticleInfo> recentArticles;
  private int numDaysInPast;
  private int numArticlesToShow;

  private void initRecentArticles() {
    ConfigurationStore config = ConfigurationStore.getInstance();
    String journalKey = journalService.getCurrentJournalKey();
    String rootKey = "ambra.virtualJournals." + journalKey + ".recentArticles";

    numDaysInPast = config.getConfiguration().getInteger(rootKey + ".numDaysInPast", 7).intValue();
    numArticlesToShow = config.getConfiguration().getInteger(rootKey + ".numArticlesToShow", 5)
        .intValue();

    Calendar startDate = Calendar.getInstance();
    startDate.set(Calendar.HOUR, 0);
    startDate.set(Calendar.MINUTE, 0);
    startDate.set(Calendar.SECOND, 0);
    startDate.set(Calendar.MILLISECOND, 0);

    Calendar endDate = (Calendar) startDate.clone();
    startDate.add(Calendar.DATE, -(numDaysInPast));
    endDate.add(Calendar.DATE, 1);

    recentArticles = browseService.getArticlesByDate(startDate, endDate, 0, -1, new int[1]);
  }

  /**
   * This execute method always returns SUCCESS
   */
  @Override
  @Transactional(readOnly = true)
  public String execute() {
    categoryInfos = browseService.getCategoryInfos();

    initRecentArticles();

    return SUCCESS;
  }

  /**
   * @return Returns the category infos (currently just number of articles per category) for all
   *         categories
   */
  public SortedMap<String, Integer> getCategoryInfos() {
    return categoryInfos;
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
   * Returns an array of numValues ints which are randomly selected between 0 (inclusive) and
   * maxValue(exclusive). If maxValue is less than numValues, will return maxValue items. Guarantees
   * uniqueness of values.
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

  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * @param browseService The browseService to set.
   */
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }

  public int getNumDaysInPast() {
    return numDaysInPast;
  }

  public int getNumArticlesToShow() {
    return numArticlesToShow;
  }
}
