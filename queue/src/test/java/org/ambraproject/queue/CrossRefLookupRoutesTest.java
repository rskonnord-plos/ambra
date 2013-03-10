/*
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.queue;

import org.ambraproject.action.BaseTest;
import org.ambraproject.models.Article;
import org.ambraproject.models.CitedArticle;
import org.ambraproject.models.CitedArticleAuthor;
import org.ambraproject.routes.CrossRefLookupRoutes;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import static org.testng.Assert.assertNotNull;

/**
 * Unit test for the cross ref lookup route
 *
 * @author Joe Osowski
 */
@ContextConfiguration
public class CrossRefLookupRoutesTest extends BaseTest {
  @Produce(uri = CrossRefLookupRoutes.UPDATE_CITED_ARTICLE)
  protected ProducerTemplate start;

  private static final String DOI_1 = "info:doi/10.1371/journal.joe.1030540";

  @DataProvider(name="testData")
  public Object[][] testData() {
    Article article1 = new Article();
    article1.setDoi("info:doi/10.1371/Fake-Doi-For-article1");
    article1.setTitle("Fake Title for Article 1");
    article1.setState(Article.STATE_ACTIVE);

    article1.setCitedArticles(new ArrayList<CitedArticle>() {{
      for(int a = 0; a < 10; a++) {
        final String citeName = "cite-" + String.valueOf(a);

        add(new CitedArticle() {{
          setAuthors(new LinkedList<CitedArticleAuthor>() {{
            for(int b = 0; b < 5; b++) {
              final String authorN = citeName + "-author-" + String.valueOf(b);

              add(new CitedArticleAuthor() {{
                setFullName("fullName-" + authorN);
                setGivenNames("givenNames-" + authorN);
                setSuffix("suffix-" + authorN);
                setSurnames("surnames-" + authorN);
              }});
            }
          }});

          setCitationType("citationType-" + citeName);
          setDay("day-" + citeName);
          setDisplayYear("displayYear-" + citeName);
          seteLocationID("eLocationID-" + citeName);
          setIssue("issue-" + citeName);
          setJournal("journal-" + citeName);
          setMonth("month-" + citeName);
          setPages("pages-" + citeName);
          setPublisherLocation("publisherLocation-" + citeName);
          setSummary("summary-" + citeName);
          setTitle("title-" + citeName);
          setUrl("url-" + citeName);
          setVolume("volume-" + citeName);
          setVolumeNumber(4);
          setYear(2013);
        }});
      }
    }});

    article1.setDate(new Date());

    article1.setTypes(new HashSet<String>() {{
      add("http://rdf.plos.org/RDF/articleType/research-article");
    }});

    dummyDataStore.store(article1);

    return new Object[][] { new Article[] { article1 }};
  }

  @Test(dataProvider = "testData")
  @SuppressWarnings("unchecked")
  public void testRoute(final Article article1) throws Exception {

    start.sendBodyAndHeaders(article1.getDoi(), new HashMap() {{
      put(CrossRefLookupRoutes.HEADER_AUTH_ID, BaseTest.DEFAULT_USER_AUTHID);
    }});

    //Let the queue do it's job before checking results
    Thread.sleep(5000);

    Article result = dummyDataStore.get(Article.class, article1.getID());

    //TODO: Check that records are updated
    for(CitedArticle citedArticle : result.getCitedArticles()) {
      assertNotNull(citedArticle.getDoi());
    }
  }
}
