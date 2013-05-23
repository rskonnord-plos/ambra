package org.ambraproject.util;

import org.ambraproject.views.CategoryView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
  public static Map<String, Set<String>> getShortTree(CategoryView categoryView) {
    Map<String, Set<String>> results = new HashMap<String, Set<String>>();

    for(String key : categoryView.getChildren().keySet()) {
      results.put(key, categoryView.getChild(key).getChildren().keySet());
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

      if(res.getChildren().size() > 0) {
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
   * Given a list of "/" delimited strings build a structured map
   *
   * @param strings
   *
   * @return a new treeMap
   */
  public static CategoryView createMapFromStringList(List<String> strings) {
    CategoryView root = new CategoryView("ROOT");

    for (String string : strings) {
      if(string.charAt(0) == '/') {
        //Ignore first "/"
        root = recurseValues(root, string.substring(1).split("\\/"), 0);
      } else {
        root = recurseValues(root, string.split("\\/"), 0);
      }
    }

    return root;
  }

  private static CategoryView recurseValues(CategoryView category, String categories[], int index) {
    CategoryView rootCategory = category.getChildren().get(categories[index]);

    if (rootCategory == null) {
      rootCategory = new CategoryView(categories[index]);
      category.addChild(rootCategory);
    }

    if ((index + 1) < categories.length) { // path end
      recurseValues(rootCategory, categories, index + 1);
    }

    return category;
  }
}
