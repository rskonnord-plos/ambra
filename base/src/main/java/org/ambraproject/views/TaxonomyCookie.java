/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
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
package org.ambraproject.views;

import org.ambraproject.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A view class representing the data stored in the taxonomy flags cookie
 */
public class TaxonomyCookie {
  private static Logger log = LoggerFactory.getLogger(TaxonomyCookie.class);
  private final List<Pair<Long, Long>> articleCategories;

  /**
   * Create the cookie view from the string representation stored in a cookie
   * @param value the string representation from the cookie
   */
  public TaxonomyCookie(String value) {
    articleCategories = new ArrayList<Pair<Long, Long>>();

    for(String valueTemp : value.split("\\|")) {
      String[] sPair = valueTemp.split(",");

      if(sPair.length == 2) {
        try {
          long storedArticleID = Long.parseLong(sPair[0]);
          long storedCategoryID = Long.parseLong(sPair[1]);

          articleCategories.add(new Pair<Long, Long>(storedArticleID, storedCategoryID));
        } catch(NumberFormatException ex) {
          log.warn("Strange values stored in cookie: '{}'", valueTemp);
        }
      } else {
        log.warn("Strange values stored in cookie: '{}'", value);
      }
    }
  }

  /**
   * Create the cookie view from a list / Pair
   *
   * @param articleCategories the list / pair to construct the object out of.
   */
  public TaxonomyCookie(final List<Pair<Long, Long>> articleCategories) {
    this.articleCategories = articleCategories;
  }

  public List<Pair<Long, Long>> getArticleCategories() {
    return articleCategories;
  }

  //Get a string representation of the cookie
  public String toCookieString() {
    String result = "";

    for(Pair<Long, Long> pair : articleCategories) {
      result = result + "|" + pair.getFirst() + "," + pair.getSecond();
    }

    //Limit cookie length to 3500 characters to stay under the storage limit
    //We'll be loosing tracked flags, but at this point, I don't see another way to handle it
    //The actual limit is about 4000 bytes.  But I don't want to walk on other cookie values
    //set as the limit is by domain, not cookie
    if(result.length() > 3500) {
      result = result.substring(0, 3500);
    }

    return result;
  }
}
