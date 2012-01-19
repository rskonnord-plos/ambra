/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc. http://topazproject.org
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

package org.topazproject.ambra.admin.action;

import org.topazproject.ambra.admin.service.SyndicationService;
import org.topazproject.ambra.admin.service.SyndicationService.SyndicationDTO;
import org.topazproject.ambra.models.Syndication;
import org.topazproject.otm.RdfUtil;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Display all of the Syndication objects associated to one Article and allow the user to
 * <ul>
 *   <li>Syndicate the article to syndication targets, if the article has not been previously submitted to those targets</li>
 *   <li>Resyndicate the article to targets, if previous syndication attempts had failed</li>
 *   <li>Mark "in progress" syndications as "failed"</li>
 * </ul>
 */
public class ArticleSyndicationHistory extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(ManageFlagsAction.class);
    
  private List<SyndicationDTO> synHistory;
  private SyndicationService syndicationService;
  private URI articleURI;
  private String[] synTargets;

  /**
   * Default Struts action, populate to be used in the freemarker templates
   * @return SUCCESS
   */
  public String execute() {
    try {
      setCommonFields();
    } catch (Exception e) {
      String err = "Failed to find history for DOI:";
      if (articleURI != null)
        err = err +  articleURI.toString() + ".<br/>";
        log.error(err, e);
        addActionError(err + e);
    }
    return this.hasActionErrors()?ERROR:SUCCESS;
  }

  /**
   * ReSyndicate the selected targets for the current article
   * @return ERROR or SUCCESS
   */
  public String resyndicate() {
    try {
      if (articleURI != null) {
        if ((synTargets != null) &&( synTargets.length > 0)) {
          for(String target : synTargets) {

            Syndication syndication = syndicationService.syndicate(articleURI.toString(), target);

            if (Syndication.STATUS_FAILURE.equals(syndication.getStatus())) {
              addActionError("Syndication failed for DOI: " +  articleURI.toString() + "<br/>"
                  + syndication.getErrorMessage());
            }
          }
        } else {
          addActionError("No targets selected for syndication for DOI: " + articleURI.toString());
        }
        synHistory = getSyndications(articleURI); // TODO: take out this line; setCommonFields() already populates this field.
      } else {
        addActionError("Invalid or unspecified Article DOI.");
      }
    } catch (URISyntaxException e) {
      log.error("Received invalid doi of:" + articleURI, e);
      addActionError("Received invalid doi of:" + articleURI);
    }

    // Set the common fields after processing so that the new Syndication status is shown.
    try {
      setCommonFields();
    } catch (Exception e) {
      String err = "Failed to find history for DOI: ";
      if (articleURI != null)
        err = err +  articleURI.toString() + "<br/>";
        log.error(err, e);
        addActionError(err + e);
    }
    return this.hasActionErrors() ? ERROR : SUCCESS;
  }

  /**
   * Update syndication statuses to FAILURE for the selected targets of the current article
   * @return ERROR or SUCCESS
   */
  public String markSyndicationAsFailed() {
    if (articleURI != null) {
      try {
        if ((synTargets != null) && (synTargets.length > 0)) {
          for (String target : synTargets) {
            syndicationService.updateSyndication(
                articleURI.toString(), target, Syndication.STATUS_FAILURE,
                "Status manually changed to " + Syndication.STATUS_FAILURE);
          }
        } else {
          addActionError("Could not mark Syndication as failed: No targets selected for DOI: "
              + articleURI.toString());
        }
        synHistory = getSyndications(articleURI); // TODO: take out this line; setCommonFields() already populates this field.
      } catch (URISyntaxException e) {
        log.error("Received invalid doi of:" + articleURI, e);
        addActionError("Received invalid doi of:" + articleURI);
      }
    } else {
      addActionError("Could not mark Syndication as failed: Invalid or unspecified Article DOI.");
    }

    // Set the common fields after processing so that the new Syndication status is shown.
    try {
      setCommonFields();
    } catch (Exception e) {
      String err = "Failed to find history for DOI: ";
      if (articleURI != null)
        err = err +  articleURI.toString() + "<br/>";
        log.error(err, e);
        addActionError(err + e);
    }
    return this.hasActionErrors()?ERROR:SUCCESS;
  }

  /**
   * TODO: Check if the article exists (i.e., has not been deleted).
   * TODO:   If not, then do not show the "Resyndicate" button(s) on the screen.
   */
  private void setCommonFields() {
    // create a faux journal object for template
    initJournal();

    // Set the list of all Syndications for this Article.
    if (articleURI != null) {
      try {
        synHistory = getSyndications(articleURI);
      } catch (URISyntaxException e) {
        addActionError("Could not set common field because unable to " +
            "create a URI from articleId = " + articleURI.toString());
      }
      if (synHistory.size() == 0)
        addActionMessage("No syndications where found");
    } else {
      addActionError("Invalid or unspecified Article DOI.");
    }
  }

  private List<SyndicationDTO> getSyndications(URI aURI) throws URISyntaxException {
    return syndicationService.querySyndication(aURI.toString());
  }

  /**
   * Get all of the Syndications associated to the articleURI.
   * @return All of the Syndications associated to the articleURI
   */
  public List<SyndicationDTO> getSyndicationHistory() {
    return synHistory;
  }

  /**
   * Get a list of all syndications that are not in an "in progress" state.
   * These are the Syndications that can be resent through this page.
   *
   * @return a list of Syndications
   */
  public List<SyndicationDTO> getFinishedSyndications() {
    List<SyndicationDTO> finishedSyndications = new ArrayList<SyndicationDTO>();

    for(SyndicationDTO syn : getSyndicationHistory())
      if(syn.complete() || syn.isPending())
        finishedSyndications.add(syn);

    return finishedSyndications;
  }

  /**
   * Sets service used to syndicate these articles to external organizations
   *
   * @param  syndicationService The service used to syndicate these articles to
   *   external organizations
   */
  @Required
  public void setSyndicationService(SyndicationService syndicationService) {
    this.syndicationService = syndicationService;
  }

  /**
   * Set the current article URI
   * @param articleURI 
   */
  public void setArticle(String articleURI) {
    try {
      this.articleURI = RdfUtil.validateUri(articleURI.trim(), "Aricle Uri");
    } catch (Exception e) {
      this.articleURI = null;
      if (log.isDebugEnabled())
        log.debug("setArticle URI conversion failed.");
    }
  }

  /**
   * Get the current article ID
   * @return article id 
   */
  public String getArticle() {
    return articleURI.toString();
  }

  /**
   * Set the targets to resyndicate
   * @param targets a list of syndication targets
   */
  public void setTarget(String[] targets) {
    synTargets = targets;
  }
}
