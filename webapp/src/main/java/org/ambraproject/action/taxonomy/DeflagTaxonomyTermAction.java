/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.action.taxonomy;

import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.service.taxonomy.TaxonomyService;
import org.ambraproject.util.Pair;
import org.ambraproject.views.TaxonomyCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * deflag a particular taxonomy term applied to an article
 */
public class DeflagTaxonomyTermAction extends BaseActionSupport {
  private static final Logger log = LoggerFactory.getLogger(DeflagTaxonomyTermAction.class);

  private TaxonomyService taxonomyService;

  private Long articleID;
  private Long categoryID;

  /**
   * Flag a particular taxonomy term applied to an article
   *
   * Check the user's cookies to make an attempt at stopping spamming one article/category with a lot of flags
   *
   * @return INPUT or SUCCESS
   *
   * @throws Exception
   */
  @Override
  public String execute() throws Exception {
    //new list devoid of article / category flag we're removing
    List<Pair<Long, Long>> valuePairs = new ArrayList<Pair<Long, Long>>();

    if(articleID != null && categoryID != null) {
      Cookie cookie = getCookie(COOKIE_ARTICLE_CATEGORY_FLAGS);

      if(cookie == null) {
        if(this.getAuthId() != null) {
          taxonomyService.deflagTaxonomyTerm(articleID, categoryID, this.getAuthId());
        } else {
          addActionError("Not logged in and flag cookie not found, you can not delete this flag.");
          return INPUT;
        }
      } else {
        String cookieValue = cookie.getValue();

        if(cookieValue != null) {
          TaxonomyCookie taxonomyCookie = new TaxonomyCookie(cookieValue);

          for(Pair<Long, Long> articleCategory : taxonomyCookie.getArticleCategories()) {
            long storedArticleID = articleCategory.getFirst();
            long storedCategoryID = articleCategory.getSecond();

            if(articleID.equals(storedArticleID) && categoryID.equals(storedCategoryID)) {
              taxonomyService.deflagTaxonomyTerm(articleID, categoryID, this.getAuthId());
              log.debug("Article/Category DE-flagged. ArticleID: {}, CategoryID: {}, AuthID: '{}'", new Object[] { articleID, categoryID, this.getAuthId() });
            } else {
              //Add one to the list.
              valuePairs.add(articleCategory);
            }
          }
        }
      }

      TaxonomyCookie newCookie = new TaxonomyCookie(valuePairs);
      setCookie(new Cookie(COOKIE_ARTICLE_CATEGORY_FLAGS, newCookie.toCookieString()));

      return SUCCESS;
    }

    addActionError("ArticleID or CategoryID not specified.");

    return INPUT;
  }

  public void setArticleID(Long articleID) {
    this.articleID = articleID;
  }

  public void setCategoryID(Long categoryID) {
    this.categoryID = categoryID;
  }

  @Required
  public void setTaxonomyService(TaxonomyService taxonomyService) {
    this.taxonomyService = taxonomyService;
  }
}
