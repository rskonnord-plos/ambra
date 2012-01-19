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


import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import org.topazproject.ambra.article.service.BrowseService;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.model.article.ArticleInfo;
import org.topazproject.ambra.model.article.Years;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.topazproject.ambra.search2.SearchHit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.net.URI;

public class BrowseArticlesActionTest {

  @Test
  public void testExecuteForName() throws Exception {
    //TODO need to update to use solr
  }

  @Test
  public void testExecuteForDate() throws Exception {
    //TODO need to update to use solr
  }
}
