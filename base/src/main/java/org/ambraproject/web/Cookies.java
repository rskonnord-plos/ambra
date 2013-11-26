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
package org.ambraproject.web;

import org.ambraproject.views.ArticleCategory;
import org.ambraproject.views.ArticleCategoryPair;
import org.ambraproject.views.TaxonomyCookie;
import org.apache.struts2.ServletActionContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;
import java.util.TreeSet;

public class Cookies {
  public static final String COOKIE_ARTICLE_CATEGORY_FLAGS = "ARTICLE_CATEGORY_FLAGS";

  /**
   * Get all the cookies associated with this request
   *
   * @return
   */
  public static Cookie[] getCookies() {
    HttpServletRequest request = ServletActionContext.getRequest();

    if(request == null) {
      throw new RuntimeException("HttpServletRequest is null");
    }

    if(request.getCookies() == null) {
      return new Cookie[] {};
    } else {
      return request.getCookies();
    }
  }

  /**
   * Get a specific value of a named cookie associated with this request
   *
   * @param name the name of the cookie
   * @return the cookie value
   */
  public static String getCookieValue(String name) {
    HttpServletRequest request = ServletActionContext.getRequest();

    if(request == null) {
      throw new RuntimeException("HttpServletRequest is null");
    }

    for(Cookie c : request.getCookies()) {
      if(c.getName().equals(name)) {
        return c.getValue();
      }
    }

    return null;
  }

  /**
   * Set a specific value of a named cookie associated with this request
   *
   * @param name the name of the cookie
   * @param value the string value of the cookie
   */
  public static void setCookieValue(String name, String value) {
    Cookie cookie = new Cookie(name, value);

    HttpServletResponse response = ServletActionContext.getResponse();

    if(response == null) {
      throw new RuntimeException("HttpServletResponse is null");
    }

    //So all cookies from the domain are accessible across all paths.
    cookie.setPath("/");

    //Let's make the cookie last a year
    int cookieAge = 60 * 60 * 24 * 365;
    cookie.setMaxAge(cookieAge);

    response.addCookie(cookie);
  }

  /**
   * Used to set a collection of articleID/CategoryIDs that are stored in
   * a cookie on the user's browser.  This is in turn is used to track categories they
   * have flagged to prevent users from flagging a term for an article multiple times
   *
   * @param flags the working set of ArticleCategories
   * @param articleID the current articleIO
   *
   * @return a new set with additional view data applied
   */
  public static Set<ArticleCategory> setAdditionalCategoryFlags(Set<ArticleCategory> flags, long articleID) {
    for(Cookie c : getCookies()) {
      if(c.getName().equals(COOKIE_ARTICLE_CATEGORY_FLAGS)) {
        String value = c.getValue();

        if(value != null) {
          flags = setAdditionalCategoryFlags(value, flags, articleID);
        }
      }
    }

    return flags;
  }

  /**
   * Used to set a collection of articleID/CategoryIDs that are stored in
   * a cookie on the user's browser.  This is in turn is used to track categories they
   * have flagged to prevent users from flagging a term for an article multiple times
   *
   * @param cookieValue the value of the current COOKIE_ARTICLE_CATEGORY_FLAGS cookie
   * @param flags the working set of ArticleCategories
   * @param articleID the current articleIO
   *
   * @return a new set with additional view data applied
   */
  public static Set<ArticleCategory> setAdditionalCategoryFlags(String cookieValue, Set<ArticleCategory> flags, long articleID) {
    //Check to see if the user has flagged any categories anonymously
    TaxonomyCookie taxonomyCookie = new TaxonomyCookie(cookieValue);
    Set<ArticleCategory> newCategories = new TreeSet<ArticleCategory>();

    for(ArticleCategory articleCategory : flags) {
      ArticleCategory articleCategoryTemp = null;

      for(ArticleCategoryPair articleCategories : taxonomyCookie.getArticleCategories()) {
        long curArticleID = articleCategories.getArticleID();
        long curCategoryID = articleCategories.getCategoryID();

        //If we find the user has flagged this pair, let's recreate the view setting the flag
        if(articleCategory.getCategoryID() == curCategoryID && articleID == curArticleID) {
          articleCategoryTemp = ArticleCategory.builder(articleCategory)
            .setFlagged(true).build();
        }
      }

      if(articleCategoryTemp != null) {
        newCategories.add(articleCategoryTemp);
      } else {
        //No match was found, append the old view to the new set
        newCategories.add(articleCategory);
      }
    }

    return newCategories;
  }
}