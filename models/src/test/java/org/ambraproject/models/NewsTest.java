package org.ambraproject.models;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class NewsTest extends BaseHibernateTest {

  @Test
  public void testSaveNews() {
    Article article = new Article();
    article.setDoi("doi");

    Article article1 = new Article();
    article.setDoi("doi1");

    Article article2 = new Article();
    article.setDoi("doi2");

    News news = new News();
    news.setArticleID(article.getID());
    news.setSortOrder(Integer.valueOf(1));

    Integer sortOrder = (Integer) hibernateTemplate.save(news);

    news = (News) hibernateTemplate.get(News.class, sortOrder);

    assertEquals(news.getArticleID(), article.getID(), "incorrect id");

  }

}
