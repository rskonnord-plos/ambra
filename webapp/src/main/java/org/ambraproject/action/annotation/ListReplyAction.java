/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
 *   http://plos.org
 *   http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.ambraproject.action.annotation;

import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.web.Cookies;
import org.ambraproject.action.article.ArticleHeaderAction;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.service.annotation.AnnotationService;
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.service.article.FetchArticleService;
import org.ambraproject.views.AnnotationView;
import org.ambraproject.views.ArticleCategory;
import org.ambraproject.views.AuthorView;
import org.ambraproject.views.JournalView;
import org.ambraproject.views.article.ArticleInfo;
import org.ambraproject.views.article.ArticleType;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.w3c.dom.Document;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Action class to get a list of replies to annotations.
 */
@SuppressWarnings("serial")
public class ListReplyAction extends BaseActionSupport implements ArticleHeaderAction {
  private static final Logger log = LoggerFactory.getLogger(ListReplyAction.class);

  private static final Set<AnnotationType> COMMENT_TYPES = Collections.unmodifiableSet(EnumSet.of(AnnotationType.COMMENT));

  protected AnnotationService annotationService;
  private ArticleService articleService;
  private FetchArticleService fetchArticleService;

  private Long root;
  private AnnotationView baseAnnotation;
  private ArticleInfo articleInfo;
  private ArticleType articleType;
  private List<AuthorView> authors;
  private int numComments;
  private boolean isResearchArticle;
  private boolean hasAboutAuthorContent;
  private Set<ArticleCategory> categories;
  private List<List<String>> articleIssues;
  private AnnotationView[] commentary = new AnnotationView[0];

  @Override
  public String execute() throws Exception {
    try {
      baseAnnotation = annotationService.getFullAnnotationView(root);
      Long articleID = baseAnnotation.getArticleID();
      articleInfo = articleService.getArticleInfo(articleID, getAuthId());
      articleType = articleInfo.getKnownArticleType();
      numComments = annotationService.countAnnotations(articleID, COMMENT_TYPES);

      Document doc = fetchArticleService.getArticleDocument(articleInfo);
      authors = fetchArticleService.getAuthors(doc);
      isResearchArticle = articleService.isResearchArticle(articleInfo);
      hasAboutAuthorContent = (AuthorView.anyHasAffiliation(authors)
          || CollectionUtils.isNotEmpty(fetchArticleService.getCorrespondingAuthors(doc))
          || CollectionUtils.isNotEmpty(fetchArticleService.getAuthorContributions(doc))
          || CollectionUtils.isNotEmpty(fetchArticleService.getAuthorCompetingInterests(doc)));

      this.categories = Cookies.setAdditionalCategoryFlags(articleInfo.getCategories(), articleInfo.getId());

      articleIssues = articleService.getArticleIssues(articleInfo.getDoi());
      commentary = annotationService.listAnnotations(articleInfo.getId(),
          EnumSet.of(AnnotationType.COMMENT),
          AnnotationService.AnnotationOrder.MOST_RECENT_REPLY);

    } catch (Exception ae) {
      log.error("Could not list all replies for root: " + root, ae);
      addActionError("Reply fetching failed with error message: " + ae.getMessage());
      return ERROR;
    }

    return SUCCESS;
  }

  /**
   * Textual view of the author names, for FreeMarker.
   *
   * @return a comma-delimited list of the names from author extras
   */
  @Override
  public String getAuthorNames() {
    return AuthorView.buildNameList(authors);
  }

  @Override
  public String getContributingAuthors() {
    return AuthorView.buildContributingAuthorsList(authors);
  }

  public void setRoot(final Long root) {
    this.root = root;
  }

  /**
   * @return Returns the baseAnnotation.
   */
  public AnnotationView getBaseAnnotation() {
    return baseAnnotation;
  }

  @Override
  public String getArticleURI() {
    return articleInfo.getDoi();
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
  public Set<JournalView> getJournalList() {
    return articleInfo.getJournals();
  }

  public List<AuthorView> getAuthors() {
    return authors;
  }

  public int getNumComments() {
    return numComments;
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

  @Required
  public void setArticleService(ArticleService articleService) {
    this.articleService = articleService;
  }

  @Required
  public void setAnnotationService(final AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  @Required
  public void setFetchArticleService(FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  /**
   * Return a list of this article's categories.
   *
   * Note: These values may be different pending the user's cookies then the values stored in the database.
   *
   * If a user is logged in, a list is built of categories(and if they have been flagged) for the article
   * from the database
   *
   * If a user is not logged in, a list is built of categories for the article.  Then we append (from a cookie)
   * flagged categories for this article
   *
   * @return Return a list of this article's categories
   */
  public Set<ArticleCategory> getCategories() {
    return categories;
  }

  public List<List<String>> getArticleIssues() {
    return articleIssues;
  }

  public AnnotationView[] getCommentary() {
    return commentary;
  }
}
