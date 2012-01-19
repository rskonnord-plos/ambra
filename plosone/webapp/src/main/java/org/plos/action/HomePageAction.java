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

import org.plos.article.service.ArticleInfo;
import org.plos.article.service.ArticleOtmService;
import org.plos.article.service.BrowseService;
import org.plos.models.ObjectInfo;
import static org.plos.models.Article.STATE_ACTIVE;

import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import com.opensymphony.oscache.base.NeedsRefreshException;

import java.rmi.RemoteException;
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
  private ArticleInfo[] lastWeeksArticles;
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
   * @return array of ArticleInfo objects
   */
  public ArticleInfo[] getRecentArticles() {
    return getLastWeeksArticles();
  }

  private ArticleInfo[] getLastWeeksArticles() {
    if (lastWeeksArticles == null) {
      try {
        // Get from the cache

        lastWeeksArticles= (ArticleInfo[])
          articleCacheAdministrator.getFromCache(WEEK_ARTICLE_CACHE_KEY, WEEK_ARTICLE_CACHE_DURATION);
        if (log.isDebugEnabled()) {
          log.debug("retrieved last week's articles from cache");
        }
      } catch (NeedsRefreshException nre) {
        boolean updated = false;
        try {
          //  Get the value from TOPAZ
          Date weekAgo = new Date();
          weekAgo.setTime(weekAgo.getTime() - FIFTEEN_DAYS);
          if (log.isDebugEnabled()){
            log.debug("retrieving last week's articles from TOPAZ");
          }
          lastWeeksArticles = articleOtmService.getArticleInfos(weekAgo.toString(), null, null,
                              null, new int[]{STATE_ACTIVE}, false);

          // Store in the cache
          articleCacheAdministrator.putInCache(WEEK_ARTICLE_CACHE_KEY, lastWeeksArticles);
          updated = true;
        } catch (RemoteException re) {
          log.error("Could not retrieve the most recent articles", re);
          lastWeeksArticles = new ArticleInfo[0];
        } finally {
          if (!updated) {
              // It is essential that cancelUpdate is called if the
              // cached content could not be rebuilt
              articleCacheAdministrator.cancelUpdate(WEEK_ARTICLE_CACHE_KEY);
          }
        }
      }
    }
    return lastWeeksArticles;
  }


  /**
   * Return an array of ObjectInfos representing the maxArticles most commented on articles
   *
   * @param maxArticles
   * @return ObjectInfo[] of maxArticles maximum size
   */
  public ObjectInfo[] getCommentedOnArticles(int maxArticles) {
    if (log.isDebugEnabled()){
      log.debug("Calling getCommentedOnArticles with " + maxArticles + " maxArticles");
    }
    ObjectInfo[] retArray = new ObjectInfo[0];
    try {
      // Get from the cache
      retArray= (ObjectInfo[])
        articleCacheAdministrator.getFromCache(MOST_COMMENTED_CACHE_KEY + maxArticles, MOST_COMMENTED_CACHE_DURATION);
      if (log.isDebugEnabled()) {
        log.debug("retrieved most commented from cache");
      }
    } catch (NeedsRefreshException nre) {
      boolean updated = false;
      try {
        //  Get the value from TOPAZ
        if (log.isDebugEnabled()){
          log.debug("retrieving most commented articles from TOPAZ");
        }
        retArray = articleOtmService.getCommentedArticles(maxArticles);
        // Store in the cache
        articleCacheAdministrator.putInCache(MOST_COMMENTED_CACHE_KEY + maxArticles, retArray);
        updated = true;
      } finally {
        if (!updated) {
            // It is essential that cancelUpdate is called if the
            // cached content could not be rebuilt
            articleCacheAdministrator.cancelUpdate(MOST_COMMENTED_CACHE_KEY + maxArticles);
        }
      }
    }
    return retArray;
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
   * @return Returns the articlesByCategory - a two dimensional array of ArticleInfo
   *          objects to go along with getCategoryNames.
   */
  public ArrayList<ArrayList<ArticleInfo>> getArticlesByCategory() {
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
