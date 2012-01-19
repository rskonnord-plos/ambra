/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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

package org.plos.admin.action;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.journal.JournalService;
import org.plos.models.Journal;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.otm.Session;
import org.topazproject.otm.criterion.DetachedCriteria;

/**
 * Allow Admin to Manage virtual Journals.
 */
@SuppressWarnings("serial")
public class ManageVirtualJournalsAction extends BaseAdminActionSupport {

  /**
   * JournalInfo - Value object holding Journal properties that require referencing in the view.
   * @author jkirton
   */
  public static final class JournalInfo {
    private String key, eissn;
    private String smartCollectionRulesDescriptor;
    private String image, currentIssue;
    private String volumes;
    private List<String> simpleCollection;

    public String getKey() {
      return key;
    }
    public void setKey(String key) {
      this.key = key;
    }
    public String getEissn() {
      return eissn;
    }
    public void setEissn(String eissn) {
      this.eissn = eissn;
    }
    public String getSmartCollectionRulesDescriptor() {
      return smartCollectionRulesDescriptor;
    }
    public void setSmartCollectionRulesDescriptor(String smartCollectionRulesDescriptor) {
      this.smartCollectionRulesDescriptor = smartCollectionRulesDescriptor;
    }
    public String getImage() {
      return image;
    }
    public void setImage(String image) {
      this.image = image;
    }
    public String getCurrentIssue() {
      return currentIssue;
    }
    public void setCurrentIssue(String currentIssue) {
      this.currentIssue = currentIssue;
    }
    public String getVolumes() {
      return volumes;
    }
    public void setVolumes(String volumes) {
      this.volumes = volumes;
    }
    public List<String> getSimpleCollection() {
      return simpleCollection;
    }
    public void setSimpleCollection(List<String> simpleCollection) {
      this.simpleCollection = simpleCollection;
    }

    @Override
    public String toString() {
      return key;
    }
  }

  private JournalInfo journalInfo;
  private String journalToModify;
  private URI image;
  private URI currentIssue;
  private String volumes;
  private String articlesToAdd;
  private String[] articlesToDelete;
  private JournalService journalService;

  private static final Log log = LogFactory.getLog(ManageVirtualJournalsAction.class);

  /**
   * Manage Journals.  Display Journals and processes all add/deletes.
   */
  @Override
  @Transactional(rollbackFor = { Throwable.class })
  public String execute() throws Exception  {
    if (log.isDebugEnabled()) {
      log.debug("journalToModify: " + journalToModify + ", articlesToAdd: " + articlesToAdd
        + ", articlesToDelete: " + articlesToDelete);
    }

    // process any pending modifications, adds, deletes
    if (journalToModify != null
      && (image != null
        || currentIssue != null
        || (articlesToAdd != null && articlesToAdd.length() != 0)
        || articlesToDelete != null)) {
      // get the Journal
      Journal journal = journalService.getJournal();
      if (journal == null) {
        final String errorMessage = "Error getting journal to modify: " + journalToModify;
        addActionMessage(errorMessage);
        log.error(errorMessage);
        return null;
      }

      // current Journal Volumes/Articles
      List<URI> volumeDois = journal.getVolumes();
      List<URI> articles = journal.getSimpleCollection();

      // process modifications
      if (image != null) {
        if (image.toString().length() == 0) {
          image = null;
        }
        journal.setImage(image);
        addActionMessage("Image set to: " + image);
      }

      if (currentIssue != null) {
        if (currentIssue.toString().length() == 0) {
          currentIssue = null;
        }
        journal.setCurrentIssue(currentIssue);
        addActionMessage("Current Issue set to: " + currentIssue);
      }

      // process Volumes
      volumeDois.clear();
      if (volumes != null && volumes.length() != 0) {
        for (final String volume : volumes.split("[,\\s]+")) {
          URI volumeDoi = getURI(volume);
          if (volumeDoi != null) {
            volumeDois.add(volumeDoi);
          }
        }
      }
      journal.setVolumes(volumeDois);

      // process adds
      if (articlesToAdd != null && articlesToAdd.length() != 0) {
        for (final String articleToAdd : articlesToAdd.split("[,\\s]+")) {
          URI art = getURI(articleToAdd);
          if (art != null) {
            articles.add(art);
            addActionMessage("Added: " + articleToAdd);
          }
        }
      }

      // process deletes
      if (articlesToDelete != null) {
        for (final String articleToDelete : articlesToDelete) {
          URI art = getURI(articleToDelete);
          if (art != null) {
            articles.remove(art);
            addActionMessage("Deleted: " + articleToDelete);
          }
        }
      }

      // new Journal Articles
      journal.setSimpleCollection(articles);

    }

    // [re-]create the journal info value object
    createJournalInfo(journalService.getJournal());

    // default action is just to display the template
    return SUCCESS;
  }

