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

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import com.thoughtworks.xstream.XStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.action.BaseActionSupport;
import org.plos.article.service.CitationInfo;
import org.plos.util.ArticleXMLUtils;
import org.plos.util.FileUtils;


/**
 * Action to create a citation.  Does not care what the output format is.
 * 
 * @author Stephen Cheng
 *
 */
public class CreateCitation extends BaseActionSupport {

  // private ArticleOtmService articleOtmService;
  private String articleURI;
  private ArticleXMLUtils citationService;
  private CitationInfo citation;
  private GeneralCacheAdministrator articleCacheAdministrator;

  private static final String CACHE_KEY_CITATION_INFO = "CITATION_INFO";
  private static final Log log = LogFactory.getLog(CreateCitation.class);


  /**
   * Generate citation information by first attempting to get from cache.  If not present,
   * will create a CitationInfo object.
   */
  public String execute () {
    try {
      citation = (CitationInfo)articleCacheAdministrator.getFromCache(articleURI + CACHE_KEY_CITATION_INFO);
      if (log.isDebugEnabled()) {
        log.debug("retrieved CitationInfo from cache for: " + articleURI);
      }
    } catch (NeedsRefreshException nre) {
      boolean updated = false;
      try {
        XStream xstream = new XStream();
        citation = (CitationInfo)xstream.fromXML(citationService.getTransformedArticle(articleURI));
        String escapedURI = FileUtils.escapeURIAsPath(articleURI);
        articleCacheAdministrator.putInCache(articleURI + CACHE_KEY_CITATION_INFO, citation,
                                             new String[]{escapedURI});
        updated = true;
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error ("Could not transform article: " + articleURI, e);
        }
        return ERROR;
      } finally {
        if (!updated)
          articleCacheAdministrator.cancelUpdate(articleURI + CACHE_KEY_CITATION_INFO);
      }
    }

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
   * @param articleCacheAdministrator The articleCacheAdministrator to set.
   */
  public void setArticleCacheAdministrator(GeneralCacheAdministrator articleCacheAdministrator) {
    this.articleCacheAdministrator = articleCacheAdministrator;
  }
}
