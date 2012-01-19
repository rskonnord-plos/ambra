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

package org.topazproject.ambra.article.action;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.article.service.BrowseService;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.model.IssueInfo;
import org.topazproject.ambra.model.VolumeInfo;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.util.ArticleXMLUtils;

import org.topazproject.otm.Session;

public class BrowseVolumeAction extends BaseActionSupport {
  private static final Log log = LogFactory.getLog(BrowseArticlesAction.class);
  private BrowseService browseService;
  private JournalService journalService;
  private IssueInfo currentIssue;
  private int currentIssueNumber;
  private VolumeInfo currentVolume;
  private List<VolumeInfo> volumeInfos;
  private String gotoVolume;
  private String currentIssueDescription;
  private ArticleXMLUtils articleXmlUtils;

  @Override
  @Transactional(readOnly = true)
  public String execute() throws Exception {
    Journal currentJournal = journalService.getCurrentJournal();

    volumeInfos = browseService.getVolumeInfosForJournal(currentJournal);
    if (volumeInfos.size() > 0)
      currentVolume = volumeInfos.get(0);

    currentIssue = browseService.getIssueInfo(currentJournal.getCurrentIssue());
    if (currentIssue != null) {
      // Figure out what issue number the currentIssue is in its volume
      for (VolumeInfo vol : volumeInfos) {
        int issueNum = 1;
        for (IssueInfo issue : vol.getIssueInfos()) {
          if (issue.getId().equals(currentIssue.getId())) {
            currentIssueNumber = issueNum;
            break;
          }
          issueNum++;
        }
      }

      // Translate the currentIssue description to HTML
      if (currentIssue.getDescription() != null) {
        try {
          currentIssueDescription =
            articleXmlUtils.transformArticleDescriptionToHtml(currentIssue.getDescription());
        } catch (ApplicationException e) {
          log.error("Failed to translate issue description to HTML.", e);
          // Just use the untranslated issue description
          currentIssueDescription = currentIssue.getDescription();
        }
      } else {
        log.error("The currentIssue description was null. Issue DOI='"+currentIssue.getId()+"'");
        currentIssueDescription = "No description found for this issue";
      }
    }

    return SUCCESS;
  }

  /**
   * The sequence number of the current issue. This is calculated in the execute
   * method and displayed in the view.
   * 
   * @return the current issue number
   */
  public int getCurrentIssueNumber() {
    return currentIssueNumber;
  }

  /**
   * The current issue as defined for the Journal.
   * 
   * @return the current issue
   */
  public IssueInfo getCurrentIssue() {
    return currentIssue;
  }

  public String getJournalName() {
    // TODO: Need to figure this out dynamically...
    return "PLoS Neglected Tropical Diseases";
  }

  /**
   * Called by Spring injection when this class is loaded...
   * 
   * @param browseService
   *          The browseService to set.
   */
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }

  /**
   * Called by Spring injection when this class is loaded...
   * 
   * @param journalService
   *          The JournalService to set.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * Returns the last linked volume for this journal - which is assumed to be
   * the current volume.
   * 
   * @return the current volume for this journal
   */
  public VolumeInfo getCurrentVolume() {
    return currentVolume;
  }

  /**
   * @return the VolumeInfos.
   */
  public List<VolumeInfo> getVolumeInfos() {
    return volumeInfos;
  }

  public String getGotoVolume() {
    return gotoVolume;
  }

  public void setGotoVolume(String gotoVolume) {
    this.gotoVolume = gotoVolume;
  }

  /**
   * Spring injected
   * 
   * @param axu
   */
  @Required
  public void setArticleXmlUtils(ArticleXMLUtils axu) {
    this.articleXmlUtils = axu;
  }

  public String getCurrentIssueDescription() {
    return currentIssueDescription;
  }
}
