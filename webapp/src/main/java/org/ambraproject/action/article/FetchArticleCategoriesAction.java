/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.action.article;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import org.ambraproject.action.BaseSessionAwareActionSupport;
import org.ambraproject.models.Article;
import org.ambraproject.models.Category;
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.service.article.NoSuchArticleIdException;
import org.springframework.beans.factory.annotation.Required;

import java.util.HashSet;
import java.util.Set;

/**
 * Action class that returns article categories as JSON.  If the categories include
 * nested sub-categories, they are returned as a single string delimited by
 * forward slashes ("/").
 */
public class FetchArticleCategoriesAction extends BaseSessionAwareActionSupport {

  private ArticleService articleService;

  private String articleURI;

  private Set<String> categories;

  @Override
  public String execute() throws Exception {
    Article article;
    try {
      article = articleService.getArticle(articleURI, getAuthId());
    } catch (NoSuchArticleIdException nsaie) {
      addActionError(String.format("Article %s not found", articleURI));
      return ERROR;
    }
    Set<Category> articleCategories = article.getCategories();
    categories = new HashSet<String>(articleCategories.size());
    for (Category category : articleCategories) {
      categories.add(category.getPath());
    }
    return SUCCESS;
  }

  @Required
  public void setArticleService(ArticleService articleService) {
    this.articleService = articleService;
  }

  @RequiredStringValidator(message = "Article URI is a required field")
  public String getArticleURI() {
    return articleURI;
  }

  public void setArticleURI(String articleURI) {
    this.articleURI = articleURI;
  }

  public Set<String> getCategories() {
    return categories;
  }
}
