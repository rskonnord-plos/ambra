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
import org.ambraproject.models.Linkback;
import org.ambraproject.models.Pingback;
import org.ambraproject.models.Trackback;
import org.ambraproject.views.LinkbackView;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;

public class LinkbackServiceTest extends BaseHttpTest {

  @Autowired
  protected PingbackService pingbackService;
  @Autowired
  protected TrackbackService trackbackService;

  @DataProvider(name = "mixedLinkbacks")
  public Object[][] getMixedLinkbacks() throws InterruptedException {
    Article article = new Article();
    article.setDoi("id:article-for-LinkbackServiceTest");
    article.setTitle("A Popular Article");

    Collection<Linkback> linkbacks = new ArrayList<Linkback>(4);
    for (int i = 0; i < 5; i++) {
      Linkback linkback = (i % 2 == 0 ? new Trackback() : new Pingback());
      linkback.setTitle("A post at Blog #" + i);
      linkback.setUrl("http://example.com/blog" + i);
      if (linkback instanceof Trackback) {
        Trackback trackback = (Trackback) linkback;
        trackback.setBlogName("Blog #" + i);
        trackback.setExcerpt("We here at Blog #" + i + " think this article is good.");
      }
      linkbacks.add(linkback);
    }

    return new Object[][]{{article, linkbacks}};
  }

  @Test(dataProvider = "mixedLinkbacks")
  public void testGetLinkbacksForArticle(Article article, Collection<? extends Linkback> linkbacks) throws Exception {
    final int expectedCount = linkbacks.size();
    final String doi = article.getDoi();
    final Long articleId = Long.valueOf(dummyDataStore.store(article));

    Set<LinkbackView> expectedViews = new HashSet<LinkbackView>(linkbacks.size() * 4 / 3 + 1);
    long fakeTime = 0L;
    for (Linkback lb : linkbacks) {
      lb.setArticleID(articleId);
      Long lbId = Long.valueOf(dummyDataStore.store(lb));
      lb = dummyDataStore.get(lb.getClass(), lbId); // Replace getCreated() with dummyDataStore's representation

      Date timestamp = lb.getCreated();
      fakeTime += 1000L;
      timestamp.setTime(fakeTime);
      dummyDataStore.store(lb);

      expectedViews.add(new LinkbackView(lb, doi, article.getTitle()));
    }

    int countT = trackbackService.countLinkbacksForArticle(doi);
    int countP = pingbackService.countLinkbacksForArticle(doi); // Barely worth testing both, but why not
    assertEquals(countT, expectedCount);
    assertEquals(countP, expectedCount);

    List<LinkbackView> foundT = trackbackService.getLinkbacksForArticle(doi);
    List<LinkbackView> foundP = pingbackService.getLinkbacksForArticle(doi);

    // TODO Check chronological ordering (need to fake the test timestamps; real ones can be in same millisecond)
    assertEquals(new HashSet<LinkbackView>(foundT), expectedViews);
    assertEquals(new HashSet<LinkbackView>(foundP), expectedViews);
  }

}
