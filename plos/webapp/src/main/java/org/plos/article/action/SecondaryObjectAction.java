/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.action;

import java.util.ArrayList;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.action.BaseActionSupport;
import org.plos.article.service.ArticleOtmService;
import org.plos.article.service.SecondaryObject;
import org.plos.util.ArticleXMLUtils;


/**
 * Fetch the secondary objects for a given uri
 */
public class SecondaryObjectAction extends BaseActionSupport {
  private String uri;
  private SecondaryObject[] secondaryObjects;
  private ArticleOtmService articleOtmService;
  private ArticleXMLUtils secondaryObjectService;
  private static final Log log = LogFactory.getLog(SecondaryObjectAction.class);

  @Override
  public String execute() throws Exception {
    try {
      secondaryObjects = articleOtmService.listSecondaryObjects(uri);
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
      secondaryObjects = articleOtmService.listFiguresTables(uri);
      ArrayList<SecondaryObject> figTables = new ArrayList<SecondaryObject>(secondaryObjects.length);
      String allTransformed;
      String[] elems;
      StringBuilder desc;
      String doi;

      for (SecondaryObject s: secondaryObjects) {
        figTables.add(s);
        try {
          allTransformed = secondaryObjectService.getTranformedDocument(s.getDescription());
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
   * @param articleOtmService articleOtmService
   */
  public void setArticleOtmService(final ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  /**
   * @param secondaryObjectUtil The secondaryObjectUtil to set.
   */
  public void setSecondaryObjectService(ArticleXMLUtils secondaryObjectService) {
    this.secondaryObjectService = secondaryObjectService;
  }
}
