/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.views;

import org.ambraproject.models.CitedArticle;

/**
 * View Object for the citedArticle model class
 */
public class CitedArticleView {
  private String articleDoi;

  //TODO: Move into their own view object or make appropriate properties of this object
  //SE-133
  private CitedArticle citedArticle;

  public CitedArticleView(String articleDoi, CitedArticle citedArticle) {
    this.articleDoi = articleDoi;
    this.citedArticle = citedArticle;
  }

  public CitedArticle getCitedArticle() {
    return citedArticle;
  }

  public String getArticleDoi() {
    return articleDoi;
  }
}
