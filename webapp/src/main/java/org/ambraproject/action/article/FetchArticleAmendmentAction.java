/*
 * Copyright (c) 2006-2014 by Public Library of Science
 *
 *    http://plos.org
 *    http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.action.article;

import org.ambraproject.action.BaseSessionAwareActionSupport;
import org.ambraproject.models.ArticleRelationship;
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.views.ArticleAmendment;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for solr . It returns the article amendments of type eoc or retractions to the client.
 */
public class FetchArticleAmendmentAction extends BaseSessionAwareActionSupport {
  private ArticleService articleService;
  private String articleURI;
  private List<ArticleAmendment> amendments = new ArrayList<ArticleAmendment>();

  @Override
  public String execute() throws Exception {
    List<ArticleRelationship> relatedArticles;
    try {
      relatedArticles = articleService.getArticleAmendments(articleURI);
    } catch (IllegalArgumentException e) {
      addActionError(String.format("articleURI = null"));
      return ERROR;
    }

    for (ArticleRelationship relationship : relatedArticles) {
      amendments.add(ArticleAmendment
              .builder()
              .setParentArticleURI(articleURI)
              .setOtherArticleDoi(relationship.getOtherArticleDoi())
              .setRelationshipType(relationship.getType())
              .build());
    }
    return SUCCESS;
  }

  public String getArticleURI() {
    return articleURI;
  }

  public void setArticleURI(String articleURI) {
    this.articleURI = articleURI;
  }

  public void setArticleService(ArticleService articleService) {
    this.articleService = articleService;
  }

  public List<ArticleAmendment> getAmendments() {
    return amendments;
  }
}

