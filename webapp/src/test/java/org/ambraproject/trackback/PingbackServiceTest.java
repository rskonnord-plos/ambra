/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2007-2012 by Public Library of Science
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

package org.ambraproject.trackback;

import org.ambraproject.BaseHttpTest;
import org.ambraproject.models.Article;
import org.ambraproject.models.Pingback;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.configuration.Configuration;
import org.apache.xmlrpc.XmlRpcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class PingbackServiceTest extends BaseHttpTest {

  @Autowired
  protected PingbackService pingbackService;

  protected String defaultJournalHostname;
  protected String articleAction;

  private static class MockBlogPost implements Processor {
    private final String title;
    private final List<URL> links;

    private MockBlogPost(String title, List<URL> links) {
      this.title = title;
      this.links = links;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
      StringBuilder html = new StringBuilder();
      html.append("<html><head>\n");
      html.append("  <title>").append(title).append("</title>\n");
      html.append("</head><body>\n");
      html.append("  <p>Some leading text. ");
      for (URL link : links) {
        html.append("<a href=\"").append(link.toString()).append("\">A link to ").append(link.getHost()).append(".</a> ");
      }
      html.append("Some ending text.</p>\n");
      html.append("</body></html>\n");
      exchange.getOut().setBody(html.toString());
    }
  }

  @BeforeClass
  protected void setUpDefaultJournal() {
    Configuration configuration = ((PingbackServiceImpl) pingbackService).getConfiguration();
    defaultJournalHostname = InboundLinkTranslator.getHostnameForJournal(defaultJournal.getJournalKey(), configuration);
    articleAction = configuration.getString(InboundLinkTranslator.ARTICLE_ACTION_KEY);

  }

  private String getAddress(String doi) {
    return "http://" + defaultJournalHostname + "/" + articleAction + doi;
  }

  @DataProvider(name = "pingbackArticle")
  public Object[][] getPingbackArticle() throws Exception {
    List<Object[]> cases = new ArrayList<Object[]>();

    Article goodArticle = new Article("id:article-for-testCreatePingback-1");
    goodArticle.setJournals(Collections.singleton(defaultJournal));
    URL[] goodLinks = new URL[]{
        new URL("http://example.com/not-a-valid-link"),
        new URL(getAddress(goodArticle.getDoi()) + ".invalid"),
        new URL("http://example.com/" + articleAction + goodArticle.getDoi()), // right DOI, wrong website
        new URL(getAddress(goodArticle.getDoi())),
        new URL("http://example.com/not-a-valid-link-either"),
    };
    cases.add(new Object[]{goodArticle, goodLinks, defaultJournalHostname, null});

    Article badArticle = new Article("id:article-for-testCreatePingback-2");
    badArticle.setJournals(Collections.singleton(defaultJournal));
    URL[] badLinks = new URL[]{
        new URL("http://example.com/not-a-valid-link"),
        new URL(getAddress(badArticle.getDoi()) + ".invalid"),
        new URL("http://example.com/" + articleAction + badArticle.getDoi()), // right DOI, wrong website
        new URL("http://example.com/not-a-valid-link-either"),
    };
    cases.add(new Object[]{badArticle, badLinks, defaultJournalHostname, PingbackFault.NO_LINK_TO_TARGET});

    Article wrongHostArticle = new Article("id:article-for-testCreatePingback-3");
    wrongHostArticle.setJournals(Collections.singleton(defaultJournal));
    URL[] wrongHostLink = new URL[]{
        new URL(getAddress(wrongHostArticle.getDoi()) + ".invalid"),
    };
    String wrongHost = new URL("http://example.com/").getHost();
    cases.add(new Object[]{badArticle, badLinks, wrongHost, PingbackFault.TARGET_DNE});

    return cases.toArray(new Object[cases.size()][]);
  }

  @Test(dataProvider = "pingbackArticle")
  public void testCreatePingback(Article article, URL[] links, String pingbackServerHostname, PingbackFault expectedFault) throws Exception {

    final String doi = article.getDoi();
    Long articleId = Long.valueOf(dummyDataStore.store(article));

    String articleAddress = getAddress(doi);
    String title = "A Blog";
    httpEndpoint.whenAnyExchangeReceived(new MockBlogPost(title, Arrays.asList(links)));

    XmlRpcException fault = null;
    try {
      pingbackService.createPingback(new URI(endpointUrl), new URI(articleAddress), pingbackServerHostname);
    } catch (XmlRpcException e) {
      fault = e;
    }

    Pingback storedPingback = null;
    for (Pingback pb : dummyDataStore.getAll(Pingback.class)) {
      if (articleId.equals(pb.getArticleID()) && endpointUrl.equals(pb.getUrl())) {
        storedPingback = pb;
        break;
      }
    }

    if (expectedFault == null) {
      assertNull(fault, "Service threw an exception");
      assertNotNull(storedPingback, "Pingback not created");
      assertEquals(storedPingback.getTitle(), title, "Blog title not retrieved");
    } else {
      assertNotNull(fault, "Service didn't throw exception on invalid input");
      assertNull(storedPingback, "Pingback was created on invalid input");
      assertEquals(fault.code, expectedFault.getCode(), "Wrong XML-RPC fault code");
    }
  }

  @Test
  public void testBogusTargetUrl() throws Exception {
    Article article = new Article("id:article-for-testBogusTargetUrl");
    article.setJournals(Collections.singleton(defaultJournal));
    dummyDataStore.store(article);

    URL bogusTargetUrl = new URL("http://example.com/" + articleAction + article.getDoi());
    httpEndpoint.whenAnyExchangeReceived(new MockBlogPost("A Tricky Blog", Collections.singletonList(bogusTargetUrl)));

    XmlRpcException fault = null;
    try {
      pingbackService.createPingback(new URI(endpointUrl), bogusTargetUrl.toURI(), defaultJournalHostname);
    } catch (XmlRpcException e) {
      fault = e;
    }

    assertNotNull(fault, "Service didn't throw fault on bogus target URL");
    assertEquals(fault.code, PingbackFault.TARGET_DNE.getCode(), "Wrong XML-RPC fault code");
  }

}
