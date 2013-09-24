/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.service.taxonomy;

import org.ambraproject.ApplicationException;
import org.ambraproject.service.cache.Cache;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.service.search.SearchService;
import org.ambraproject.util.CategoryUtils;
import org.ambraproject.views.CategoryView;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateCallback;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * {@inheritDoc}
 */
public class TaxonomyServiceImpl extends HibernateServiceImpl implements TaxonomyService   {

  private static final int CACHE_TTL = 3600 * 24;  // one day

  private SearchService searchService;
  private Cache cache;

  /**
   * {@inheritDoc}
   */
  public void flagTaxonomyTerm(final long articleID, final long categoryID, final String authID) {
    //The style of query used is significantly different pending the authID is null or not

    //I don't use a hibernate model here to save on precious CPU.

    if(authID != null && authID.length() > 0) {
      //This query will update on a duplicate
      hibernateTemplate.execute(new HibernateCallback() {
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
          session.createSQLQuery(
            "insert into articleCategoryFlagged(articleID, categoryID, userProfileID, created, lastModified) select " +
              ":articleID, :categoryID, up.userProfileID, :created, :lastModified " +
              "from userProfile up where up.authId = :authID on duplicate key update lastModified = :lastModified")
            .setString("authID", authID)
            .setLong("articleID", articleID)
            .setLong("categoryID", categoryID)
            .setCalendar("created", Calendar.getInstance())
            .setCalendar("lastModified", Calendar.getInstance())
          .executeUpdate();

          return null;
        }
      });
    } else {
      //Insert userProfileID as a null value
      hibernateTemplate.execute(new HibernateCallback() {
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
          session.createSQLQuery(
            "insert into articleCategoryFlagged(articleID, categoryID, userProfileID, created, lastModified) values(" +
              ":articleID, :categoryID, null, :created, :lastModified)")
            .setLong("articleID", articleID)
            .setLong("categoryID", categoryID)
            .setCalendar("created", Calendar.getInstance())
            .setCalendar("lastModified", Calendar.getInstance())
            .executeUpdate();

          return null;
        }
      });
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deflagTaxonomyTerm(final long articleID, final long categoryID, final String authID) {
    //The style of query used is significantly different pending the authID is null or not
    //I don't use a hibernate model here to save on precious CPU.
    if(authID != null && authID.length() > 0) {
      hibernateTemplate.execute(new HibernateCallback() {
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
          session.createSQLQuery(
            "delete acf.* from articleCategoryFlagged acf " +
              "join userProfile up on up.userProfileID = acf.userProfileID " +
              "where acf.articleID = :articleID and acf.categoryID = :categoryID and " +
              "up.authId = :authID")
            .setString("authID", authID)
            .setLong("articleID", articleID)
            .setLong("categoryID", categoryID)
            .executeUpdate();

          return null;
        }
      });
    } else {
      hibernateTemplate.execute(new HibernateCallback() {
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
          //Remove one record from the database at random
          session.createSQLQuery(
            "delete from articleCategoryFlagged where articleID = :articleID and categoryID = :categoryID " +
              "and userProfileID is null limit 1")
            .setLong("articleID", articleID)
            .setLong("categoryID", categoryID)
            .executeUpdate();

          return null;
        }
      });
    }
  }

  /**
   * {@inheritDoc}
   */
  public SortedMap<String, List<String>> parseTopAndSecondLevelCategories(final String currentJournal)
    throws ApplicationException {
    if (cache == null) {
      return parseTopAndSecondLevelCategoriesWithoutCache(currentJournal);
    } else {
      String key = ("topAndSecondLevelCategoriesCacheKey" + currentJournal).intern();

      return cache.get(key, CACHE_TTL,
          new Cache.SynchronizedLookup<SortedMap<String, List<String>>, ApplicationException>(key) {
            @Override
            public SortedMap<String, List<String>> lookup() throws ApplicationException {
              return parseTopAndSecondLevelCategoriesWithoutCache(currentJournal);
            }
          });
    }
  }

  private SortedMap<String, List<String>> parseTopAndSecondLevelCategoriesWithoutCache(String currentJournal)
    throws ApplicationException {
    List<String> fullCategoryPaths = searchService.getAllSubjects(currentJournal);

    // Since there are lots of duplicates, we start by adding the second-level
    // categories to a Set instead of a List.
    Map<String, Set<String >> map = new HashMap<String, Set<String>>();
    for (String category : fullCategoryPaths) {

      // If the category doesn't start with a slash, it's one of the old-style
      // categories where we didn't store the full path.  Ignore these.
      if (category.charAt(0) == '/') {
        String[] fields = category.split("\\/");
        if (fields.length >= 3) {
          Set<String> subCats = map.get(fields[1]);
          if (subCats == null) {
            subCats = new HashSet<String>();
          }
          subCats.add(fields[2]);
          map.put(fields[1], subCats);
        }
      }
    }

    // Now sort all the subcategory lists, and add them to the result.
    SortedMap<String, List<String>> results = new TreeMap<String, List<String>>();
    for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
      List<String> subCatList = new ArrayList<String>(entry.getValue());
      Collections.sort(subCatList);
      results.put(entry.getKey(), subCatList);
    }
    return results;
  }

  /**
   * {@inheritDoc}
   */
  public CategoryView parseCategories(final String currentJournal)
    throws ApplicationException {
    if (cache == null) {
      return parseCategoriesWithoutCache(currentJournal);
    } else {
      String key = ("categoriesCacheKey" + ((currentJournal==null)?"":currentJournal)).intern();

      return cache.get(key, CACHE_TTL,
        new Cache.SynchronizedLookup<CategoryView, ApplicationException>(key) {
          @Override
          public CategoryView lookup() throws ApplicationException {
            return parseCategoriesWithoutCache(currentJournal);
          }
        });
    }
  }

  @SuppressWarnings("unchecked")
  private CategoryView parseCategoriesWithoutCache(String currentJournal)
    throws ApplicationException {

    List<String> subjects = searchService.getAllSubjects(currentJournal);

    return CategoryUtils.createMapFromStringList(subjects);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Long> getCounts(CategoryView taxonomy, String currentJournal) throws ApplicationException {
    Map<String, Long> counts = getAllCounts(currentJournal);
    Map<String, Long> results = new HashMap<String, Long>();
    for (CategoryView child : taxonomy.getChildren().values()) {
      results.put(child.getName(), counts.get(child.getName()));
    }
    results.put(taxonomy.getName(), counts.get(taxonomy.getName()));
    return results;
  }

  /**
   * Returns article counts for a given journal for all subject terms in the taxonomy.
   * The results will be cached for CACHE_TTL.
   *
   * @param currentJournal specifies the current journal
   * @return map from subject term to article count
   * @throws ApplicationException
   */
  private Map<String, Long> getAllCounts(final String currentJournal) throws ApplicationException {
    if (cache == null) {
      return getAllCountsWithoutCache(currentJournal);
    } else {
      String key = ("categoryCountCacheKey" + ((currentJournal == null) ? "" : currentJournal)).intern();
      return cache.get(key, CACHE_TTL,
          new Cache.SynchronizedLookup<Map<String, Long>, ApplicationException>(key) {
            @Override
            public Map<String, Long> lookup() throws ApplicationException {
              return getAllCountsWithoutCache(currentJournal);
            }
          });
    }
  }

  private Map<String, Long> getAllCountsWithoutCache(String currentJournal) throws ApplicationException {
    SearchService.SubjectCounts subjectCounts = searchService.getAllSubjectCounts(currentJournal);
    Map<String, Long> counts = subjectCounts.subjectCounts;
    counts.put(CategoryView.ROOT_NODE_NAME, subjectCounts.totalArticles);
    return counts;
  }

  public void setCache(Cache cache) {
    this.cache = cache;
  }

  @Required
  public void setSearchService(final SearchService searchService) {
    this.searchService = searchService;
  }
}
