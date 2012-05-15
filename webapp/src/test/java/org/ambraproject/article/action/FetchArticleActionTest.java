/*
 * $HeadURL: http://ambraproject.org/svn/ambra/ambra/branches/aggregation-refactor/webapp/src/test/java/org/ambraproject/article/action/FetchArticleActionTest.java $
 * $Id: FetchArticleActionTest.java 10967 2012-05-04 00:20:40Z josowski $
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.article.action;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.article.service.NoSuchArticleIdException;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.models.ArticleView;
import org.ambraproject.models.UserProfile;
import org.ambraproject.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Joe Osowski
 */
public class FetchArticleActionTest extends FetchActionTest {

  @Autowired
  protected FetchArticleAction action;

  @Autowired
  protected UserService userService;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @Test
  public void testFetchArticle() throws NoSuchArticleIdException {
    action.setArticleURI(getArticleToFetch().getDoi());

    dummyDataStore.deleteAll(ArticleView.class);

    String result = action.fetchArticleInfo();

    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    compareArticles(action.getArticleInfoX(), getArticleToFetch());

    assertEquals(dummyDataStore.getAll(ArticleView.class).size(), 0,
        "Action recorded an article view");
  }
}
