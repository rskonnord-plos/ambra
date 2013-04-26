package org.ambraproject.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utilities for working with Maps
 */
public class MapUtils {
  private static final Logger log = LoggerFactory.getLogger(MapUtils.class);
  /**
   * For the top elements: return keys and the count of children
   *
   * @param categories
   *
   * @return a map of keys and the count of children
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Integer> keyCounts(Map<String, Object> categories) {
    Map<String, Integer> results = new HashMap<String, Integer>();

    for(String key : categories.keySet()) {
      results.put(key, ((Map<String, Object>)categories.get(key)).size());
    }

    return results;
  }

  /**
   * Return a map that contains entries that match the filters given
   *
   * Any match in a tree will result in that branch being included
   *
   * @param categories the map to filter
   * @param filters the filters to apply
   *
   * @return a new map
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> filterMap(Map<String, Object>categories, String[] filters) {
    Map<String, Object> finalRes = new TreeMap<String, Object>();

    for(String key : categories.keySet()) {
      Map<String, Object> res = filterMap((Map<String, Object>)categories.get(key), filters);

      if(res.size() > 0) {
        finalRes.put(key, res);
      }

      for(String filter : filters) {
        if(key.toLowerCase().contains(filter.toLowerCase())) {
          finalRes.put(key, new TreeMap<String, Object>());
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
  public static Map createMapFromStringList(List<String> strings) {
    Map structure = new TreeMap();

    for (String string : strings) {
      if(string.charAt(0) == '/') {
        //Ignore first "/"
        structure = recurseValues(structure, string.substring(1).split("\\/"), 0);
      } else {
        structure = recurseValues(structure, string.split("\\/"), 0);
      }
    }

    return structure;
  }

  private static Map recurseValues(Map structure, String category[], int index) {
    TreeMap rootDir = (TreeMap) structure.get(category[index]);

    if (rootDir == null) {
      rootDir = new TreeMap();
      structure.put(category[index], rootDir);
    }

    if ((index + 1) < category.length) { // path end
      recurseValues(rootDir, category, index + 1);
    }

    return structure;
  }
}
