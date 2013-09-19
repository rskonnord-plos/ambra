/*
 * $HeadURL: http://ambraproject.org/svn/ambra/ambra/branches/pom-refactor/webapp/src/main/java/org/ambraproject/action/MostViewedAction.java $
 * $Id: MostViewedAction.java 10472 2012-03-05 17:04:45Z josowski $
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
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

package org.ambraproject.action;

import com.opensymphony.xwork2.Action;
import org.ambraproject.util.Pair;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 *
 * @author Alex Kudlick 5/15/12
 */
public class MostViewedActionTest extends AmbraHttpTest {

  @Autowired
  protected MostViewedAction action;

  @Test
  @DirtiesContext
  public void test() {
    action.setRequest(BaseWebTest.getDefaultRequestAttributes());
    httpEndpoint.whenAnyExchangeReceived(new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
        exchange.getOut().setBody(testSolrXml);
      }
    });

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    List<Pair<String,String>> mostViewedArticles = action.getMostViewedArticles();
    assertNotNull(mostViewedArticles,"Action had null list of most viewed articles");
    assertEquals(mostViewedArticles.size(), articlesFromSolrXml.size(), "returned incorrect number of articles");
    for (int i = 0; i < mostViewedArticles.size(); i++) {
      Pair<String,String> actual = mostViewedArticles.get(i);
      Pair<String, String> expected = articlesFromSolrXml.get(i);
      assertEquals(actual.getFirst(), expected.getFirst(), "Didn't have correct doi for entry " + i);
      assertEquals(actual.getSecond(), expected.getSecond(), "Didn't have correct title for entry " + i);
    }

  }

}
