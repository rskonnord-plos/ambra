/*
 * $HeadURL$
 * $Id$
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

/**
 * Simple wrapper around subject facet counts (article category counts).  In C++,
 * this would be a Pair<string, int>.  In dynamically-typed languages, it would be
 * a tuple.
 */
public class CategoryCount implements Comparable<CategoryCount> {

  private String category;

  private long count = -1;

  public CategoryCount(String category, long count) {
    this.category = category;
    this.count = count;
  }

  @Override
  public int compareTo(CategoryCount other) {
    return category.compareTo(other.category);
  }

  /**
   * @return the full, path-delimited category and any subcategories.
   */
  public String getCategory() {
    return category;
  }

  /**
   * @return the number of articles that fall within this category
   */
  public long getCount() {
    return count;
  }

  @Override
  public String toString() {
    return category;
  }
}
