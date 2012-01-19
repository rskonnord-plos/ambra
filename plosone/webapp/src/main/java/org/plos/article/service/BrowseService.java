/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.article.service;

import static org.topazproject.ws.article.Article.ST_ACTIVE;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.ws.article.ArticleInfo;

import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

/**
 * Class to get all ArticleInfos in system and organize them by date and by category
 * 
 * @author stevec
 *
 */
public class BrowseService {
  private static final Log log = LogFactory.getLog(BrowseService.class);

  private GeneralCacheAdministrator articleCacheAdministrator;
  private ArticleWebService articleWebService;

  private Object[] allBrowseObjects;

  private static final int DATES_INDEX= 0;
  private static final int DATES_ARTICLES_INDEX= 1;
  private static final int CAT_NAME_INDEX = 2;
  private static final int CAT_ARTICLES_INDEX = 3;
  
  private static final String ALL_ARTICLE_CACHE_KEY = "ALL_ARTICLE_LIST";
  private static final String ALL_BROWSE_OBJECTS = "ALL_BROWSE_OBJECTS";
  
  public static final String ALL_ARTICLE_CACHE_GROUP_KEY = "ALL_ARTICLE_LIST_GROUP";

  
  
  /**
   * retrieve a listing of all articles in Topaz
   * 
   * @return all articles
   */
  public ArticleInfo[] getAllArticles() {
    ArticleInfo[] allArticles = null;
    try {
      // Get from the cache

      allArticles = (ArticleInfo[]) 
        articleCacheAdministrator.getFromCache(ALL_ARTICLE_CACHE_KEY, CacheEntry.INDEFINITE_EXPIRY);
      if (log.isDebugEnabled()) {
        log.debug("retrieved all articles from cache");
      }
    } catch (NeedsRefreshException nre) {
      boolean updated = false;
      if (log.isDebugEnabled()){
        log.debug("retrieving all articles from TOPAZ");
      }
      try {
        //  Get the value from TOPAZ
        allArticles = articleWebService.getArticleInfos(null, null, null, null, 
                                                        new int[]{ST_ACTIVE}, true);
        
        // Store in the cache
        articleCacheAdministrator.putInCache(ALL_ARTICLE_CACHE_KEY, allArticles, 
                                             new String[]{ALL_ARTICLE_CACHE_GROUP_KEY});
        updated = true;
      } catch (RemoteException re) {
        log.error("Could not retrieve the all articles", re);
        allArticles = new ArticleInfo[0];
      } finally {
        if (!updated) {
            // It is essential that cancelUpdate is called if the
            // cached content could not be rebuilt
            articleCacheAdministrator.cancelUpdate(ALL_ARTICLE_CACHE_KEY);
        }
      }
    }
    return allArticles;
  }
  
  
  /**
   * Takes the articles and sets the categoryNames and articlesByCategory as well as the articleDates
   * and articlesByDate values.  
   */
  private void populateArticlesAndCategories() {
    try {
      allBrowseObjects = (Object[])articleCacheAdministrator.getFromCache (ALL_BROWSE_OBJECTS, CacheEntry.INDEFINITE_EXPIRY);
    } catch (NeedsRefreshException nre) {
      boolean updated = false;
      if (log.isDebugEnabled()){
        log.debug("constructing category and date browse objects");
      }      
      try {
        allBrowseObjects = createBrowseObjects();
        articleCacheAdministrator.putInCache(ALL_BROWSE_OBJECTS, allBrowseObjects, new String[] {ALL_ARTICLE_CACHE_GROUP_KEY});
        updated = true;
      } finally {
        if (!updated) {
          // It is essential that cancelUpdate is called if the
          // cached content could not be rebuilt
          articleCacheAdministrator.cancelUpdate(ALL_BROWSE_OBJECTS);
        }
      }
    }
  }
  
