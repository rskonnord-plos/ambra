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

import org.ambraproject.BaseTest;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class InboundLinkTranslatorTest extends BaseTest {

  @Autowired
  protected Configuration configuration;

  private static final String SAMPLE_DOI = "info:doi/10.1371/journal.pone.0000000";

  // Convenience method
  private static String getDoi(InboundLinkTranslator xlator, String url) throws MalformedURLException {
    return xlator.getDoi(new URL(url));
  }


  @Test
  public void testGlobalResolver() throws Exception {
    final InboundLinkTranslator global = InboundLinkTranslator.GLOBAL_RESOLVER;
    assertNotNull(global);

    assertEquals(getDoi(global, "http://dx.doi.org/10.1371/journal.pone.0000000"), SAMPLE_DOI);
    assertEquals(getDoi(global, "http://dx.doi.org/info:doi/10.1371/journal.pone.0000000"), SAMPLE_DOI);
    assertEquals(getDoi(global, "http://dx.doi.org/info%3Adoi%2F10.1371%2Fjournal.pone.0000000"), SAMPLE_DOI);
    assertEquals(getDoi(global, "http://DX.DOI.ORG/10.1371/journal.pone.0000000"), SAMPLE_DOI);
    assertNull(getDoi(global, "http://doi.org/10.1371/journal.pone.0000000"));
    assertNull(getDoi(global, "http://www.dx.doi.org/10.1371/journal.pone.0000000"));
    assertNull(getDoi(global, "http://dx.doi.org/"));
    assertNull(getDoi(global, "http://dx.doi.org"));
  }

  @Test
  public void testLocalResolver() throws Exception {
    final URL localResolver = new URL(configuration.getString(InboundLinkTranslator.LOCAL_RESOLVER_KEY));
    final String resolverHost = localResolver.getHost();
    final String resolverRoot = localResolver.getPath();
    final InboundLinkTranslator local = InboundLinkTranslator.forLocalResolver(configuration);
    assertNotNull(local);

    assertEquals(getDoi(local, "http://" + resolverHost + resolverRoot + "10.1371/journal.pone.0000000"), SAMPLE_DOI);
    assertEquals(getDoi(local, "http://" + resolverHost + "/10.1371/journal.pone.0000000"), SAMPLE_DOI);
    assertEquals(getDoi(local, "http://" + resolverHost.toUpperCase() + "/10.1371/journal.pone.0000000"), SAMPLE_DOI);
    assertNull(getDoi(local, "http://example.com" + resolverRoot + "10.1371/journal.pone.0000000"));
    assertNull(getDoi(local, "http://example.com" + "/10.1371/journal.pone.0000000"));
    assertNull(getDoi(local, "http://" + resolverHost + resolverRoot + "info:doi/10.1371/journal.pone.0000000"));
    assertNull(getDoi(local, "http://" + resolverHost + "/info:doi/10.1371/journal.pone.0000000"));
  }

  @Test
  public void testForJournal() throws Exception {
    String journalUrl = configuration.getString(String.format(InboundLinkTranslator.JOURNAL_HOST_FORMAT, defaultJournal.getJournalKey()));
    final String journalHost = new URL(journalUrl).getHost();
    final String articleAction = configuration.getString(InboundLinkTranslator.ARTICLE_ACTION_KEY);
    final InboundLinkTranslator xlator = InboundLinkTranslator.forJournal(defaultJournal, configuration);
    assertNotNull(xlator);

    assertEquals(getDoi(xlator, "http://" + journalHost + "/" + articleAction + SAMPLE_DOI), SAMPLE_DOI);
    assertEquals(getDoi(xlator, "http://www." + journalHost + "/" + articleAction + SAMPLE_DOI), SAMPLE_DOI);
    assertEquals(getDoi(xlator, "http://" + journalHost.toUpperCase() + "/" + articleAction + SAMPLE_DOI), SAMPLE_DOI);
    assertEquals(getDoi(xlator, "http://wWw." + journalHost.toUpperCase() + "/" + articleAction + SAMPLE_DOI), SAMPLE_DOI);

    assertNull(getDoi(xlator, "http://ww." + journalHost + "/" + articleAction + SAMPLE_DOI));
    assertNull(getDoi(xlator, "http://" + journalHost + "/" + SAMPLE_DOI));
    assertNull(getDoi(xlator, "http://" + journalHost + "/" + articleAction + "10.1371/journal.pone.0000000"));
    assertNull(getDoi(xlator, "http://" + journalHost + "/" + "10.1371/journal.pone.0000000"));
    assertNull(getDoi(xlator, "http://example.com/" + articleAction + SAMPLE_DOI));
    assertNull(getDoi(xlator, "http://www.example.com/" + articleAction + SAMPLE_DOI));
  }

}
