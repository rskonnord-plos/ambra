/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.action.article;

import org.ambraproject.action.AmbraWebTest;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.ambraproject.action.BaseActionSupport;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.assertEquals;


/**
 * Basic test for {@link SlideshowAction} action
 * @author Joe Osowski
 */
public class SlideshowActionTest extends AmbraWebTest {

  @Autowired
  protected SlideshowAction action;

  @DataProvider(name = "testSlideshow")
  public Object[][] getTestCitation() {
    String articleUri = "id:test-article-uri";
    Article article = new Article();
    article.setDoi(articleUri);
    article.setDate(new Date());

    dummyDataStore.store(article);

    return new Object[][]{ { articleUri } };
  }

  @Test(dataProvider = "testSlideshow")
  public void testExecute(String articleUri) throws Exception
  {
    /* Test that if an article exists without related content, input is returned */
    action.setUri(articleUri);
    assertEquals(action.execute(), BaseActionSupport.INPUT, "Action didn't return input");
  }

  @DataProvider(name = "articleWithAssets")
  public Object[][] getArticleWithAssets() {
    String doi = "info:doi/10.1371/Fake-Doi-For-article1";

    Article article1 = new Article();
    article1.setDoi(doi);
    article1.setTitle("Fake Title for Article 1");
    article1.seteIssn("Fake EIssn for Article 1");
    article1.setState(Article.STATE_ACTIVE);
    article1.setArchiveName("Fake ArchiveName for Article 1");
    article1.setDescription("Fake Description for Article 1");
    article1.setRights("Fake Rights for Article 1");
    article1.setLanguage("Fake Language for Article 1");
    article1.setFormat("Fake Format for Article 1");
    article1.setVolume("Fake Volume for Article 1");
    article1.setIssue("Fake Issue for Article 1");
    article1.setPublisherLocation("Fake PublisherLocation for Article 1");
    article1.setPublisherName("Fake PublisherName for Article 1");
    article1.setJournal("Fake Journal for Article 1");

    List<ArticleAsset> assetsForArticle1 = new LinkedList<ArticleAsset>();
    ArticleAsset asset1ForArticle1 = new ArticleAsset();
    asset1ForArticle1.setContentType("Fake ContentType for asset1ForArticle1");
    asset1ForArticle1.setContextElement("fig");
    asset1ForArticle1.setDoi("info:doi/10.1371/Fake-Doi-For-asset1ForArticle1");
    asset1ForArticle1.setExtension("Fake Name for asset1ForArticle1");
    asset1ForArticle1.setSize(1000001l);
    asset1ForArticle1.setCreated(new Date());
    asset1ForArticle1.setLastModified(new Date());
    assetsForArticle1.add(asset1ForArticle1);

    ArticleAsset asset2ForArticle1 = new ArticleAsset();
    asset2ForArticle1.setContentType("Fake ContentType for asset2ForArticle1");
    asset2ForArticle1.setContextElement("table-wrap");
    asset2ForArticle1.setDoi("info:doi/10.1371/Fake-Doi-For-asset2ForArticle1");
    asset2ForArticle1.setExtension("Fake Name for asset2ForArticle1");
    asset2ForArticle1.setSize(1000002l);
    assetsForArticle1.add(asset2ForArticle1);

    ArticleAsset asset3ForArticle1 = new ArticleAsset();
    asset3ForArticle1.setContentType("Fake ContentType for asset3ForArticle1");
    asset3ForArticle1.setContextElement("alternatives");
    asset3ForArticle1.setDoi("info:doi/10.1371/Fake-Doi-For-asset3ForArticle1");
    asset3ForArticle1.setExtension("Fake Name for asset3ForArticle1");
    asset3ForArticle1.setSize(1000003l);
    assetsForArticle1.add(asset3ForArticle1);
    article1.setAssets(assetsForArticle1);

    dummyDataStore.store(article1);

    return new Object[][]{ { doi } };
  }

  @Test(dataProvider = "articleWithAssets")
  public void testGetFigureTables(String articleUri) throws Exception
  {
    action.setUri(articleUri);
    String result = action.fetchFigureTables();
    assertEquals(result, BaseActionSupport.SUCCESS, "Action didn't return input");
  }

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }
}