  private Object[] createBrowseObjects() {
    ArrayList<ArrayList<ArticleInfo>> articlesByCategory;
    TreeMap<String, ArrayList<ArticleInfo>> articlesByCategoryMap;
    TreeMap<Date, ArrayList<ArticleInfo>> articlesByDateMap;  
    ArrayList<ArrayList<ArrayList<ArrayList<ArticleInfo>>>> articlesByDate;
    ArrayList<ArrayList<ArrayList<Date>>> articleDates;
    String[] categoryNames;
    
    ArticleInfo[] allArticleList = getAllArticles();
    
    if (allArticleList.length > 0){
      articlesByCategoryMap = new TreeMap<String, ArrayList<ArticleInfo>>();
      articlesByDateMap = new TreeMap<Date, ArrayList<ArticleInfo>>();
      String[] categories;
      ArrayList<ArticleInfo> theList;
      Date theDate;
      for (ArticleInfo art : allArticleList) {
        categories = art.getCategories();
        theDate = art.getArticleDate();
        theList = articlesByDateMap.get(theDate);
        if (theList == null) {
          theList = new ArrayList<ArticleInfo>();
          articlesByDateMap.put(theDate, theList);
        }
        theList.add(0, art);
        for (String cat : categories) {
          theList = articlesByCategoryMap.get(cat);
          if (theList == null) {
            theList = new ArrayList<ArticleInfo>();
            articlesByCategoryMap.put(cat, theList);
          }
          theList.add(art);
        }
      }
      Set<Map.Entry<String, ArrayList<ArticleInfo>>> allEntries = articlesByCategoryMap.entrySet();  
      Iterator<Map.Entry<String, ArrayList<ArticleInfo>>> iter = allEntries.iterator();
      Map.Entry<String, ArrayList<ArticleInfo>> entry;
      categoryNames = new String[allEntries.size()];
      articlesByCategory = new ArrayList<ArrayList<ArticleInfo>>(allEntries.size());
      ArrayList<ArticleInfo> artInfoArrayList;
      for (int i = 0; iter.hasNext(); i++) {
        entry = iter.next();
        categoryNames[i] = entry.getKey();
        artInfoArrayList = entry.getValue();
        articlesByCategory.add(i, artInfoArrayList);
      }
      
      Set<Map.Entry<Date, ArrayList<ArticleInfo>>> allDateEntries = articlesByDateMap.entrySet();  
      Iterator<Map.Entry<Date, ArrayList<ArticleInfo>>> dateIter = allDateEntries.iterator();
      Map.Entry<Date, ArrayList<ArticleInfo>> dateEntry;
      articleDates = new ArrayList<ArrayList<ArrayList<Date>>>(2);
      articlesByDate = new ArrayList<ArrayList<ArrayList<ArrayList<ArticleInfo>>>>(2);

      int j = -1;
      int currentMonth = -1;
      int currentYear = -1;
      int k = -1;
      Calendar oneDate;
      for (int i = 0; dateIter.hasNext(); i++) {
        dateEntry = dateIter.next();
        oneDate = Calendar.getInstance();
        oneDate.setTime(dateEntry.getKey());
        if (currentYear != oneDate.get(Calendar.YEAR)) {
          articleDates.add(++k, new ArrayList<ArrayList<Date>>(12));
          articlesByDate.add(k, new ArrayList<ArrayList<ArrayList<ArticleInfo>>>(12));
          currentYear = oneDate.get(Calendar.YEAR);
          j = -1;
        }
        
        if (currentMonth != oneDate.get(Calendar.MONTH)) {
          //flaw here is if you have two consecutive entries with the same month but different year
          articleDates.get(k).add(++j,new ArrayList<Date>());
          articlesByDate.get(k).add(j,new ArrayList<ArrayList<ArticleInfo>>());
          currentMonth = oneDate.get(Calendar.MONTH);
        }
        articleDates.get(k).get(j).add(dateEntry.getKey());
        articlesByDate.get(k).get(j).add(dateEntry.getValue());
      }
    } else {
      articleDates = new ArrayList<ArrayList<ArrayList<Date>>>(0);
      articlesByDate = new ArrayList<ArrayList<ArrayList<ArrayList<ArticleInfo>>>>(0);
      categoryNames = new String[0];
      articlesByCategory = new ArrayList<ArrayList<ArticleInfo>>(0);
    }
    Object[] cacheObjects = new Object[4];
    cacheObjects[DATES_INDEX] = articleDates;
    cacheObjects[DATES_ARTICLES_INDEX] = articlesByDate;
    cacheObjects[CAT_NAME_INDEX] = categoryNames;
    cacheObjects[CAT_ARTICLES_INDEX] = articlesByCategory;
    return cacheObjects;
  }


  /**
   * @return Returns the articleDates.
   */
  public ArrayList<ArrayList<ArrayList<Date>>> getArticleDates() {
    populateArticlesAndCategories();
    return (ArrayList<ArrayList<ArrayList<Date>>>)allBrowseObjects[DATES_INDEX];
  }


  /**
   * @return Returns the articlesByCategory.
   */
  public ArrayList<ArrayList<ArticleInfo>> getArticlesByCategory() {
    populateArticlesAndCategories();      
    return (ArrayList<ArrayList<ArticleInfo>>) allBrowseObjects[CAT_ARTICLES_INDEX];
  }


  /**
   * @return Returns the articlesByDate.
   */
  public ArrayList<ArrayList<ArrayList<ArrayList<ArticleInfo>>>> getArticlesByDate() {
    populateArticlesAndCategories();      
    return (ArrayList<ArrayList<ArrayList<ArrayList<ArticleInfo>>>>) allBrowseObjects[DATES_ARTICLES_INDEX];
  }


  /**
   * @return Returns the categoryNames.
   */
  public String[] getCategoryNames() {
    populateArticlesAndCategories();      
    return (String[]) allBrowseObjects[CAT_NAME_INDEX];
  }


  /**
   * @param articleCacheAdministrator The articleCacheAdministrator to set.
   */
  public void setArticleCacheAdministrator(GeneralCacheAdministrator articleCacheAdministrator) {
    this.articleCacheAdministrator = articleCacheAdministrator;
  }


  /**
   * @param articleWebService The articleWebService to set.
   */
  public void setArticleWebService(ArticleWebService articleWebService) {
    this.articleWebService = articleWebService;
  }

}
