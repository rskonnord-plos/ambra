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

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.article.service.ArticleOtmService;
import org.plos.article.service.BrowseService;
import org.plos.models.Article;
import org.plos.util.CacheAdminHelper;

import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Set;

/**
 * @author stevec
 *
 */
public class HomePageAction extends BaseActionSupport {

  private static final Log log = LogFactory.getLog(HomePageAction.class);
  private ArticleOtmService articleOtmService;
  private BrowseService browseService;
  private Article[] lastWeeksArticles;
  private static final long ONE_WEEK = 7 * 24 * 60 * 60 * 1000;
  private static final long FIFTEEN_DAYS = 15 * 24 * 60 * 60 * 1000;
  private GeneralCacheAdministrator articleCacheAdministrator;
  public static final String WEEK_ARTICLE_CACHE_KEY = "WEEK_ARTICLE_LIST";
  private static final int WEEK_ARTICLE_CACHE_DURATION = 43200; //12hrs
  private static final String MOST_COMMENTED_CACHE_KEY = "MOST_COMMENTED_LIST";
  private static final int MOST_COMMENTED_CACHE_DURATION = 3600;  //1 hr


  /**
   * This execute method always returns SUCCESS
   *
   */
  public String execute() throws Exception {
    return SUCCESS;
  }

  /**
   * Retrieves the most recently published articles in the last 7 days
   *
   * @return array of Article objects
   */
  public Article[] getRecentArticles() {
    return getLastWeeksArticles();
  }

  private Article[] getLastWeeksArticles() {
    if (lastWeeksArticles == null) {
      lastWeeksArticles = CacheAdminHelper.getFromCache(articleCacheAdministrator,
                                                        WEEK_ARTICLE_CACHE_KEY,
                                                        WEEK_ARTICLE_CACHE_DURATION, null,
                                                        "last week's articles",
                                             new CacheAdminHelper.CacheUpdater<Article[]>() {
          public Article[] lookup(boolean[] updated) {
            Date weekAgo = new Date();
            weekAgo.setTime(weekAgo.getTime() - FIFTEEN_DAYS);

            Article[] res;
            try {
              res = articleOtmService.getArticles(weekAgo.toString(), null, null, null,
                                                  new int[] { Article.STATE_ACTIVE }, false);
              updated[0] = true;
            } catch (ParseException pe) {
              log.error("Internal error", pe);
              res = new Article[0];
            }

            return res;
          }
        }
      );
    }

    return lastWeeksArticles;
  }


  /**
   * Return an array of Article's representing the maxArticles most commented on articles
   *
   * @param maxArticles
   * @return Article[] of maxArticles maximum size
   */
  public Article[] getCommentedOnArticles(final int maxArticles) {
    if (log.isDebugEnabled())
      log.debug("Calling getCommentedOnArticles with " + maxArticles + " maxArticles");

    return CacheAdminHelper.getFromCache(articleCacheAdministrator,
                                         MOST_COMMENTED_CACHE_KEY + maxArticles,
                                         MOST_COMMENTED_CACHE_DURATION, null,
                                         "most commented articles",
                                         new CacheAdminHelper.CacheUpdater<Article[]>() {
        public Article[] lookup(boolean[] updated) {
          Article[] res = articleOtmService.getCommentedArticles(maxArticles);
          updated[0] = true;
          return res;
        }
      }
    );
  }

  /**
   * @return Returns the articleOtmService.
   */
  protected ArticleOtmService getArticleOtmService() {
    return articleOtmService;
  }

  /**
   * @param articleOtmService The articleOtmService to set.
   */
  public void setArticleOtmService(ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  /**
   * @return Returns the articlesByCategory - a two dimensional array of Article
   *          objects to go along with getCategoryNames.
   */
  public ArrayList<ArrayList<Article>> getArticlesByCategory() {
    return browseService.getArticlesByCategory();
  }

  /**
   * @return Returns the categoryNames for articles in the last week in a sorted array.
   */
  public String[] getCategoryNames() {
    return browseService.getCategoryNames();
  }

  /**
   * returns an array of numValues ints which are randomly selected between 0 (inclusive)
   * and maxValue(exclusive). If maxValue is less than numValues, will return maxValue items.
   * Guarantees uniquness of values.
   *
   * @param numValues
   * @param maxValue
   * @return array of random ints
   */

  public int[] randomNumbers (int numValues, int maxValue) {
    if (maxValue < numValues) {
      numValues = maxValue;
    }
    LinkedHashMap<Integer,Integer> values = new LinkedHashMap<Integer,Integer>();
    int oneNum;
    boolean found;
    for (int i = 0; i < numValues; i++) {
      found = false;
      do{
        oneNum = RandomUtils.nextInt(new Random(System.currentTimeMillis()), Integer.MAX_VALUE );
        if (!values.containsKey(oneNum % maxValue)) {
          values.put(oneNum % maxValue, null);
          found = true;
        }
      } while (!found);
    }
    Set<Integer> intValues = values.keySet();
    Iterator<Integer> iter = intValues.iterator();
    int[] returnArray = new int[intValues.size()];
    for (int i = 0; iter.hasNext(); i++) {
      returnArray[i] = iter.next();
    }
    return returnArray;
  }

  /**
   * @return Returns the articleCacheAdministrator.
   */
  public GeneralCacheAdministrator getArticleCacheAdministrator() {
    return articleCacheAdministrator;
  }

  /**
   * @param articleCacheAdministrator The articleCacheAdministrator to set.
   */
  public void setArticleCacheAdministrator(GeneralCacheAdministrator articleCacheAdministrator) {
    this.articleCacheAdministrator = articleCacheAdministrator;
  }

  /**
   * @param browseService The browseService to set.
   */
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }
}
