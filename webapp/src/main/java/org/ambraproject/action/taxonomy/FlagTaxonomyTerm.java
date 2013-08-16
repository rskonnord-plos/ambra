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
import org.ambraproject.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Flag a particular taxonomy term applied to an article
 */
public class FlagTaxonomyTerm extends BaseActionSupport {
  private static final Logger log = LoggerFactory.getLogger(FlagTaxonomyTerm.class);

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
    List<List<String>> nameCookieValuePairs = new ArrayList<List<String>>();

    if(articleID != null && categoryID != null) {
      Cookie cookie = getCookie(COOKIE_ARTICLE_CATEGORY_FLAGS);
      boolean flaggedAlready = false;

      if(cookie != null) {
        String cookieValue = cookie.getValue();

        if(cookieValue != null) {
          for(List<String> valuePairTemp : TextUtils.parsePipedCSV(cookieValue)) {
            //If somehow the valuePair is not two elements, log a warning, keep going
            if(valuePairTemp.size() == 2) {
              //Add existing value to new cookie value to be set
              nameCookieValuePairs.add(valuePairTemp);
              try {
                long storedArticleID = Long.valueOf(valuePairTemp.get(0));
                long storedCategoryID = Long.valueOf(valuePairTemp.get(1));

                if(articleID.equals(storedArticleID) && categoryID.equals(storedCategoryID)) {
                  flaggedAlready = true;
                }
              } catch (NumberFormatException ex) {
                log.warn("Strange values stored in: '{}' Cookie: '{}', Specifically: '{}, {}'",
                  new Object[] { COOKIE_ARTICLE_CATEGORY_FLAGS, cookieValue, valuePairTemp.get(0),
                    valuePairTemp.get(1) });
              }
            } else {
              log.warn("Strange values stored in: '{}' Cookie: '{}'", COOKIE_ARTICLE_CATEGORY_FLAGS, cookieValue);
            }
          }
        }
      }

      if(!flaggedAlready) {
        //Here add new value to the first in the list.  This way if cookie limit is reached, the oldest values will
        // get lost.
        List<List<String>> temp = new ArrayList<List<String>>();
        temp.add(Arrays.asList(new String[]{ String.valueOf(articleID), String.valueOf(categoryID) }));
        temp.addAll(nameCookieValuePairs);
        nameCookieValuePairs = temp;
        this.taxonomyService.flagTaxonomyTerm(articleID, categoryID, this.getAuthId());
      }

      //Limit cookie length to 3500 characters to stay under the storage limit
      //We'll be loosing tracked flags, but at this point, I don't see another way to handle it
      //The actual limit is about 4000 bytes.  But I don't want to walk on other cookie values
      //set as the limit is by domain, not cookie
      String cookeValue = TextUtils.createdPipedCSV(nameCookieValuePairs);
      if(cookeValue.length() > 3500) {
        cookeValue = cookeValue.substring(0, 3500);
      }

      setCookie(new Cookie(COOKIE_ARTICLE_CATEGORY_FLAGS, cookeValue));

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
