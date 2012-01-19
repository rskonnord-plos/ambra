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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts2.ServletActionContext;

import org.plos.models.Article;
import org.plos.models.Category;
import org.plos.util.CacheAdminHelper;
import org.plos.web.VirtualJournalContext;

import com.opensymphony.oscache.general.GeneralCacheAdministrator;

/**
 * Class to get all Articles in system and organize them by date and by category
 *
 * @author stevec
 */
public class BrowseService {
  private static final Log log = LogFactory.getLog(BrowseService.class);

  private static final String ALL_ARTICLE_CACHE_KEY = "ALL_ARTICLE_LIST-";
  private static final String ALL_BROWSE_OBJECTS    = "ALL_BROWSE_OBJECTS-";

  public static final String ALL_ARTICLE_CACHE_GROUP_KEY = "ALL_ARTICLE_LIST_GROUP";

  private BrowseObjects             allBrowseObjects;
  private GeneralCacheAdministrator articleCacheAdministrator;
  private ArticleOtmService         articleOtmService;


  /**
   * retrieve a listing of all articles in Topaz
   *
   * @param jnlName the name of the journal for which to get the articles (must be the "current"
   *                journal, i.e.  what the otm filters are set to)
   * @return all articles
   */
  private Article[] getAllArticles(String jnlName) {
    return CacheAdminHelper.getFromCache(articleCacheAdministrator, ALL_ARTICLE_CACHE_KEY + jnlName,
                                         -1, new String[] { ALL_ARTICLE_CACHE_GROUP_KEY },
                                         "all articles for " + jnlName,
                                         new CacheAdminHelper.CacheUpdater<Article[]>() {
        public Article[] lookup(boolean[] updated) {
          try {
            Article[] res = articleOtmService.getArticles(null, null, null, null,
                                                          new int[] { Article.STATE_ACTIVE }, true);
            updated[0] = true;
            return res;
          } catch (ParseException pe) {
            log.error("Internal error", pe);
            return new Article[0];
          }
        }
      }
    );
  }


  /**
   * Takes the articles and sets the categoryNames and articlesByCategory as well as the
   * articleDates and articlesByDate values.
   */
  private void populateArticlesAndCategories() {
    final String jnlName =
        ((VirtualJournalContext)
          ServletActionContext.getRequest().
            getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT)).getJournal();

