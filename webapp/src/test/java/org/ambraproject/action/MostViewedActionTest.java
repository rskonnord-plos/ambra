package org.ambraproject.action;

import com.opensymphony.xwork2.Action;
import org.ambraproject.BaseHttpTest;
import org.ambraproject.BaseWebTest;
import org.ambraproject.util.Pair;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 *
 * @author Alex Kudlick 5/15/12
 */
public class MostViewedActionTest extends BaseHttpTest {

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
      Pair<String, String> actual = mostViewedArticles.get(i);
      Pair<String, String> expected = articlesFromSolrXml.get(i);
      assertEquals(actual.getFirst(), expected.getFirst(), "Didn't have correct doi for entry " + i);
      assertEquals(actual.getSecond(), expected.getSecond(), "Didn't have correct title for entry " + i);
    }

  }

}
