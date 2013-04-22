package org.ambraproject.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Utilities for working with Maps
 */
public class MapUtils {
  /**
   * For the top elements: return keys and the count of children
   *
   * @param categories
   *
   * @return a map of keys and the count of children
   */
  public static Map<String, Integer> treeMapFacet(SortedMap<String, List<Object>> categories) {
    Map<String, Integer> results = new HashMap<String, Integer>();

    for(String key : categories.keySet()) {
      results.put(key, categories.get(key).size());
    }

    return results;
  }
  /**
   * Given a list of "/" delimited strings build a structured map
   *
   * @param strings
   *
   * @return a new treeMap
   */
  public static TreeMap createMapFromStringList(List<String> strings) {
    TreeMap structure = new TreeMap();

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

  private static TreeMap recurseValues(TreeMap structure, String category[], int index) {
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
