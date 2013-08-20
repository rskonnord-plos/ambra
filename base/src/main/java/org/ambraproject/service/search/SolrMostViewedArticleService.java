/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
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

package org.ambraproject.service.search;

import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAuthor;
import org.ambraproject.models.ArticleList;
import org.ambraproject.service.article.MostViewedArticleService;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.util.Pair;
import org.ambraproject.views.article.HomePageArticleInfo;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Alex Kudlick Date: 4/19/11
 *         <p/>
 *         org.ambraproject.solr
 */
public class SolrMostViewedArticleService extends HibernateServiceImpl implements MostViewedArticleService {
  private SolrFieldConversion solrFieldConverter;
  private SolrHttpService solrHttpService;

  /**
   * Cache for the most viewed results. This is a one-off caching implementation, but since this is a spring-injected
   * bean, there will only be one cache per ambra instance. Also, the cache will contain 2 strings per article (doi and
   * title) for each of the journals, which is 100 (relatively small) strings if the limit for articles is 5 and there
   * are 10 journals
   */
  private ConcurrentMap<String, MostViewedCache> cachedMostViewedResults = new ConcurrentHashMap<String, MostViewedCache>();
  private static final String DOI_ATTR = "id";
  private static final String TITLE_ATTR = "title_display";
  private static final String STRIKING_ATTR = "striking_image";
  private static final String AUTHORS_ATTR = "author_display";
  private static final String ABSTRACT_ATTR = "abstract_primary_display";

  @Override
  public List<Pair<String, String>> getMostViewedArticles(String journal, int limit, Integer numDays) throws SolrException {
    //check if we still have valid results in the cache
    MostViewedCache cache = cachedMostViewedResults.get(journal);
    if (cache != null && cache.isValid()) {
      return cache.getArticles();
    }

    Map<String, String> params = new HashMap<String, String>();
    params.put("fl", DOI_ATTR + "," + TITLE_ATTR);
    params.put("fq", "doc_type:full AND !article_type_facet:\"Issue Image\" AND cross_published_journal_key:" + journal);
    params.put("start", "0");
    params.put("rows", String.valueOf(limit));
    params.put("indent", "off");
    String sortField = (numDays != null) ? solrFieldConverter.getViewCountingFieldName(numDays)
        : solrFieldConverter.getAllTimeViewsField();
    params.put("sort", sortField + " desc");

    Document doc = solrHttpService.makeSolrRequest(params);

    List<Pair<String, String>> articles = new ArrayList<Pair<String, String>>(limit);

    //get the children of the "result" node
    XPath xPath = XPathFactory.newInstance().newXPath();
    try {
      Integer count = Integer.valueOf(xPath.evaluate("count(//result/doc)", doc));
      for (int i = 1; i <= count; i++) {
        String doi = xPath.evaluate("//result/doc[" + i + "]/str[@name = '" + DOI_ATTR + "']/text()", doc);
        String title = xPath.evaluate("//result/doc[" + i + "]/str[@name = '" + TITLE_ATTR + "']/text()", doc);
        articles.add(new Pair<String, String>(doi, title));
      }
    } catch (XPathExpressionException e) {
      throw new SolrException("Error parsing solr xml response", e);
    }

    //cache the results
    cachedMostViewedResults.put(journal, new MostViewedCache(articles));
    return articles;
  }


  @Override
  public List<HomePageArticleInfo> getMostViewedArticleInfo(String journal, int offset, int limit, Integer numDays) throws SolrException {
    //check if we still have valid results in the cache
    String cacheIndex = journal + ":mostviewed" + String.valueOf(offset) + ":" + String.valueOf(limit);
    MostViewedCache cache = cachedMostViewedResults.get(cacheIndex);
    if (cache != null && cache.isValid()) {
      return cache.getArticleInfo();
    }

    Map<String, String> params = new HashMap<String, String>();
    params.put("fl", DOI_ATTR + "," + TITLE_ATTR + "," + STRIKING_ATTR + "," + AUTHORS_ATTR + "," + ABSTRACT_ATTR);
    params.put("fq", "doc_type:full AND !article_type_facet:\"Issue Image\" AND cross_published_journal_key:" + journal);
    params.put("start", String.valueOf(offset));
    params.put("rows", String.valueOf(limit));
    params.put("indent", "off");
    String sortField = (numDays != null) ? solrFieldConverter.getViewCountingFieldName(numDays)
        : solrFieldConverter.getAllTimeViewsField();
    params.put("sort", sortField + " desc");

    Document doc = solrHttpService.makeSolrRequest(params);
    List<HomePageArticleInfo> articles = getArticleInfoFromSolrResponse(doc);
    //cache the results
    cachedMostViewedResults.put(cacheIndex, new MostViewedCache(articles));
    return articles;
  }

