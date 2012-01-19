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

package org.topazproject.ambra.article.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.BaseWebTest;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.ArticleContributor;
import org.topazproject.ambra.models.Citation;
import org.topazproject.ambra.models.DublinCore;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Basic test for {@link CreateCitation} action
 * @author Alex Kudlick
 */
public class CreateCitationTest extends BaseWebTest {

  @Autowired
  protected CreateCitation action;

  @DataProvider(name = "testCitation")
  public Object[][] getTestCitation() {
    String articleUri = "id:test-article-uri";
    Article article = new Article();
    article.setId(URI.create(articleUri));
    article.setDublinCore(new DublinCore());

    Citation citation = new Citation();
    citation.setTitle("test title");
    citation.setCitationType("type:test-citation");
    citation.setDay("1");
    citation.setMonth("02");
    citation.setDisplayYear("1999");
    citation.setELocationId("eLocationId");

    article.getDublinCore().setBibliographicCitation(citation);
    dummyDataStore.store(citation);
    dummyDataStore.store(article.getDublinCore());
    List<ArticleContributor> authors = new ArrayList<ArticleContributor>(2);
    ArticleContributor author1 = new ArticleContributor();
    author1.setFullName("Michael B. Eisen");
    author1.setGivenNames("Michael B.");
    author1.setSurnames("Eisen");
    author1.setIsAuthor(true);
    authors.add(author1);
    ArticleContributor author2 = new ArticleContributor();
    author2.setFullName("William T. Johnson");
    author2.setGivenNames("William T.");
    author2.setSurnames("Johnson");
    author2.setIsAuthor(true);
    authors.add(author2);

    dummyDataStore.store(authors);
    article.setAuthors(authors);
    dummyDataStore.store(article);
    return new Object[][]{
        {articleUri, citation, authors}
    };
  }


//  @Test(dataProvider = "testCitation", groups = {USER_GROUP})
//  public void testExecute(String articleUri, Citation expectedCitation, List<ArticleContributor> expectedAuthors) throws Exception {
//    action.setArticleURI(articleUri);
//    assertEquals(action.execute(), BaseActionSupport.SUCCESS, "Action didn't return success");
//    compareCitations(action.getCitation(), expectedCitation, false);
//    assertEquals(action.getAuthorList().size(), expectedAuthors.size(), "Didn't return correct number of authors");
//    for (int i = 0; i < expectedAuthors.size(); i++) {
//      ArticleContributor actual = action.getAuthorList().get(i);
//      ArticleContributor expected = expectedAuthors.get(i);
//      assertEquals(actual.getFullName(), expected.getFullName(), "Author didn't have correct full name");
//      assertEquals(actual.getSurnames(), expected.getSurnames(), "Author didn't have correct surname");
//      assertEquals(actual.getGivenNames(), expected.getGivenNames(), "Author didn't have correct given name");
//      assertTrue(actual.getIsAuthor(),"Author profile didn't indicate that it corresponded to an author");
//    }
//  }
}
