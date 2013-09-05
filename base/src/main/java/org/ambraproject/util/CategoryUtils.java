/*
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.util;

import org.ambraproject.views.CategoryView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Utilities for working with Maps
 */
public class CategoryUtils {
  private static final Logger log = LoggerFactory.getLogger(CategoryUtils.class);

  private CategoryUtils() {}

  /**
   * For the top elements: return keys and the count of children
   *
   * @param categoryView
   *
   * @return a map of keys and the count of children
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Integer> keyCounts(CategoryView categoryView) {
    Map<String, Integer> results = new HashMap<String, Integer>();

    for(String key : categoryView.getChildren().keySet()) {
      //The size call below as this is a ConcurrentSkipListMap, might be expensive
      results.put(key, categoryView.getChild(key).getChildren().size());
    }

    return results;
  }

  /**
   * For the top elements: return keys and the immediate children
   *
   * @param categoryView
   *
   * @return a map of keys and the immediate children
   */
  @SuppressWarnings("unchecked")
  public static Map<String, SortedSet<String>> getShortTree(CategoryView categoryView) {
    //Use sorted map
    Map<String, SortedSet<String>> results = new ConcurrentSkipListMap<String, SortedSet<String>>();

    for(String key : categoryView.getChildren().keySet()) {
      ConcurrentSkipListSet sortedSet = new ConcurrentSkipListSet();
      sortedSet.addAll(categoryView.getChild(key).getChildren().keySet());
      results.put(key, sortedSet);
    }

    return results;
  }

  /**
   * Returns information about the number of articles that fall into particular
   * categories.
   *
   * @param categoryView the root category
   * @return map of category to article count.  Included keys will be the root category
   *     and all of its first-level children.
   */
  public static Map<String, Long> getCounts(CategoryView categoryView) {
    Map<String, Long> results = new HashMap<String, Long>();
    results.put(categoryView.getName(), categoryView.getCount());
    for (CategoryView child : categoryView.getChildren().values()) {
      results.put(child.getName(), child.getCount());
    }
    return results;
  }

  /**
   * Return a map that contains entries that match the filters given
   *
   * Any match in a tree will result in that branch being included
   *
   * @param categoryView the categoryView to filter
   * @param filters the filters to apply
   *
   * @return a new map
   */
  @SuppressWarnings("unchecked")
  public static CategoryView filterMap(CategoryView categoryView, String[] filters) {
    CategoryView finalRes = new CategoryView(categoryView.getName());

    for(String key : categoryView.getChildren().keySet()) {
      CategoryView res = filterMap(categoryView.getChild(key), filters);

      if(!res.getChildren().isEmpty()) {
        finalRes.addChild(res);
      }

      for(String filter : filters) {
        if(key.toLowerCase().contains(filter.toLowerCase())) {
          finalRes.addChild(filterMap(categoryView.getChild(key), filters));
        }
      }
    }

    return finalRes;
  }

  /**
   * For the passed in category, find the matching CategoryView.  This will come in handy when looking for
   * getting the correctly formatted case corrected category name
   *
   * @param categoryView the categoryView to search from
   * @param category the string of the category to search for
   *
   * @return The first matching category view
   *
   * @throws org.ambraproject.ApplicationException
   */
  public static CategoryView findCategory(CategoryView categoryView, String category) {
    if(categoryView.getName().toLowerCase().equals(category.toLowerCase())) {
      return categoryView;
    }

    for(String key : categoryView.getChildren().keySet()) {
      CategoryView res = findCategory(categoryView.getChild(key), category);

      if(res != null) {
        return res;
      }
    }

    return null;
  }

  /**
   * Given a list of "/" delimited strings build a structured map
   *
   * @param categories list of Pairs wrapping the category name and article count
   *
   * @return a new treeMap
   */
  public static CategoryView createMapFromStringList(List<Pair<String, Long>> categories) {
    CategoryView root = new CategoryView("ROOT", 0);

    // Since there can be multiple paths to the same child, we don't want to create the same
    // child more than once.  Hence the need for this map.
    Map<String, CategoryView> createdCategories = new HashMap<String, CategoryView>();
    for (Pair<String, Long> category : categories) {
      if(category.getFirst().charAt(0) == '/') {
        //Ignore first "/"
        root = recurseValues(root, category.getFirst().substring(1).split("\\/"), 0, category.getSecond(),
            createdCategories);
      } else {
        root = recurseValues(root, category.getFirst().split("\\/"), 0, category.getSecond(), createdCategories);
      }
    }

    incrementAncestorCounts(root);
    return root;
  }

  private static CategoryView recurseValues(CategoryView root, String categories[], int index,
      long lastChildCount, Map<String, CategoryView> created) {
    CategoryView child = root.getChildren().get(categories[index]);

    if (child == null) {
      long count = index == categories.length - 1 ? lastChildCount : 0;
      child = created.get(categories[index]);
      if (child == null) {
        child = new CategoryView(categories[index], count);
        created.put(categories[index], child);
      }
      root.addChild(child);
    }

    if ((index + 1) < categories.length) { // path end
      recurseValues(child, categories, index + 1, lastChildCount, created);
    }

    return root;
  }

  /**
   * Updates the article counts for a category tree/DAG.  Before this method is called,
   * counts for each category will be as they are returned from solr: *only* articles
   * that are tagged with that exact category will be included.  But in the UI, we want
   * the article count for a category to also include the counts of all subcategories,
   * recursively.  That's what this method does.
   *
   * @param root the root of the category hierarchy we are interested in
   */
  private static void incrementAncestorCounts(CategoryView root) {
    Map<CategoryView, Set<CategoryView>> nodesToDescendants = new HashMap<CategoryView, Set<CategoryView>>();
    calculateDescendants(root, nodesToDescendants);
    sumCountsRecursively(root, nodesToDescendants);
  }

  /**
   * Populates a map from each node in the category hierarchy to the set of all descendants
   * of that node.  In other words, conceptually, we decorate each node with the set of all
   * of its descendants.
   *
   * @param root root of the category hierarchy
   * @param nodesToDescendants map that will be populated
   * @return set of descendants calculated for the current root
   */
  private static Set<CategoryView> calculateDescendants(CategoryView root,
      Map<CategoryView, Set<CategoryView>> nodesToDescendants) {
    Set<CategoryView> descendants = nodesToDescendants.get(root);
    if (descendants == null) {
      descendants = new HashSet<CategoryView>();
    }
    for (CategoryView child : root.getChildren().values()) {
      if (child.getChildren().isEmpty()) {
        nodesToDescendants.put(child, Collections.EMPTY_SET);
      } else {
        descendants.addAll(calculateDescendants(child, nodesToDescendants));
      }
      descendants.add(child);
    }
    nodesToDescendants.put(root, descendants);
    return descendants;
  }

  /**
   * Increments the article count for each node in a category hierarchy, based on the
   * set of descendants in nodesToDescendants.
   *
   * @param root node we are currently processing
   * @param nodesToDescendants map, populated by calculateDescendants, that maps a node
   *     to all of its descendants
   */
  private static void sumCountsRecursively(CategoryView root,
      Map<CategoryView, Set<CategoryView>> nodesToDescendants) {

    // Since there can be multiple paths to a child, we run the risk of processing it
    // multiple times unless we remove it from nodesToDescendants.
    Set<CategoryView> descendants = nodesToDescendants.remove(root);
    if (descendants != null) {
      for (CategoryView descendant : descendants) {
        root.incrementCount(descendant.getCount());
      }
    }
    for (CategoryView child : root.getChildren().values()) {
      sumCountsRecursively(child, nodesToDescendants);
    }
  }
}