    allBrowseObjects =
        CacheAdminHelper.getFromCache(articleCacheAdministrator, ALL_BROWSE_OBJECTS + jnlName, -1,
                                      new String[] { ALL_ARTICLE_CACHE_GROUP_KEY },
                                      "category and date browse objects for " + jnlName,
                                      new CacheAdminHelper.CacheUpdater<BrowseObjects>() {
        public BrowseObjects lookup(boolean[] updated) {
          BrowseObjects res = createBrowseObjects(jnlName);
          updated[0] = true;
          return res;
        }
      }
    );
  }

  private BrowseObjects createBrowseObjects(String jnlName) {
    ArrayList<ArrayList<Article>> articlesByCategory;
    TreeMap<String, ArrayList<Article>> articlesByCategoryMap;
    TreeMap<Date, ArrayList<Article>> articlesByDateMap;
    ArrayList<ArrayList<ArrayList<ArrayList<Article>>>> articlesByDate;
    ArrayList<ArrayList<ArrayList<Date>>> articleDates;
    String[] categoryNames;

    Article[] allArticleList = getAllArticles(jnlName);

    if (allArticleList.length > 0){
      articlesByCategoryMap = new TreeMap<String, ArrayList<Article>>();
      articlesByDateMap = new TreeMap<Date, ArrayList<Article>>();
      ArrayList<Article> theList;
      Date theDate;
      for (Article art : allArticleList) {
        Set<String> categories = getMainCategories(art);
        theDate = art.getDublinCore().getDate();
        theList = articlesByDateMap.get(theDate);
        if (theList == null) {
          theList = new ArrayList<Article>();
          articlesByDateMap.put(theDate, theList);
        }
        theList.add(0, art);
        for (String cat : categories) {
          theList = articlesByCategoryMap.get(cat);
          if (theList == null) {
            theList = new ArrayList<Article>();
            articlesByCategoryMap.put(cat, theList);
          }
          theList.add(art);
        }
      }
      Set<Map.Entry<String, ArrayList<Article>>> allEntries = articlesByCategoryMap.entrySet();
      Iterator<Map.Entry<String, ArrayList<Article>>> iter = allEntries.iterator();
      categoryNames = new String[allEntries.size()];
      articlesByCategory = new ArrayList<ArrayList<Article>>(allEntries.size());
      for (int i = 0; iter.hasNext(); i++) {
        Map.Entry<String, ArrayList<Article>> entry = iter.next();
        categoryNames[i] = entry.getKey();
        articlesByCategory.add(i, entry.getValue());
      }

      Set<Map.Entry<Date, ArrayList<Article>>> allDateEntries = articlesByDateMap.entrySet();
      Iterator<Map.Entry<Date, ArrayList<Article>>> dateIter = allDateEntries.iterator();
      Map.Entry<Date, ArrayList<Article>> dateEntry;
      articleDates = new ArrayList<ArrayList<ArrayList<Date>>>(2);
      articlesByDate = new ArrayList<ArrayList<ArrayList<ArrayList<Article>>>>(2);

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
          articlesByDate.add(k, new ArrayList<ArrayList<ArrayList<Article>>>(12));
          currentYear = oneDate.get(Calendar.YEAR);
          j = -1;
        }

        if (currentMonth != oneDate.get(Calendar.MONTH)) {
          //flaw here is if you have two consecutive entries with the same month but different year
          articleDates.get(k).add(++j,new ArrayList<Date>());
          articlesByDate.get(k).add(j,new ArrayList<ArrayList<Article>>());
          currentMonth = oneDate.get(Calendar.MONTH);
        }
        articleDates.get(k).get(j).add(dateEntry.getKey());
        articlesByDate.get(k).get(j).add(dateEntry.getValue());
      }
    } else {
      articleDates = new ArrayList<ArrayList<ArrayList<Date>>>(0);
      articlesByDate = new ArrayList<ArrayList<ArrayList<ArrayList<Article>>>>(0);
      categoryNames = new String[0];
      articlesByCategory = new ArrayList<ArrayList<Article>>(0);
    }

    BrowseObjects cacheObjects = new BrowseObjects();
    cacheObjects.articleDates       = articleDates;
    cacheObjects.articlesByDate     = articlesByDate;
    cacheObjects.categoryNames      = categoryNames;
    cacheObjects.articlesByCategory = articlesByCategory;
    return cacheObjects;
  }

  private static Set<String> getMainCategories(Article art) {
    Set<String> mainCats = new HashSet<String>();
    for (Category cat : art.getCategories())
      mainCats.add(cat.getMainCategory());
    return mainCats;
  }

  /**
   * @return Returns the articleDates.
   */
  public ArrayList<ArrayList<ArrayList<Date>>> getArticleDates() {
    populateArticlesAndCategories();
    return allBrowseObjects.articleDates;
  }


  /**
   * @return Returns the articlesByCategory.
   */
  public ArrayList<ArrayList<Article>> getArticlesByCategory() {
    populateArticlesAndCategories();
    return allBrowseObjects.articlesByCategory;
  }


  /**
   * @return Returns the articlesByDate.
   */
  public ArrayList<ArrayList<ArrayList<ArrayList<Article>>>> getArticlesByDate() {
    populateArticlesAndCategories();
    return allBrowseObjects.articlesByDate;
  }


  /**
   * @return Returns the categoryNames.
   */
  public String[] getCategoryNames() {
    populateArticlesAndCategories();
    return allBrowseObjects.categoryNames;
  }

  /**
   * Flush the cache for a Journal.
   *
   * @param journal Journal key to flush
   */
  public void flush(final String journal) {
    articleCacheAdministrator.flushEntry(ALL_BROWSE_OBJECTS + journal);
    articleCacheAdministrator.flushEntry(ALL_ARTICLE_CACHE_KEY + journal);
  }

  /**
   * @param articleCacheAdministrator The articleCacheAdministrator to set.
   */
  public void setArticleCacheAdministrator(GeneralCacheAdministrator articleCacheAdministrator) {
    this.articleCacheAdministrator = articleCacheAdministrator;
  }


  /**
   * @param articleOtmService The articleOtmService to set.
   */
  public void setArticleOtmService(ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  private static class BrowseObjects {
     ArrayList<ArrayList<ArrayList<Date>>>               articleDates;
     ArrayList<ArrayList<ArrayList<ArrayList<Article>>>> articlesByDate;
     String[]                                            categoryNames;
     ArrayList<ArrayList<Article>>                       articlesByCategory;
  }
}
