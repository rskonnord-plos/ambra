/*
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.models;

/**
 * POJO Object to contain all the parameters for saved searches.
 * It is mapped with table savedSearch.
 *
 * Each search has a serialized string of parameters, this
 * class is used in collections representing the unique set of searches
 * to avoid executing a search more then once
 *
 * @author Joe Osowski
 */
public class SavedSearchQuery extends AmbraEntity {

  private String searchParams;
  private String hash;

  public SavedSearchQuery() {
    super();
  }

  public SavedSearchQuery(String searchParams, String hash) {
    this();
    this.searchParams = searchParams;
    this.hash = hash;
  }

  /**
   * getter for searchParams
   * @return
   */
  public String getSearchParams() {
    return searchParams;
  }

  /**
   * setter for searchParams
   * @param searchParams
   */
  public void setSearchParams(String searchParams) {
    this.searchParams = searchParams;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SavedSearchQuery)) return false;

    SavedSearchQuery that = (SavedSearchQuery) o;

    if (getID() != null ? !getID().equals(that.getID()) : that.getID() != null) return false;
    if (searchParams != null ? !searchParams.equals(that.searchParams) : that.searchParams != null) return false;
    if (hash != null ? !hash.equals(that.hash) : that.hash != null) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = getID() != null ? getID().hashCode() : 0;
    result = 31 * result + (searchParams != null ? searchParams.hashCode() : 0);
    result = 31 * result + (hash != null ? hash.hashCode() : 0);
    return result;
  }
}
