/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.action;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.article.service.BrowseService;
import org.plos.journal.JournalService;
import org.plos.models.Journal;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.util.TransactionHelper;

/**
 * Allow Admin to Manage virtual Journals.
 */
public class ManageVirtualJournalsAction extends BaseAdminActionSupport {

  private Journal journal;
  private String journalToModify;
  private URI image;
  private URI currentIssue;
  private String volumes;
  private String articlesToAdd;
  private String[] articlesToDelete;
  private Session session;
  private JournalService journalService;
  private BrowseService browseService;

  private static final Log log = LogFactory.getLog(ManageVirtualJournalsAction.class);

  /**
   * Manage Journals.  Display Journals and processes all add/deletes.
   */
  @Override
  public String execute() throws Exception  {

    if (log.isDebugEnabled()) {
      log.debug("journalToModify: " + journalToModify + ", articlesToAdd: " + articlesToAdd
        + ", articlesToDelete: " + articlesToDelete);
    }

    // JournalService wants to be in a Transaction
    TransactionHelper.doInTx(session,
      new TransactionHelper.Action<Void>() {

        public Void run(Transaction tx) {

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

            // Journal was updated
            session.saveOrUpdate(journal);
            journalService.journalWasModified(journal);
            browseService.notifyJournalModified(journal.getKey());
            addActionMessage("Browse cache flush for: " + journal.getKey());
          }

          // get current Journal
          journal = journalService.getJournal();

          if (log.isDebugEnabled()) {
              log.debug("execute(): Journal: " + journal);
          }

          return null;
        }
      });

    // default action is just to display the template
    return SUCCESS;
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
   * Gets current virtual Journal.
   *
   * @return Current virtual Journal.
   */
  public Journal getJournal() {

    return journal;
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
   * @param Comma separated list of volumes.
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
   * @param Comma separated list of articles to add.
   */
  public void setArticlesToAdd(String articlesToAdd) {
    this.articlesToAdd = articlesToAdd;
  }

  /**
   * Set the OTM Session.
   *
   * @param session The OTM Session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
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

  /**
   * @param browseService The browseService to set.
   */
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }
}
