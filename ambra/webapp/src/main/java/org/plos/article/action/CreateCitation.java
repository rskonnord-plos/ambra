/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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

package org.plos.article.action;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.action.BaseActionSupport;
import org.plos.article.service.CitationInfo;
import org.plos.article.service.FetchArticleService;
import org.plos.article.service.NoSuchArticleIdException;
import org.plos.cache.Cache;
import org.plos.cache.ObjectListener;
import org.plos.models.Article;
import org.plos.util.ArticleXMLUtils;
import org.plos.util.CitationUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Interceptor.Updates;
import org.topazproject.otm.Session;


/**
 * Action to create a citation.  Does not care what the output format is.
 *
 * @author Stephen Cheng
 *
 */
@SuppressWarnings("serial")
public class CreateCitation extends BaseActionSupport {

  public static final String CITATION_KEY = "ArticleAnnotationCache-Citation-";

  // private ArticleOtmService articleOtmService;
  private String articleURI;
  private ArticleXMLUtils citationService;
  private CitationInfo citation;
  private String citationString;
  private Cache articleAnnotationCache;

  private static final Log log = LogFactory.getLog(CreateCitation.class);
  private static Invalidator invalidator;

  /**
   * Generate citation information by first attempting to get from cache.  If not present,
   * will create a CitationInfo object.
   */
  @Override
  @Transactional(readOnly = true)
  public String execute () throws Exception {

    // lock @ Article level
    final Object lock = (FetchArticleService.ARTICLE_LOCK + articleURI).intern();

    citation = articleAnnotationCache.get(CITATION_KEY + articleURI, -1,
            new Cache.SynchronizedLookup<CitationInfo, ApplicationException>(lock) {
              public CitationInfo lookup() throws ApplicationException {
                XStream xstream = new XStream();
                try {
                  return (CitationInfo) xstream.fromXML(citationService.getTransformedArticle(articleURI));
                } catch (IOException ioe) {
                  throw new ApplicationException(ioe);
                } catch (NoSuchArticleIdException nsaie) {
                  throw new ApplicationException(nsaie);
                } catch (ParserConfigurationException pce) {
                  throw new ApplicationException(pce);
                } catch (SAXException se) {
                  throw new ApplicationException(se);
                }
              }
    });

    citationString = CitationUtils.generateArticleCitationString(citation);

    return SUCCESS;
  }

  /**
   * @param citationService The citationService to set.
   */
  public void setCitationService(ArticleXMLUtils citationService) {
    this.citationService = citationService;
  }

  /**
   * @return Returns the articleURI.
   */
  public String getArticleURI() {
    return articleURI;
  }

  /**
   * @param articleURI The articleURI to set.
   */
  public void setArticleURI(String articleURI) {
    this.articleURI = articleURI;
  }

  /**
   * @return Returns the citation.
   */
  public CitationInfo getCitation() {
    return citation;
  }

  /**
   * @return The formatted citation String.
   */
  public String getCitationString() {
    return citationString;
  }

  /**
   * @param articleAnnotationCache The Article(transformed)/ArticleInfo/Annotation/Citation cache
   *   to use.
   */
  @Required
  public void setArticleAnnotationCache(Cache articleAnnotationCache) {
    this.articleAnnotationCache = articleAnnotationCache;
    if (invalidator == null)
      invalidator = new Invalidator(articleAnnotationCache);
  }

  private static class Invalidator implements ObjectListener {
    private Cache articleAnnotationCache;

    public Invalidator(Cache articleAnnotationCache) {
      this.articleAnnotationCache = articleAnnotationCache;
      articleAnnotationCache.getCacheManager().registerListener(this);
    }

    public void objectChanged(Session session, ClassMetadata cm, String id, Object o,
        Updates updates) {
    }

    public void objectRemoved(Session session, ClassMetadata cm, String id, Object o) {
      if (o instanceof Article) {
        if (log.isDebugEnabled())
          log.debug("Invalidating citation for the article that was deleted.");
        articleAnnotationCache.remove(CITATION_KEY + id);
      }
    }
  }
}