  private void createJournalInfo(Journal journal) {
    assert journal != null;

    journalInfo = new JournalInfo();

    journalInfo.setKey(journal.getKey());
    journalInfo.setEissn(journal.getEIssn());
    journalInfo.setCurrentIssue(journal.getCurrentIssue() == null ? null : journal.getCurrentIssue().toString());
    journalInfo.setImage(journal.getImage() == null ? null : journal.getImage().toString());

    List<URI> jscs = journal.getSimpleCollection();
    if(jscs != null) {
      List<String> slist = new ArrayList<String>(jscs.size());
      for(URI uri : jscs) {
        slist.add(uri.toString());
      }
      journalInfo.setSimpleCollection(slist);
    }

    final List<DetachedCriteria> dclist = journal.getSmartCollectionRules();
    if(dclist != null && dclist.size() > 0) {
      StringBuffer sb = new StringBuffer();
      for(DetachedCriteria dc : journal.getSmartCollectionRules()) {
        sb.append(", ");
        sb.append(dc.toString());
      }
      journalInfo.setSmartCollectionRulesDescriptor(sb.substring(2));
    }

    final List<URI> volumes = journal.getVolumes();
    if(volumes != null && volumes.size() > 0) {
      StringBuffer sb = new StringBuffer();
      for(URI v : volumes) {
        sb.append(", ");
        sb.append(v.toString());
      }
      journalInfo.setVolumes(sb.substring(2));
    }

    if (log.isDebugEnabled()) log.debug("Journal info assembled");
  }

  private URI getURI(String uriStr) {
    try {
      URI uri = new URI(uriStr);
      if (uri.isAbsolute())
        return uri;

      addActionMessage("Not an absolute URI: '" + uriStr + "'");
    } catch (URISyntaxException use) {
      addActionMessage("Not a valid URI (" + use.getMessage() + "): '" + uriStr + "'");
    }

    return null;
  }

  /**
   * Gets the JournalInfo value object for access in the view.
   *
   * @return Current virtual Journal value object.
   */
  public JournalInfo getJournal() {

    return journalInfo;
  }

  /**
   * Set Journal to modify.
   *
   * @param journalToModify Journal to modify.
   */
  public void setJournalToModify(String journalToModify) {
    this.journalToModify = journalToModify;
  }

  /**
   * Get current issue.
   *
   * @return current issue.
   */
  public String getCurrentIssue() {
    return currentIssue.toString();
  }

  /**
   * Set current issue.
   *
   * @param currentIssue the current issue for this journal.
   */
  public void setCurrentIssue(String currentIssue) {
    this.currentIssue = URI.create(currentIssue);
  }

  /**
   * Set Volumes.
   *
   * @param volumes a comma separated list of volumes.
   */
  public void setVolumes(String volumes) {
    this.volumes = volumes;
  }

  /**
   * Get image.
   *
   * @return image.
   */
  public String getImage() {
    return image.toString();
  }

  /**
   * Set image.
   *
   * @param image the image for this journal.
   */
  public void setImage(String image) {
    this.image = URI.create(image);
  }

  /**
   * Set Articles to delete.
   *
   * @param articlesToDelete Array of articles to delete.
   */
  public void setArticlesToDelete(String[] articlesToDelete) {
    this.articlesToDelete = articlesToDelete;
  }

  /**
   * Set Articles to add.
   *
   * @param articlesToAdd a comma separated list of articles to add.
   */
  public void setArticlesToAdd(String articlesToAdd) {
    this.articlesToAdd = articlesToAdd;
  }

  /**
   * Sets the JournalService.
   *
   * @param journalService The JournalService to set.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

}
