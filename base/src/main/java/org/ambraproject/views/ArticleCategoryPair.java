package org.ambraproject.views;

/**
 * View object for a article / category pair
 */
public class ArticleCategoryPair {
  private final long articleID;
  private final long categoryID;

  public ArticleCategoryPair(long articleID, long categoryID) {
    this.articleID = articleID;
    this.categoryID = categoryID;
  }

  public long getArticleID() {
    return articleID;
  }

  public long getCategoryID() {
    return categoryID;
  }
}
