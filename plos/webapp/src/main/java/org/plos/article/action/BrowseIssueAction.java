/* $HeadURL:: $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.action;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.action.BaseActionSupport;
import org.plos.article.service.BrowseService;
import org.plos.journal.JournalService;
import org.plos.model.IssueInfo;
import org.plos.model.VolumeInfo;
import org.plos.model.article.ArticleInfo;
import org.plos.model.article.ArticleType;
import org.plos.models.Journal;
import org.plos.util.ArticleXMLUtils;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.otm.Session;

/**
 * BrowseIssueAction retrieves data for presentation of an issue and a table of contents. Articles 
 * contained in the issue are grouped into article types. 
 * 
 * @author Alex Worden
 *
 */
public class BrowseIssueAction extends BaseActionSupport{
  private static final Log log  = LogFactory.getLog(BrowseIssueAction.class);

  private String issue;
  private Session session;
  private JournalService journalService;
  private BrowseService browseService;
  private IssueInfo issueInfo;
  private String issueDescription;
  private ArrayList<TOCArticleGroup> articleGroups = new ArrayList<TOCArticleGroup>();

  private ArticleXMLUtils articleXmlUtils;
  
  @Override
  public String execute() {

    // was issued specified, or use Journal.currentIssue?
    if (issue == null || issue.length() == 0) {
      // JournalService, OTM usage wants to be in a Transaction
      Journal currentJournal = journalService.getCurrentJournal(session);
      
      if (currentJournal != null) {
        URI currentIssueUri = currentJournal.getCurrentIssue();
        if (currentIssueUri != null) {
          issue = currentIssueUri.toString();
        } else {
          /* Current Issue has not been set for the Journal - It should have
           * been configured via the admin console. Try to find the latest issue
           * from the latest volume. If no issue exists in the latest volume - 
           * look at the previous volume and so on.
           */ 
          List<VolumeInfo> vols = browseService.getVolumeInfos(currentJournal.getVolumes());
          if (vols.size() > 0) {
            Collections.reverse(vols);
            for (VolumeInfo volInfo : vols) {
              IssueInfo latestIssue = null;
              List<IssueInfo> issuesInVol = volInfo.getIssueInfos();
              if (issuesInVol.size() > 0) {
                latestIssue = issuesInVol.get(issuesInVol.size()-1);
              }
              if (latestIssue != null) {
                issue = latestIssue.getId().toString();
                break;
              }
            }
          }
        }
      }
    }

    // if still no issue, report an error and return ERROR status
    if (issue == null || issue.length() == 0) {
      // issueInfo = new IssueInfo(null,
      //     "No current Issue is defined for this Journal", null, null, null,
      //     null);
      log.error("No issue was specified for BrowseIssue action and no current issue could be derived.");
      return ERROR;
    }

    // look up Issue
    issueInfo = browseService.getIssueInfo(URI.create(issue));
    if (issueInfo == null) {
      log.error("Failed to retrieve IssueInfo for issue id='"+issue+"'");
      return ERROR; 
    }

    // Translate the currentIssue description to HTML
    if (issueInfo.getDescription() != null) {
      try {
        issueDescription = articleXmlUtils.transformArticleDescriptionToHtml(issueInfo.getDescription());
      } catch (ApplicationException e) {
        log.error("Failed to translate issue description to HTML.", e);
        issueDescription = issueInfo.getDescription(); // Just use the untranslated issue description
      }
    } else {
      log.error("The currentIssue description was null. Issue DOI='"+issueInfo.getId()+"'");
      issueDescription = "No description found for this issue";
    }
    
    // clear out the articleGroups and rebuild the list with one TOCArticleGroup
    // for each ArticleType to be displayed in the order defined by
    // ArticleType.getOrderedListForDisplay()
    articleGroups = new ArrayList<TOCArticleGroup>();
    TOCArticleGroup defaultArticleGroup = null;
    for (ArticleType at : ArticleType.getOrderedListForDisplay()) {
      TOCArticleGroup newArticleGroup = new TOCArticleGroup(at);
      articleGroups.add(newArticleGroup);
      if (at == ArticleType.getDefaultArticleType()) {
        defaultArticleGroup = newArticleGroup;
      }
    }

    // For every article that is of the same ArticleType as a
    // TOCArticleGroup, add it to that group. Articles can appear in
    // multiple TOCArticleGroups.
    for (ArticleInfo ai : issueInfo.getArticlesInIssue()) {
      boolean articleAddedToAtLeastOneGroup = false;
      for (TOCArticleGroup ag : articleGroups) {
        for (ArticleType articleType : ai.getArticleTypes()) {
          if (ag.getArticleType().equals(articleType)) {
            ag.addArticle(ai);
            articleAddedToAtLeastOneGroup = true;
            break;
          }
        }
      }
      
      if (log.isErrorEnabled() && !articleAddedToAtLeastOneGroup) {
        StringBuffer buf = new StringBuffer("| ");
        Iterator<ArticleType> it = ai.getArticleTypes().iterator();
        while (it.hasNext()) {
          ArticleType at = it.next();
          buf.append(at.getUri());
          if (it.hasNext()) { buf.append(" | "); }
        }
        buf.append(" |");
        log.error("Failed to add article '"+ ai.getId()
                + "' to an article group. Check configured articles types (plosone.xml) "
                + "against the article types found for this Article: " + buf);
      }
    }
    
    // Remove all empty TOCArticleGroups (avoid ConcurrentModificationException by 
    // building a new ArrayList of non-empty article groups). 
    int i = 1;
    ArrayList<TOCArticleGroup> newArticleGroups = new ArrayList<TOCArticleGroup>();
    for (TOCArticleGroup grp : articleGroups) {
      if (grp.articles.size() != 0) {
        newArticleGroups.add(grp);
        grp.setId("tocGrp_"+i);
        grp.sortArticles();
        i++;
      }
    }
    articleGroups = newArticleGroups;
    
    return SUCCESS;
  }


  /**
   * Used by the view to retrieve the IssueInfo from the struts value stack. 
   * @return the IssueInfo.
   */
  public IssueInfo getIssueInfo() {
    return issueInfo;
  }

  /**
   * Used by the view to retrieve an ordered list of TOCArticleGroup objects. Each TOCArticleGroup 
   * represents a collection of article types that are defined in defaults.xml. 
   * The groups are listed by the view in the order returned here with links to the articles in that group category. 
   * @return ordered list of TOCArticleGroup(s) 
   */
  public ArrayList<TOCArticleGroup> getArticleGroups() {
    return articleGroups;
  }
  
  /**
   * If the request parameter 'issue' is specified, stuts will call this method. The action will 
   * return a BrowseIssue page for this specific issue doi. 
   * @param issue The issue for ToC view.
   */
  public void setIssue(String issue) {
    this.issue = issue;
  }
  
  /**
   * Spring injected method sets the JournalService.
   *
   * @param journalService The JournalService to set.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * Spring injected method sets the OTM Session.
   *
   * @param session The OTM Session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }
  
  /**
   * Spring injected 
   * 
   * @param axu
   */
  @Required
  public void setArticleXmlUtils(ArticleXMLUtils axu) {
    articleXmlUtils = axu;
  }
  
  /**
   * Spring injected method sets the browseService. 
   * @param browseService The browseService to set.
   */
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }


  public String getIssueDescription() {
    return issueDescription;
  }
}
