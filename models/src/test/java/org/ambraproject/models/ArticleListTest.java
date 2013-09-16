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

package org.ambraproject.models;

import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ArticleListTest extends BaseHibernateTest {

  @Test
  public void testSaveBasicCategory() {

    ArticleList articleList1 = new ArticleList("code:testArticleListToSave");
    articleList1.setDisplayName("News");
    articleList1.setArticleDois(Arrays.asList("doi1", "doi2", "doi3"));

    Serializable id1 = hibernateTemplate.save(articleList1);

    ArticleList storedList1 = (ArticleList) hibernateTemplate.get(ArticleList.class, id1);
    assertNotNull(storedList1, "didn't save article list");
    assertEquals(storedList1, articleList1, "didn't store correct article list properties");
    assertNotNull(storedList1.getCreated(), "article list didn't get created date set");

  }

  @Test
  public void testUpdateCategory() {
    long testStart = Calendar.getInstance().getTimeInMillis();
    ArticleList articleList = new ArticleList("listCode:testarticleListToUpdate");
    articleList.setDisplayName("Old News Article");
    List<String> articleDois = new ArrayList<String>(3);
    articleDois.add("old doi 1");
    articleDois.add("old doi 2");
    articleDois.add("old doi 3");

    articleList.setArticleDois(articleDois);


    Serializable id = hibernateTemplate.save(articleList);

    articleList.getArticleDois().remove(1);
    articleList.getArticleDois().add("new doi 4");
    articleList.setDisplayName("New News Articles");

    hibernateTemplate.update(articleList);

    ArticleList storedArticleList = (ArticleList) hibernateTemplate.get(ArticleList.class, id);
    assertEquals(storedArticleList, articleList, "didn't update news properties");
    assertNotNull(storedArticleList.getLastModified(), "news didn't get last modified date set");
    assertTrue(storedArticleList.getLastModified().getTime() > testStart, "last modified wasn't after test start");
    assertTrue(storedArticleList.getLastModified().getTime() > storedArticleList.getCreated().getTime(),
        "last modified wasn't after created");
  }
}

