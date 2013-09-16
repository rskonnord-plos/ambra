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
package org.ambraproject.views;

/**
 * View object for article Categories
 *
 * @author Joe Osowski
 */
public class ArticleCategory implements Comparable<ArticleCategory> {
  private final long categoryID;
  private final String mainCategory;
  private final String subCategory;
  private final String path;
  private final boolean flagged;

  private ArticleCategory(long categoryID, String mainCategory, String subCategory, String path, boolean flagged) {
    this.categoryID = categoryID;
    this.mainCategory = mainCategory;
    this.subCategory = subCategory;
    this.path = path;
    this.flagged = flagged;
  }

  public long getCategoryID() {
    return categoryID;
  }

  public String getMainCategory() {
    return mainCategory;
  }

  public String getSubCategory() {
    return subCategory;
  }

  public String getPath() {
    return path;
  }

  public boolean getFlagged() {
    return flagged;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(ArticleCategory articleCategory) {
    return new Builder(articleCategory);
  }

  /**
   * We want to sort on the last note in the category path.  This will be the sub category most of the time.
   * But sometimes main, if it is only one level deep.
   *
   * We do this as on the user interface, we usually just display the last term
   *
   * @param o
   *
   * @return
   */
  @Override
  public int compareTo(ArticleCategory o) {
    String term = o.getSubCategory();
    String term1 = this.getSubCategory();

    if(term == null || term.length() == 0) {
      term = o.getMainCategory();
    }

    if(term1 == null || term1.length() == 0) {
      term1 = this.getMainCategory();
    }

    return term1.compareTo(term);
  }

  public static class Builder {
    private Builder() {
      super();
    }

    private Builder(ArticleCategory articleCategory) {
      super();

      categoryID = articleCategory.getCategoryID();
      mainCategory = articleCategory.getMainCategory();
      subCategory = articleCategory.getSubCategory();
      path = articleCategory.getPath();
      flagged = articleCategory.getFlagged();
    }

    private long categoryID;
    private String mainCategory;
    private String subCategory;
    private String path;
    private boolean flagged;

    public Builder setCategoryID(long categoryID) {
      this.categoryID = categoryID;
      return this;
    }

    public Builder setMainCategory(String mainCategory) {
      this.mainCategory = mainCategory;
      return this;
    }

    public Builder setSubCategory(String subCategory) {
      this.subCategory = subCategory;
      return this;
    }

    public Builder setPath(String path) {
      this.path = path;
      return this;
    }

    public Builder setFlagged(boolean flagged) {
      this.flagged = flagged;
      return this;
    }

    public ArticleCategory build() {
      return new ArticleCategory(categoryID, mainCategory, subCategory, path, flagged);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ArticleCategory that = (ArticleCategory) o;

    if (categoryID != that.categoryID) return false;
    if (flagged != that.flagged) return false;
    if (!mainCategory.equals(that.mainCategory)) return false;
    if (!path.equals(that.path)) return false;
    if (!subCategory.equals(that.subCategory)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = (int) (categoryID ^ (categoryID >>> 32));
    result = 31 * result + mainCategory.hashCode();
    result = 31 * result + subCategory.hashCode();
    result = 31 * result + path.hashCode();
    result = 31 * result + (flagged ? 1 : 0);
    return result;
  }
}
