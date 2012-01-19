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

package org.topazproject.ambra.article.service;

import org.testng.annotations.Test;
import org.testng.Assert;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.topazproject.ambra.model.article.Years;
import org.topazproject.ambra.cache.CacheManager;
import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.ambra.cache.MockCache;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.otm.Session;
import org.topazproject.otm.Query;
import org.topazproject.otm.query.Results;

public class BrowseServiceTest {

  @Test
  public void testGetArticleDates() throws Exception{

    IMocksControl ctrl = EasyMock.createStrictControl();

    JournalService journalService = ctrl.createMock(JournalService.class);
    CacheManager cacheManager = ctrl.createMock(CacheManager.class);
    Session session = ctrl.createMock(Session.class);
    Query query = ctrl.createMock(Query.class);
    Results results = ctrl.createMock(Results.class);

    MockCache cache = new MockCache();
    cache.setCacheManager(cacheManager);

    cacheManager.registerListener(EasyMock.isA(AbstractObjectListener.class));
    EasyMock.expectLastCall().times(0,1);
    EasyMock.expect(journalService.getCurrentJournalName()).andReturn("this-test");
    EasyMock.expect(session.createQuery("select a.dublinCore.date from Article a;")).andReturn(query);
    EasyMock.expect(query.execute()).andReturn(results);
    results.beforeFirst();
    EasyMock.expectLastCall();
    EasyMock.expect(results.next()).andReturn(true);
    EasyMock.expect(results.getString(0)).andReturn("2008-02-22");
    EasyMock.expect(results.next()).andReturn(true);
    EasyMock.expect(results.getString(0)).andReturn("2008-11-01");
    EasyMock.expect(results.next()).andReturn(false);

    ctrl.replay();

    BrowseService service = new BrowseService();
    service.setBrowseCache(cache);
    service.setJournalService(journalService);
    service.setOtmSession(session);

    Years result = service.getArticleDates();

    Years dates = new Years();
    dates.getMonths(2008).getDays(2).add(22);
    dates.getMonths(2008).getDays(11).add(1);

    Assert.assertEquals(result, dates);
    ctrl.verify();
  }
}
