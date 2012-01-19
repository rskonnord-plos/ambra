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
import org.plos.util.CitationUtils;
import org.springframework.beans.factory.annotation.Required;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;


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
  private Ehcache articleAnnotationCache;

  private static final Log log = LogFactory.getLog(CreateCitation.class);


  /**
   * Generate citation information by first attempting to get from cache.  If not present,
   * will create a CitationInfo object.
   */
  @Override
  public String execute () throws Exception {

    // lock @ Article level
    final Object lock = (FetchArticleService.ARTICLE_LOCK + articleURI).intern();

    citation = CacheAdminHelper.getFromCacheE(articleAnnotationCache, CITATION_KEY + articleURI, -1,
            lock, "citation",
            new CacheAdminHelper.EhcacheUpdaterE<CitationInfo, ApplicationException>() {
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
  public void setArticleAnnotationCache(Ehcache articleAnnotationCache) {
    this.articleAnnotationCache = articleAnnotationCache;
  }
}
