/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.models;

import java.util.List;

public class ArticleCategory extends AmbraEntity{

  private String displayName;
  private List<String> articleDois;
  private Long journalID;


  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public List<String> getArticleDois() {
    return articleDois;
  }

  public void setArticleDois(List<String> articleDois) {
    this.articleDois = articleDois;
  }

  public Long getJournalID() {
    return journalID;
  }

  public void setJournalID(Long journalID) {
    this.journalID = journalID;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ArticleCategory)) return false;

    ArticleCategory articleCategory = (ArticleCategory) o;

    if (getID() != null ? !getID().equals(articleCategory.getID()) : articleCategory.getID() != null) return false;
    if (articleDois != null ? !articleDois.equals(articleCategory.articleDois) : articleCategory.articleDois != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = getID() != null ? getID().hashCode() : 0;
    result = 31 * result + (articleDois != null ? articleDois.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ArticleCategory{" +
        "id='" + getID() + '\'' +
        ", articleDois=" + articleDois +
        '}';
  }
}
