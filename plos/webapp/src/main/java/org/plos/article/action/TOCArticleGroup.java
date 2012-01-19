package org.plos.article.action;

import java.util.ArrayList;

import org.plos.model.article.ArticleInfo;
import org.plos.model.article.ArticleType;
/**
 * Represents a group of articles for display in the presentation layer. 
 *  
 * @author Alex
 */
public class TOCArticleGroup {
  ArticleType type;
  ArrayList<ArticleInfo> articles = new ArrayList<ArticleInfo>();
  private String id = null;
  private String heading = null;
  
  public TOCArticleGroup(ArticleType type) {
    this.type = type;
  }
  
  /**
   * The heading displayed for this article group. 
   * @return
   */
  public String getHeading() {
    if (heading != null) {
      return heading;
    } else if (type != null) {
      return type.getHeading();
    } else {
      return "Undefined";
    }
  }
  
  public void setHeading(String h) {
    heading = h;
  }
  /**
   * An id for the group to be used in html. May be set by the action for each ArticleGroup
   * and should be unique within that group. 
   * 
   * @param i
   */
  public void setId(String i) {
    this.id = i;
  }
  
  public String getId() {
    if (id != null) {
      return id;
    } else {
      return "id_"+getHeading();
    }
  }
  
  public void addArticle(ArticleInfo article) {
    articles.add(article);
  }

  public ArticleType getArticleType() {
    return type;
  }
  
  public ArrayList<ArticleInfo> getArticles() {
    return articles;
  }
}
