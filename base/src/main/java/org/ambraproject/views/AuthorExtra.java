/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AuthorExtra {
  private String authorName;
  private ArrayList<String> affiliations = new ArrayList<String>();
  private String equalContrib;

  public String getAuthorName() {
    return authorName;
  }

  public void setAuthorName(String surName, String givenName) {
    this.authorName = givenName + " " + surName;
  }

  public ArrayList<String> getAffiliations() {
    return affiliations;
  }

  public void setAffiliations(ArrayList<String> affiliations) {
    this.affiliations = affiliations;
  }

  public String getEqualContrib() {
    return equalContrib;
  }

  public void setEqualContrib(String equalContrib) {
    this.equalContrib = equalContrib;
  }

  /**
   * Build a comma-delimited list of author names.
   *
   * @param authors a list of non-null authors
   * @return the list of author names, as text
   */
  public static String buildNameList(List<? extends AuthorExtra> authors) {
    Iterator<? extends AuthorExtra> iterator = authors.iterator();
    if (!iterator.hasNext()) {
      return "";
    }
    StringBuilder textList = new StringBuilder();
    textList.append(iterator.next().getAuthorName());
    while (iterator.hasNext()) {
      textList.append(", ").append(iterator.next().getAuthorName());
    }
    return textList.toString();
  }

}