  @Override
  public List<HomePageArticleInfo> getRecentArticleInfo(String journal, int offset, int limit, List<URI> articleTypes) throws SolrException {
    //check if we still have valid results in the cache
    String cacheIndex = journal + ":recent:" + String.valueOf(offset) + ":" + String.valueOf(limit);
    MostViewedCache cache = cachedMostViewedResults.get(cacheIndex);
    if (cache != null && cache.isValid()) {
      return cache.getArticleInfo();
    }
    Map<String, String> params = new HashMap<String, String>();
    params.put("fl", DOI_ATTR + "," + TITLE_ATTR + "," + STRIKING_ATTR + "," + AUTHORS_ATTR + "," + ABSTRACT_ATTR);
    params.put("fq", "doc_type:full AND !article_type_facet:\"Issue Image\" AND cross_published_journal_key:" + journal);
    params.put("start", String.valueOf(offset));
    params.put("rows", String.valueOf(limit));
    params.put("indent", "off");
    params.put("sort", "publication_date desc");

    Document doc = solrHttpService.makeSolrRequest(params);
    List<HomePageArticleInfo> articles = getArticleInfoFromSolrResponse(doc);
    //cache the results
    cachedMostViewedResults.put(cacheIndex, new MostViewedCache(articles));
    return articles;
  }

  private List<HomePageArticleInfo> getArticleInfoFromSolrResponse(Document doc) throws SolrException {
    List<HomePageArticleInfo> articles = new ArrayList<HomePageArticleInfo>();

    //get the children of the "result" node
    XPath xPath = XPathFactory.newInstance().newXPath();
    try {
      Integer count = Integer.valueOf(xPath.evaluate("count(//result/doc)", doc));
      for (int i = 1; i <= count; i++) {
        String doi = xPath.evaluate("//result/doc[" + i + "]/str[@name = '" + DOI_ATTR + "']/text()", doc);
        String title = xPath.evaluate("//result/doc[" + i + "]/str[@name = '" + TITLE_ATTR + "']/text()", doc);
        String strkImg = xPath.evaluate("//result/doc[" + i + "]/str[@name = '" + STRIKING_ATTR + "']/text()", doc);
        String description = xPath.evaluate("//result/doc[" + i + "]/str[@name = '" + ABSTRACT_ATTR + "']/text()", doc);

        Integer authors_count = Integer.valueOf(xPath.evaluate("count(//result/doc[" + i + "]/arr[@name = '" + AUTHORS_ATTR + "']/str)", doc));

        List<String> authors = new ArrayList<String>();

        for(int j = 1; j <= authors_count; ++j) {
          String authorName = xPath.evaluate("//result/doc[" + i + "]/arr[@name = '" + AUTHORS_ATTR + "']/str[" + j + "]/text()", doc);
          authors.add(authorName);
        }

        String author = StringUtils.join(authors, ", ");

        HomePageArticleInfo article = new HomePageArticleInfo();
        article.setDoi(doi);
        article.setTitle(title);
        article.setStrkImgURI(strkImg);
        article.setAuthors(author);
        article.setDescription(description);

        articles.add(article);
      }
    } catch (XPathExpressionException e) {
      throw new SolrException("Error parsing solr xml response", e);
    }
    return articles;
  }

  /**
   * Returns a list of dois in a article list for the given Journal.
   * @return String of articleDois
   */
  @Transactional(readOnly = true)
  public ArticleList getArticleList(final String listCode) {
    try {
      return (ArticleList) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(ArticleList.class)
              .add(Restrictions.eq("listCode", listCode))
      ).get(0);
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  @Override
  public List<HomePageArticleInfo> getNewsArticleInfo(String listCode) {

    ArticleList al = getArticleList(listCode);

    List<HomePageArticleInfo> articleList = new ArrayList<HomePageArticleInfo>();

    for(String doi: al.getArticleDois()){
      List<Article> articles = hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Article.class)
              .add(Restrictions.eq("doi", doi)));
      if(articles.size() == 1) {
        Article article = articles.get(0);
        HomePageArticleInfo articleInfo = new HomePageArticleInfo();
        articleInfo.setDoi(article.getDoi());
        articleInfo.setTitle(article.getTitle());
        articleInfo.setStrkImgURI(article.getStrkImgURI());
        articleInfo.setDescription(article.getDescription());
        List<String> authors = new ArrayList<String>();
        for(ArticleAuthor author: article.getAuthors()) {
          String authorName = author.getFullName();
          authors.add(authorName);
        }
        String author = StringUtils.join(authors, ", ");
        articleInfo.setAuthors(author);
        articleList.add(articleInfo);
      }
    }

    return articleList;
  }

  @Required
  public void setSolrFieldConverter(SolrFieldConversion solrFieldConverter) {
    this.solrFieldConverter = solrFieldConverter;
  }

  @Required
  public void setSolrHttpService(SolrHttpService solrHttpService) {
    this.solrHttpService = solrHttpService;
  }
}
