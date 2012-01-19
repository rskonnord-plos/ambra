/*
 * $HeadURL$
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

package org.topazproject.ambra.article;

import org.topazproject.ambra.search2.SearchHit;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Object that holds the result from Solr for the browse pages
 *
 */
public class BrowseResult implements Serializable {

  private static final long serialVersionUID = -5896324074315268868L;

  private long total;
  private ArrayList<SearchHit> articles;

  public ArrayList<SearchHit> getArticles() {
    return articles;
  }

  public void setArticles(ArrayList<SearchHit> articles) {
    this.articles = articles;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }
}
