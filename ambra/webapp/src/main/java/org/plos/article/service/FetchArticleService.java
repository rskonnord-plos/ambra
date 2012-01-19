/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
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

package org.plos.article.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.List;

import javax.activation.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Required;

import org.plos.ApplicationException;
import org.plos.annotation.service.ArticleAnnotationService;
import org.plos.annotation.service.Annotator;
import org.plos.cache.Cache;
import org.plos.cache.ObjectListener;
import org.plos.models.Article;
import org.plos.models.ArticleAnnotation;
import org.plos.util.ArticleXMLUtils;
import org.plos.util.TextUtils;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Session;
import org.topazproject.otm.Interceptor.Updates;

/**
 * Fetch article service.
 */
public class FetchArticleService {

  /**
   * All Article(transformed)/ArticleInfo/Annotation/Citation cache activity is syncronized on
   * ARTICLE_LOCK.
   */
  public  static final String ARTICLE_LOCK     = "ArticleAnnotationCache-Lock-";
  private static final String ARTICLE_KEY      = "ArticleAnnotationCache-Article-";

  private String encodingCharset;
  private ArticleXMLUtils articleXmlUtils;

  private static final Log log = LogFactory.getLog(FetchArticleService.class);
  private ArticleAnnotationService articleAnnotationService;

  private Cache articleAnnotationCache;
  private Invalidator invalidator;

