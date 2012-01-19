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

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.article.service.ArticlePersistenceService;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.DublinCore;
import org.topazproject.ambra.models.Citation;
import org.topazproject.ambra.models.UserProfile;
import org.easymock.classextension.IMocksControl;
import static org.easymock.classextension.EasyMock.createStrictControl;
import static org.easymock.classextension.EasyMock.expect;

import java.net.URI;
import java.util.ArrayList;

/**
 * @author Dragisa Krsmanovic
 */
public class CreateCitationTest {

  @Test
  public void testExecute() throws Exception{

    CreateCitation action = new CreateCitation();


    IMocksControl control = createStrictControl();
    ArticlePersistenceService articlePersistenceService = control.createMock(ArticlePersistenceService.class);
    action.setArticlePersistenceService(articlePersistenceService);
    String articleUri = "http://topazproject/xyz";

    Article article = new Article();
    DublinCore dublinCore = new DublinCore();
    Citation citation = control.createMock(Citation.class);
    dublinCore.setBibliographicCitation(citation);
    article.setDublinCore(dublinCore);

    expect(articlePersistenceService.getArticle(URI.create(articleUri)))
        .andReturn(article);

    expect(citation.getAuthors())
        .andReturn(new ArrayList<UserProfile>());

    control.replay();

    action.setArticleURI(articleUri);
    assertEquals(action.execute(), BaseActionSupport.SUCCESS);
    assertEquals(action.getCitation(), citation);

    control.verify();
  }
}
