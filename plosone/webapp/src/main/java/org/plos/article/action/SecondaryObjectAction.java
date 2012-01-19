/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.action;

import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.article.service.ArticleWebService;
import org.plos.article.service.FetchArticleService;
import org.plos.article.service.SecondaryObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
/**
 * Fetch the secondary objects for a given uri
 */
public class SecondaryObjectAction extends BaseActionSupport {
  private String uri;
  private SecondaryObject[] secondaryObjects;
  private ArticleWebService articleWebService;
  private FetchArticleService fetchArticleService;
  private DocumentBuilderFactory factory;
  private Map<String, String> xmlFactoryProperty;
  private static final Log log = LogFactory.getLog(SecondaryObjectAction.class);

  private static final String FIGURE_CONTEXT = "fig";
  private static final String TABLE_CONTEXT = "table-wrap";
  
  public String execute() throws Exception {
    try {
      secondaryObjects = articleWebService.listSecondaryObjects(uri);
    } catch (Exception ex) {
      log.error ("Could not get secondary objects for: " + uri, ex);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Action to return list of Secondary object for an article that are enclosed in Tables (table-warp)
   * and Figures (fig) tags.   
   * 
   * @return webork status string
   * @throws Exception
   */
  
  public String listFiguresAndTables() throws Exception {
    try {
      secondaryObjects = articleWebService.listSecondaryObjects(uri);
      ArrayList<SecondaryObject> figTables = new ArrayList<SecondaryObject>(secondaryObjects.length);
      String contextElem;
      String allTransformed;
      String[] elems;
      StringBuilder desc;
      String doi;
      
      for (SecondaryObject s: secondaryObjects) {
        contextElem = s.getContextElement();
        if (FIGURE_CONTEXT.equals(contextElem) || TABLE_CONTEXT.equals(contextElem)) {
          figTables.add(s);
          try {
            allTransformed = fetchArticleService.getTranformedSecondaryObjectDescription(s.getDescription());
            if (log.isDebugEnabled()){
              log.debug("Transformed figure captions for article: " + uri);
              log.debug(allTransformed);              
            }
            elems = allTransformed.split("END_TITLE");
            desc = new StringBuilder();
            doi = s.getDoi();
            if (elems.length > 1) {
              s.setTransformedCaptionTitle(elems[0]);
              s.setPlainCaptionTitle(elems[0].replaceAll("<.*>",""));
              desc.append(elems[1]);
              if ((doi != null) && (doi.length() > 0)) {
                desc.append("doi:").append(doi);
              }
              s.setTransformedDescription(desc.toString());
            } else if (elems.length == 1) {
              desc.append(elems[0]);
              if ((doi != null) && (doi.length() > 0)) {
                desc.append("doi:").append(doi);
              }
              s.setTransformedDescription(desc.toString());              
            }
          } catch (Exception e) {
            log.warn("Could not transform description for Object: " + getUri(), e);
            s.setTransformedDescription(s.getDescription());
          }
        }
      }
      secondaryObjects = figTables.toArray(new SecondaryObject[figTables.size()]);
    } catch (Exception ex) {
      log.warn("Couldn't retrieve secondary object for URI: " + uri, ex);
      return ERROR;
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
  public SecondaryObject[] getSecondaryObjects() {
    return secondaryObjects;
  }

  /**
   * Set the secondary objects
   * @param articleWebService articleWebService
   */
  public void setArticleWebService(final ArticleWebService articleWebService) {
    this.articleWebService = articleWebService;
  }

  /**
   * @param xmlFactoryProperty The xmlFactoryProperty to set.
   */
  public void setXmlFactoryProperty(Map<String, String> xmlFactoryProperty) {
    this.xmlFactoryProperty = xmlFactoryProperty;
  }

  /**
   * @param fetchArticleService The fetchArticleService to set.
   */
  public void setFetchArticleService(FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }
}
