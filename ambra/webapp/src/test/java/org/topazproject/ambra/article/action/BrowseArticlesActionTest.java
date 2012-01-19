/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
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


import org.easymock.classextension.EasyMock;
import org.topazproject.ambra.article.service.BrowseService;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.model.article.ArticleInfo;
import org.topazproject.ambra.model.article.Years;
import org.testng.annotations.*;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.net.URI;

public class BrowseArticlesActionTest {

  @Test
  public void testExecuteForName() throws Exception {
    BrowseService browseService = EasyMock.createStrictMock(BrowseService.class);
    BrowseArticlesAction action = new BrowseArticlesAction();
    action.setStartPage(5);
    action.setPageSize(7);
    action.setCatName("category");
    action.setField("category");
    EasyMock.expect(browseService
        .getCategoryInfos())
        .andReturn(null);
    ArrayList<ArticleInfo> articleInfos = new ArrayList<ArticleInfo>();
    ArticleInfo articleInfo = new ArticleInfo();
    articleInfo.setId(URI.create("http://plos.org"));
    articleInfo.setDate(new Date());
    articleInfos.add(articleInfo);
    EasyMock.expect(browseService
        .getArticlesByCategory(EasyMock.eq("category"), EasyMock.eq(5), EasyMock.eq(7),
        EasyMock.aryEq(new int[]{0})))
        .andReturn(articleInfos);
    EasyMock.replay(browseService);
    action.setBrowseService(browseService);
    Assert.assertEquals(action.execute(), BaseActionSupport.SUCCESS);
    Assert.assertEquals(action.getArticleList(), articleInfos, "Articles not same");
    EasyMock.verify(browseService);
  }

  @Test
  public void testExecuteForDate() throws Exception {
    BrowseService browseService = EasyMock.createStrictMock(BrowseService.class);
    BrowseArticlesAction action = new BrowseArticlesAction();
    action.setStartPage(5);
    action.setPageSize(7);
    action.setCatName("category");
    action.setField("date");
    ArrayList<ArticleInfo> articleInfos = new ArrayList<ArticleInfo>();
    ArticleInfo articleInfo = new ArticleInfo();
    articleInfo.setId(URI.create("http://plos.org"));
    articleInfo.setDate(new Date());
    articleInfos.add(articleInfo);

    Calendar startDate = Calendar.getInstance();
    startDate.set(Calendar.HOUR_OF_DAY, 0);
    startDate.set(Calendar.MINUTE,      0);
    startDate.set(Calendar.SECOND,      0);
    startDate.set(Calendar.MILLISECOND, 0);
    Calendar endDate = (Calendar) startDate.clone();
    startDate.add(Calendar.DATE, -7);
    endDate.add(Calendar.DATE, 1);

    EasyMock.expect(browseService
        .getArticlesByDate(EasyMock.eq(startDate), EasyMock.eq(endDate), EasyMock.eq(5),
        EasyMock.eq(7), EasyMock.aryEq(new int[]{0})))
        .andReturn(articleInfos);
    EasyMock.expect(browseService
        .getArticleDates())
        .andReturn(new Years());

    EasyMock.replay(browseService);
    action.setBrowseService(browseService);
    Assert.assertEquals(action.execute(), BaseActionSupport.SUCCESS);
    Assert.assertEquals(action.getArticleList(), articleInfos, "Articles not same");
    EasyMock.verify(browseService);
  }
}
