package org.plos.model.article;

import java.util.Comparator;

/**
 * Comparator class used to sort ArticleInfo objects based on article publication date. 
 * The more recent article will appear first. Where the two publication dates are the 
 * same, the article id is used to preserve a consistent ordering. 
 * 
 * Note that an article publication date is ingested from the original article XML and is
 * based upon year / month / day only. Although getDate() returns a java.util.Date object, 
 * (which stores dates to the nearest millisecond) these should be equal for articles with 
 * the same publication date. 
 *  
 * @author Alex Worden
 *
 */
public class ArticleInfoMostRecentDateComparator implements Comparator<ArticleInfo>{

  public int compare(ArticleInfo o1, ArticleInfo o2) {
    if (o1.getDate().after(o2.getDate())) {
      return -1;
    }
    if (o1.getDate().before(o2.getDate())) {
      return 1;
    }
    
    return o1.getId().compareTo(o2.getId());
  }
}
