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

package org.topazproject.ambra.article.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.activation.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.service.XMLService;
import org.topazproject.ambra.annotation.service.Annotator;
import org.topazproject.ambra.annotation.service.AnnotationService;
import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.util.TextUtils;
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

  private XMLService articleTransformService;

  private static final Logger log = LoggerFactory.getLogger(FetchArticleService.class);
  private AnnotationService annotationService;
  private ArticleOtmService articleService;

  private Cache articleAnnotationCache;
  private Invalidator invalidator;

  private String getTransformedArticle(final String articleURI)
      throws ApplicationException, NoSuchArticleIdException {
    try {
      return articleTransformService.getTransformedDocument(getAnnotatedContentAsDocument(articleURI));
    } catch (ApplicationException ae) {
      throw ae;
    } catch (NoSuchArticleIdException nsae) {
      throw nsae;
    } catch (Exception e) {
      throw new ApplicationException (e);
    }
  }

  /**
   * Get the URI transformed as HTML.
   * @param articleURI articleURI
   * @return String representing the annotated article as HTML
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  public String getURIAsHTML(final String articleURI) throws Exception {
    final Object lock = (ARTICLE_LOCK + articleURI).intern();  // lock @ Article level

    return articleAnnotationCache.get(ARTICLE_KEY  + articleURI, -1,
            new Cache.SynchronizedLookup<String, Exception>(lock) {
              public String lookup() throws Exception {
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
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
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
   * @return Article DOM document
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
      content = articleService.getContent(articleDOI, articleTransformService.getArticleRep());
    } catch (NoSuchObjectIdException ex) {
      throw new NoSuchArticleIdException(articleDOI,
                                         "(representation=" + articleTransformService.getArticleRep() + ")",
                                         ex);
    }

    final ArticleAnnotation[] annotations = annotationService.listAnnotations(articleDOI, null);
    return applyAnnotationsOnContentAsDocument(content, annotations);
  }

  private Document applyAnnotationsOnContentAsDocument(DataSource content,
                                                       ArticleAnnotation[] annotations)
          throws ApplicationException {
    try {
      if (log.isDebugEnabled())
        log.debug("Parsing article xml ...");

      Document doc = articleTransformService.createDocBuilder().parse(content.getInputStream());
      if (annotations.length == 0)
        return doc;

      if (log.isDebugEnabled())
        log.debug("Applying " + annotations.length + " annotations to article ...");

      return Annotator.annotateAsDocument(doc, annotations);
    } catch (Exception e){
      if (log.isErrorEnabled()) {
        log.error("Could not apply annotations to article: " + content.getName(), e);
      }
      throw new ApplicationException("Applying annotations failed for resource:" +
                                     content.getName(), e);
    }
  }

  /**
   * Getter for AnnotationService
   *
   * @return the annotationService
   */
  public AnnotationService getAnnotationService() {
    return annotationService;
  }

  /**
   * Setter for annotationService
   *
   * @param annotationService annotationService
   */
  @Required
  public void setAnnotationService(final AnnotationService annotationService) {
    this.annotationService = annotationService;
  }


  /**
   * @param articleService The articleService to set.
   */
  @Required
  public void setArticleService(ArticleOtmService articleService) {
    this.articleService = articleService;
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
   * @param articleTransformService The articleXmlUtils to set.
   */
  @Required
  public void setArticleTransformService(XMLService articleTransformService) {
    this.articleTransformService = articleTransformService;
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
      return articleService.getArticleIds(startDate, endDate, null, null, state, true, 0);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Get a list of ids of all articles that match the given criteria.
   *
   * @param startDate startDate
   * @param endDate   endDate
   * @param state     array of matching state values
   * @param ascending controls the sort order (by date).
   * @return list of article uri's
   * @throws ApplicationException
   */
  public List<String> getArticleIds(String startDate, String endDate, int[] state, boolean ascending)
    throws ApplicationException {
    try {
      return articleService.getArticleIds(startDate, endDate, null, null, state, ascending, 0);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  private class Invalidator extends AbstractObjectListener {
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
