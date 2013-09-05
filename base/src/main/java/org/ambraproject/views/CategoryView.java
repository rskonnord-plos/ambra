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

package org.ambraproject.views;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * CategoryView to hold category structure and information
 */
public class CategoryView {
  private Map<String, CategoryView> parents;
  private Map<String, CategoryView> children;
  private final String name;
  private long count;

  public CategoryView(String name) {
    this.name = name;
    this.count = -1;

    parents = new ConcurrentSkipListMap<String, CategoryView>();
    children = new ConcurrentSkipListMap<String, CategoryView>();
  }

  public CategoryView(String name, long count) {
    this(name);
    this.count = count;
  }

  public void incrementCount(long increment) {
    count += increment;
  }

  public Map<String, CategoryView> getParents() {
    return parents;
  }

  public Map<String, CategoryView> getChildren() {
    return children;
  }

  public String getName() {
    return name;
  }

  /**
   * @return the number of articles that fall within this category, or -1 if that info
   *     is not available
   */
  public long getCount() {
    return count;
  }

  @Override
  public String toString() {
    return name;
  }

  public CategoryView getChild(String key) {
    return children.get(key);
  }

  public CategoryView getParent(String key) {
    return parents.get(key);
  }

  public void addParent(CategoryView categoryView) {
    parents.put(categoryView.getName(), categoryView);
    categoryView.children.put(this.name, this);
  }

  public void addChild(CategoryView categoryView) {
    children.put(categoryView.getName(), categoryView);
    categoryView.parents.put(this.name, this);
  }

  @Override
  public boolean equals(Object that) {
    if (that == null || !(that instanceof CategoryView)) {
      return false;
    }
    return name.equals(((CategoryView) (that)).name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
