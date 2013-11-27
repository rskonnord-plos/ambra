/* $HeadURL::                                                                            $
 * $Id$
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
package org.ambraproject.action.article;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.service.article.ArticleAssetService;
import org.ambraproject.service.article.ArticleAssetWrapper;
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.service.article.FetchArticleService;
import org.ambraproject.service.search.SearchService;
import org.ambraproject.service.xml.XMLService;
import org.ambraproject.views.CitationReference;
import org.ambraproject.views.article.ArticleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Fetch the secondary objects for a given uri
 */
public class SlideshowAction extends BaseActionSupport {
  private static final Logger log = LoggerFactory.getLogger(SlideshowAction.class);

  private String uri;
  private ArticleAssetWrapper[] articleAssetWrapper;
  private ArticleAssetService articleAssetService;
  private XMLService secondaryObjectService;
  private ArticleService articleService;
  private SearchService searchService;
  private FetchArticleService fetchArticleService;

  private String articleTitle;
  private String articleType;
  private List<String> authors;
  private String abstractText;
  private List<CitationReference> references;
  private String metaData;

  private String fetchReferences = "yes";
  private String fetchMetaData = "yes";
  private String fetchAbstract = "yes";

  /**
   * Action to return list of Secondary object for an article that are enclosed in Tables (table-warp)
   * and Figures (fig) tags.
   *
   * @return webork status string
   * @throws Exception
   */
  @Override
  public String execute() throws Exception {
    try {
      ArticleInfo articleInfo = this.articleService.getBasicArticleView(uri);
      this.articleTitle = articleInfo.getTitle();
      this.articleType = articleInfo.getArticleTypeForDisplay();
      this.authors = articleInfo.getAuthors();
      this.authors.addAll(articleInfo.getCollaborativeAuthors());

      articleAssetWrapper = articleAssetService.listFiguresTables(uri, getAuthId());
      ArrayList<ArticleAssetWrapper> figTables = new ArrayList<ArticleAssetWrapper>(articleAssetWrapper.length);
      String allTransformed;
      String[] elems;
      StringBuilder desc;

      for (ArticleAssetWrapper s: articleAssetWrapper) {
        figTables.add(s);
        try {
          allTransformed = secondaryObjectService.getTransformedDocument(s.getDescription());
          if (log.isDebugEnabled()){
            log.debug("Transformed figure captions for article: " + uri);
            log.debug(allTransformed);
          }
          elems = allTransformed.split("END_TITLE");
          desc = new StringBuilder();
          if (elems.length > 1) {
            s.setTransformedCaptionTitle(elems[0]);
            s.setPlainCaptionTitle(elems[0].replaceAll("<.*?>",""));
            desc.append(elems[1]);
            s.setTransformedDescription(desc.toString());
          } else if (elems.length == 1) {
            desc.append(elems[0]);
            s.setTransformedDescription(desc.toString());
          }
        } catch (Exception e) {
          log.warn("Could not transform description for Object: " + getUri(), e);
          s.setTransformedDescription(s.getDescription());
        }
      }

      articleAssetWrapper = figTables.toArray(new ArticleAssetWrapper[figTables.size()]);

      if(articleAssetWrapper.length == 0) {
        log.debug("There are no objects for URI: {}", uri);
        return INPUT;
      }

      if ("yes".equals(fetchAbstract)) {
        abstractText = searchService.fetchAbstractText(uri);
      }

      if ("yes".equals(fetchReferences)) {
        // references are extracted from XML document because the database
        // does not have correct values for mixed-citation types.
        ArticleInfo articleInfoX = articleService.getArticleInfo(uri, getAuthId());
        Document doc = this.fetchArticleService.getArticleDocument(articleInfoX);
        references = this.fetchArticleService.getReferences(doc);
      }

      if ("yes".equals(fetchMetaData)) {
        metaData = "TODO: get this data";
      }
    } catch (Exception ex) {
      log.info("Couldn't retrieve secondary object for URI: " + uri, ex);
      return INPUT;
    }
    return SUCCESS;
  }

  /**
   * Get a list of figure/table doi and its title
   * This is used in tooltip for the figshare tile in the article metrics tab.
   *
   * @return list of figure/table doi and title
   * @throws Exception
   */
  public String fetchFigureTables() throws Exception {

    articleAssetWrapper = articleAssetService.listFiguresTables(uri, getAuthId());

    if(articleAssetWrapper.length == 0) {
      log.debug("There are no objects for URI: {}", uri);
      return INPUT;
    }

    return SUCCESS;
  }

  @RequiredStringValidator(message = "Object URI is required.")
  public String getUri() {
    return uri;
  }

  /**
   * Set the uri
   * @param uri uri
   */
  public void setUri(final String uri) {
    this.uri = uri;
  }

  /**
   * Get the secondary objects.
   * @return secondary objects
   */
  public ArticleAssetWrapper[] getSecondaryObjects() {
    return articleAssetWrapper;
  }

  public String getArticleTitle() {
    return articleTitle;
  }

  public String getArticleType() {
    return articleType;
  }

  public List<String> getAuthors() {
    return authors;
  }

  public String getAbstractText() {
    return abstractText;
  }

  public List<CitationReference> getReferences() {
    return references;
  }

  public String getMetaData() {
    return metaData;
  }

  public void setFetchReferences(String fetchReferences) {
    this.fetchReferences = fetchReferences;
  }

  public void setFetchMetaData(String fetchMetaData) {
    this.fetchMetaData = fetchMetaData;
  }

  public void setFetchAbstract(String fetchAbstract) {
    this.fetchAbstract = fetchAbstract;
  }

  /**
   * Set the secondary objects
   * @param articleAssetService articleAssetService
   */
  @Required
  public void setArticleAssetService(final ArticleAssetService articleAssetService) {
    this.articleAssetService = articleAssetService;
  }

  /**
   * @param secondaryObjectService The secondaryObjectUtil to set.
   */
  @Required
  public void setSecondaryObjectService(XMLService secondaryObjectService) {
    this.secondaryObjectService = secondaryObjectService;
  }

  @Required
  public void setArticleService(ArticleService articleService) {
    this.articleService = articleService;
  }

  @Required
  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  @Required
  public void setFetchArticleService(FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }


}