  private String getTransformedArticle(final String articleURI) throws ApplicationException {
    try {
      return articleXmlUtils.getTransformedDocument(getAnnotatedContentAsDocument(articleURI));
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error ("Could not transform article: " + articleURI, e);
      }
      if (e instanceof ApplicationException) {
        throw (ApplicationException)e;
      } else {
        throw new ApplicationException (e);
      }
    }
  }

  /**
   * Get the URI transformed as HTML.
   * @param articleURI articleURI
   * @return String representing the annotated article as HTML
   * @throws org.plos.ApplicationException ApplicationException
   * @throws java.rmi.RemoteException RemoteException
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public String getURIAsHTML(final String articleURI) throws ApplicationException,
                          RemoteException, NoSuchArticleIdException {
    final Object lock = (ARTICLE_LOCK + articleURI).intern();  // lock @ Article level

    return articleAnnotationCache.get(ARTICLE_KEY  + articleURI, -1,
            new Cache.SynchronizedLookup<String, ApplicationException>(lock) {
              public String lookup() throws ApplicationException {
                return getTransformedArticle(articleURI);
              }
            });
  }

  /**
   * Return the annotated content as a String
   * @param articleURI articleURI
   * @return an the annotated content as a String
   * @throws ParserConfigurationException ParserConfigurationException
   * @throws SAXException SAXException
   * @throws IOException IOException
   * @throws URISyntaxException URISyntaxException
   * @throws org.plos.ApplicationException ApplicationException
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   * @throws javax.xml.transform.TransformerException TransformerException
   */
  public String getAnnotatedContent(final String articleURI)
      throws ParserConfigurationException, SAXException, IOException, URISyntaxException,
             ApplicationException, NoSuchArticleIdException,TransformerException{
    return TextUtils.getAsXMLString(getAnnotatedContentAsDocument(articleURI));
  }

  /**
   * 
   * @param articleDOI - the DOI of the (Article) content
   * @return
   * @throws IOException
   * @throws NoSuchArticleIdException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws ApplicationException
   */
  private Document getAnnotatedContentAsDocument(final String articleDOI)
      throws IOException, NoSuchArticleIdException, ParserConfigurationException, SAXException,
             ApplicationException {
    DataSource content;
    try {
      content =
        articleXmlUtils.getArticleService().getContent(articleDOI, articleXmlUtils.getArticleRep());
    } catch (NoSuchObjectIdException ex) {
      throw new NoSuchArticleIdException(articleDOI,
                                         "(representation=" + articleXmlUtils.getArticleRep() + ")",
                                         ex);
    }

    final ArticleAnnotation[] annotations = articleAnnotationService.listAnnotations(articleDOI);
    return applyAnnotationsOnContentAsDocument(content, annotations);
  }

  private Document applyAnnotationsOnContentAsDocument(DataSource content,
                                                       ArticleAnnotation[] annotations)
          throws ApplicationException {
    try {
      Document doc = articleXmlUtils.createDocBuilder().parse(content.getInputStream());
      if (annotations.length != 0)
        return Annotator.annotateAsDocument(doc, annotations);
      else
        return doc;
    } catch (Exception e){
      if (log.isErrorEnabled()) {
        log.error("Could not apply annotations to article: " + content.getName(), e);
      }
      throw new ApplicationException("Applying annotations failed for resource:" +
                                     content.getName(), e);
    }
  }

  /**
   * Getter for ArticleAnnotationService
   * 
   * @return the articleAnnotationService
   */
  public ArticleAnnotationService getArticleAnnotationService() {
    return articleAnnotationService;
  }

  /**
   * Setter for articleAnnotationService
   * 
   * @param articleAnnotationService articleAnnotationService
   */
  public void setArticleAnnotationService(final ArticleAnnotationService articleAnnotationService) {
    this.articleAnnotationService = articleAnnotationService;
  }

  /**
   * Get a list of ids of all articles that match the given criteria.
   *
   * @param startDate startDate
   * @param endDate   endDate
   * @param state     array of matching state values
   * @return list of article uri's
   * @throws ApplicationException ApplicationException
   */
  public List<String> getArticleIds(String startDate, String endDate, int[] state)
      throws ApplicationException {
    try {
      return articleXmlUtils.getArticleService().getArticleIds(startDate, endDate, state, true);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Set the encoding charset
   * @param encodingCharset encodingCharset
   */
  public void setEncodingCharset(final String encodingCharset) {
    this.encodingCharset = encodingCharset;
  }

  /**
   * @param articleAnnotationCache The Article(transformed)/ArticleInfo/Annotation/Citation cache
   *   to use.
   */
  @Required
  public void setArticleAnnotationCache(Cache articleAnnotationCache) {
    this.articleAnnotationCache = articleAnnotationCache;
    if (invalidator == null) {
      invalidator = new Invalidator();
      articleAnnotationCache.getCacheManager().registerListener(invalidator);
    }
  }

  /**
   * @param articleXmlUtils The articleXmlUtils to set.
   */
  public void setArticleXmlUtils(ArticleXMLUtils articleXmlUtils) {
    this.articleXmlUtils = articleXmlUtils;
  }

  /**
   * @param articleURI articleURI
   * @return Article
   * @throws ApplicationException ApplicationException
   * @see ArticleOtmService#getArticle(java.net.URI)
   */
  public Article getArticleInfo(final String articleURI) throws ApplicationException {
    try {
      Article article = articleXmlUtils.getArticleService().getArticle(new URI(articleURI));
      if (article == null) {
        throw new ApplicationException("null Article for: " + articleURI);
      }
      return article;
    } catch (NoSuchArticleIdException nsaie) {
       throw new ApplicationException(nsaie);
    } catch (URISyntaxException use) {
       throw new ApplicationException(use);
    }
  }

  private class Invalidator implements ObjectListener {
    public void objectChanged(Session session, ClassMetadata cm, String id, Object o,
        Updates updates) {
      handleEvent(id, o, updates, false);
    }
    public void objectRemoved(Session session, ClassMetadata cm, String id, Object o) {
      handleEvent(id, o, null, true);
    }
    private void handleEvent(String id, Object o, Updates updates, boolean removed) {
      if ((o instanceof Article) && removed) {
        if (log.isDebugEnabled())
          log.debug("Invalidating transformed-article for the article that was deleted.");
        articleAnnotationCache.remove(ARTICLE_KEY + id);
      } else if (o instanceof ArticleAnnotation) {
        if (log.isDebugEnabled())
          log.debug("ArticleAnnotation changed/deleted. Invalidating transformed-article " + 
              " for the article this was annotating or is about to annotate.");
        articleAnnotationCache.remove(ARTICLE_KEY + ((ArticleAnnotation)o).getAnnotates().toString());
        if ((updates != null) && updates.isChanged("annotates")) {
           List<String> v = updates.getOldValue("annotates");
           if (v.size() == 1)
             articleAnnotationCache.remove(ARTICLE_KEY + v.get(0));
        }
      }
    }
  }
}
