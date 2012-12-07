/* $HeadURL:: http://ambraproject.org/svn/ambra/ambra/branches/create-permissions/webapp#$
 * $Id:GetAnnotationAction.java 722 2006-10-02 16:42:45Z viru $
 *
 * Copyright (c) 2006-2010 by Public Library of Science
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
package org.ambraproject.action.annotation;

import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.action.article.ArticleHeaderAction;
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.service.article.FetchArticleService;
import org.ambraproject.views.AuthorView;
import org.ambraproject.views.JournalView;
import org.ambraproject.views.article.ArticleInfo;
import org.ambraproject.views.article.ArticleType;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;
import org.w3c.dom.Document;

import java.util.List;
import java.util.Set;

/**
 * Basic action for the start discussion page. just fetches up article doi and title.
 *
 * @author Alex Kudlick 4/12/12
 */
public class StartDiscussionAction extends BaseActionSupport implements ArticleHeaderAction {

  private ArticleService articleService;
  private FetchArticleService fetchArticleService;

  private String articleURI;
  private ArticleInfo articleInfo;
  private ArticleType articleType;
  private boolean isResearchArticle;
  private boolean hasAboutAuthorContent;
  private List<AuthorView> authors;


  @Override
  public String execute() throws Exception {
    articleInfo = articleService.getArticleInfo(articleURI, getAuthId());
    articleType = articleInfo.getKnownArticleType();

    Document doc = fetchArticleService.getArticleDocument(articleInfo);
    isResearchArticle = articleService.isResearchArticle(articleInfo);
    authors = fetchArticleService.getAuthors(doc);
    hasAboutAuthorContent = (AuthorView.anyHasAffiliation(authors)
        || CollectionUtils.isNotEmpty(fetchArticleService.getCorrespondingAuthors(doc))
        || CollectionUtils.isNotEmpty(fetchArticleService.getAuthorContributions(doc))
        || CollectionUtils.isNotEmpty(fetchArticleService.getAuthorCompetingInterests(doc)));

    return SUCCESS;
  }

  @Override
  public String getArticleURI() {
    return articleURI;
  }

  @Override
  public List<AuthorView> getAuthors() {
    return authors;
  }

  @Override
  public Set<JournalView> getJournalList() {
    return articleInfo.getJournals();
  }

  @Override
  public String getAuthorNames() {
    return AuthorView.buildNameList(authors);
  }

  @Override
  public String getContributingAuthors() {
    return AuthorView.buildContributingAuthorsList(authors);
  }

  @Required
  public void setArticleService(ArticleService articleService) {
    this.articleService = articleService;
  }

  @Required
  public void setFetchArticleService(FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  public void setArticleURI(String articleURI) {
    this.articleURI = articleURI;
  }

  public ArticleInfo getArticleInfo() {
    return articleInfo;
  }

  /**
   * Alias for adapting to FreeMarker.
   * @deprecated Prefer {@link #getArticleInfo()} in Java code.
   */
  @Override
  @Deprecated
  public ArticleInfo getArticleInfoX() {
    return getArticleInfo();
  }

  @Override
  public boolean getIsResearchArticle() {
    return isResearchArticle;
  }

  public boolean getHasAboutAuthorContent() {
    return hasAboutAuthorContent;
  }

  public ArticleType getArticleType() {
    return articleType;
  }

}
