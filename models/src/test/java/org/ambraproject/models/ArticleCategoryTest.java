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

public class ArticleCategoryTest extends BaseHibernateTest {

  @Test
  public void testSaveBasicCategory() {

    Journal journalOne = new Journal();
    journalOne.setJournalKey("PLOSONE");

    Journal journalBio = new Journal();
    journalBio.setJournalKey("PLOSBIOLOGY");

    ArticleCategory articleCategory1 = new ArticleCategory();
    articleCategory1.setDisplayName("News");
    articleCategory1.setArticleDois(Arrays.asList("doi1", "doi2", "doi3"));
    articleCategory1.setJournalID(journalOne.getID());

    Serializable id1 = hibernateTemplate.save(articleCategory1);

    ArticleCategory storedCategory1 = (ArticleCategory) hibernateTemplate.get(ArticleCategory.class, id1);
    assertNotNull(storedCategory1, "didn't save category for plosone");
    assertEquals(storedCategory1, articleCategory1, "didn't store correct category properties for plosone");
    assertNotNull(storedCategory1.getCreated(), "category didn't get created date set for plosone");

    ArticleCategory articleCategory2 = new ArticleCategory();
    articleCategory2.setDisplayName("News");
    articleCategory2.setArticleDois(Arrays.asList("doi1", "doi2", "doi3"));
    articleCategory2.setJournalID(journalBio.getID());

    Serializable id2 = hibernateTemplate.save(articleCategory2);

    ArticleCategory storedCategory2 = (ArticleCategory) hibernateTemplate.get(ArticleCategory.class, id2);
    assertNotNull(storedCategory2, "didn't save category for plosbio");
    assertEquals(storedCategory2, articleCategory2, "didn't store correct category properties for plosbio");
    assertNotNull(storedCategory2.getCreated(), "category didn't get created date set for plosbio");

  }

  @Test
  public void testUpdateCategory() {
    long testStart = Calendar.getInstance().getTimeInMillis();
    ArticleCategory articleCategory = new ArticleCategory();
    articleCategory.setDisplayName("Old News Article");
    List<String> articleDois = new ArrayList<String>(3);
    articleDois.add("old doi 1");
    articleDois.add("old doi 2");
    articleDois.add("old doi 3");

    articleCategory.setArticleDois(articleDois);


    Serializable id = hibernateTemplate.save(articleCategory);

    articleCategory.getArticleDois().remove(1);
    articleCategory.getArticleDois().add("new doi 4");
    articleCategory.setDisplayName("New News Articles");

    hibernateTemplate.update(articleCategory);

    ArticleCategory storedArticleCategory = (ArticleCategory) hibernateTemplate.get(ArticleCategory.class, id);
    assertEquals(storedArticleCategory, articleCategory, "didn't update news properties");
    assertNotNull(storedArticleCategory.getLastModified(), "news didn't get last modified date set");
    assertTrue(storedArticleCategory.getLastModified().getTime() > testStart, "last modified wasn't after test start");
    assertTrue(storedArticleCategory.getLastModified().getTime() > storedArticleCategory.getCreated().getTime(),
        "last modified wasn't after created");
  }
}

