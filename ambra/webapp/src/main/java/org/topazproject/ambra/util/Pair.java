package org.topazproject.ambra.util;

/**
 * Utility class for holding a typed pair of Objects
 *
 *
 * @author Alex Kudlick Date: Feb 17, 2011
 * <p/>
 * org.topazproject.ambra.util
 */
public class Pair<K,V> {
  private K first;
  private V second;

  public Pair(K first, V second) {
    this.first = first;
    this.second = second;
  }

  public K getFirst() {
    return first;
  }

  public V getSecond() {
    return second;
  }
}
