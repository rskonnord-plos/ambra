/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2011 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.service.article;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import org.ambraproject.action.BaseHttpTest;
import org.ambraproject.service.search.SolrException;
import org.ambraproject.util.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test the {@link MostViewedArticleService}.  There are two aspects to this:
 * <p/>
 * <ol> <li>That you make the correct solr request</li> <li>That you correctly parse the results</li> </ol>
 *
 * @author Alex Kudlick 9/19/11
 */
public class MostViewedArticleServiceTest extends BaseHttpTest {

  @Autowired
  protected MostViewedArticleService mostViewedArticleService;

  private static final String EXPECTED_FQ_PARAM = "doc_type:full AND !article_type_facet:\"Issue Image\" AND cross_published_journal_key:journal";
  private static final String JOURNAL = "journal";
  private static final int NUM_DAYS = 14;
  private static final String VIEW_FIELD = "two_week_field";

  @Test
  public void testGetMostViewed() throws SolrException {
    final Map<String, String> headers = new HashMap<String, String>(3);
    httpEndpoint.whenAnyExchangeReceived(new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();
        headers.put("sort", in.getHeader("sort", String.class));
        headers.put("fq", in.getHeader("fq", String.class));
        exchange.getOut().setBody(testSolrXml);
      }
    });

    List<Pair<String, String>> mostViewedArticles = mostViewedArticleService.getMostViewedArticles(JOURNAL, 10, NUM_DAYS);
    //check the headers
    assertEquals(headers.get("sort"), VIEW_FIELD + " desc", "solr request didn't have correct sort field");
    assertEquals(headers.get("fq"), EXPECTED_FQ_PARAM, "solr request didn't have correct fq param");

    //Check that the solr xml was parsed correctly
    assertNotNull(mostViewedArticles, "returned null list of most viewed articles");
    assertEquals(mostViewedArticles.size(), articlesFromSolrXml.size(), "returned incorrect number of articles");
    for (int i = 0; i < mostViewedArticles.size(); i++) {
      Pair<String, String> actual = mostViewedArticles.get(i);
      Pair<String, String> expected = articlesFromSolrXml.get(i);
      assertEquals(actual.getFirst(), expected.getFirst(), "Didn't have correct doi for entry " + i);
      assertEquals(actual.getSecond(), expected.getSecond(), "Didn't have correct title for entry " + i);
    }
  }
}
