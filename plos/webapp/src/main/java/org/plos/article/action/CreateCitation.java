/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.article.action;

import com.thoughtworks.xstream.XStream;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.ehcache.Ehcache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.ApplicationException;
import org.plos.action.BaseActionSupport;
import org.plos.article.service.CitationInfo;
import org.plos.article.service.FetchArticleService;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.util.ArticleXMLUtils;
import org.plos.util.CacheAdminHelper;

import org.springframework.beans.factory.annotation.Required;

import org.xml.sax.SAXException;


/**
 * Action to create a citation.  Does not care what the output format is.
 *
 * @author Stephen Cheng
 *
 */
public class CreateCitation extends BaseActionSupport {

  public static final String CITATION_KEY = "ArticleAnnotationCache-Citation-";

  // private ArticleOtmService articleOtmService;
  private String articleURI;
  private ArticleXMLUtils citationService;
  private CitationInfo citation;
  private Ehcache articleAnnotationCache;

  private static final String CACHE_KEY_CITATION_INFO = "CITATION_INFO";
  private static final Log log = LogFactory.getLog(CreateCitation.class);


  /**
   * Generate citation information by first attempting to get from cache.  If not present,
   * will create a CitationInfo object.
   */
  public String execute () {

    // lock @ Article level
    final Object lock = (FetchArticleService.ARTICLE_LOCK + articleURI).intern();

    Object result = CacheAdminHelper.getFromCache(articleAnnotationCache, CITATION_KEY + articleURI,
                                         -1, lock,
                                         "citation",
                                         new CacheAdminHelper.EhcacheUpdater<Object>() {
        public Object lookup() {
          try {
            XStream xstream = new XStream();
            CitationInfo citationInfo = (CitationInfo) xstream.fromXML(
                                          citationService.getTransformedArticle(articleURI));
            return citationInfo;
          } catch (ApplicationException ae) {
            return ae;
          } catch (IOException ioe) {
            return ioe;
          } catch (NoSuchArticleIdException nsaie) {
            return nsaie;
          } catch (ParserConfigurationException pce) {
            return pce;
          } catch (SAXException se) {
            return se;
          }
        }
    });

    if (result instanceof Exception) {
      citation = null;
      if (log.isErrorEnabled()) { log.error(result); }
      addActionError(result.toString());
      return ERROR;
    }

    citation = (CitationInfo) result;
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
   * @param articleAnnotationCache The Article(transformed)/ArticleInfo/Annotation/Citation cache
   *   to use.
   */
  @Required
  public void setArticleAnnotationCache(Ehcache articleAnnotationCache) {
    this.articleAnnotationCache = articleAnnotationCache;
  }
}
