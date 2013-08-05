package org.ambraproject.models;

public class News {

  private Long articleID;
  private Integer sortOrder;

  public Long getArticleID() {
    return articleID;
  }

  public void setArticleID(Long articleID) {
    this.articleID = articleID;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof News)) return false;

    News news = (News) o;

    if (articleID != null ? !articleID.equals(news.articleID) : news.articleID != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return articleID != null ? articleID.hashCode() : 0;
  }

}
