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
import java.util.List;
import java.util.Set;

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

  private Set<Journal> journals;
  private String journalToModify;
  private String articlesToAdd;
  private String[] articlesToDelete;
  private Session session;
  private JournalService journalService;
  private BrowseService browseService;

  private static final Log log = LogFactory.getLog(ManageVirtualJournalsAction.class);

  /**
   * Manage Journals.  Display Journals and processes all add/deletes.
   */
  public String execute() throws Exception  {

    if (log.isDebugEnabled()) {
      log.debug("journalToModify: " + journalToModify + ", articlesToAdd: " + articlesToAdd
        + ", articlesToDelete: " + articlesToDelete);
    }

    // JournalService wants to be in a Transaction
    TransactionHelper.doInTx(session,
      new TransactionHelper.Action<Void>() {

        public Void run(Transaction tx) {

          // process any pending adds, deletes
          if (journalToModify != null
            && ((articlesToAdd != null && articlesToAdd.length() != 0)
              || articlesToDelete != null)) {
            // get the Journal
            Journal journal = journalService.getJournal(journalToModify);
            if (journal == null) {
              final String errorMessage = "Error getting journal to modify: " + journalToModify;
              addActionMessage(errorMessage);
              log.error(errorMessage);
              return null;
            }

            // current Journal Articles
            List<URI> articles = journal.getSimpleCollection();

            // process adds
            if (articlesToAdd != null && articlesToAdd.length() != 0) {
              for (final String articleToAdd : articlesToAdd.split(",")) {
                  articles.add(URI.create(articleToAdd));
                  addActionMessage("Added: " + articleToAdd);
              }
            }

            // process deletes
            if (articlesToDelete != null) {
              for (final String articleToDelete : articlesToDelete) {
                  articles.remove(URI.create(articleToDelete));
                  addActionMessage("Deleted: " + articleToDelete);
              }
            }

            // new Journal Articles
            journal.setSimpleCollection(articles);

            // Journal was updated
            session.saveOrUpdate(journal);
            journalService.journalWasModified(journal);
            browseService.flush(journal.getKey());
            addActionMessage("Browse cache flush for: " + journal.getKey());
          }

          // get all Journals
          journals = journalService.getAllJournals();

          if (log.isDebugEnabled()) {
            for (final Journal journal : journals) {
              log.debug("execute(): Journal: key:" + journal.getKey() + ", eIssn:" + journal.getEIssn()
                + ", smartCollectionRules:" + journal.getSmartCollectionRules().toString()
                + ", simpleCollection:" + journal.getSimpleCollection().toString());
            }
          }

          return null;
        }
      });

    // default action is just to display the template
    return SUCCESS;
  }

  /**
   * Gets all virtual Journals.
   *
   * @return All virtual Journals.
   */
  public Set<Journal> getJournals() {
    if (log.isDebugEnabled()) {
      for (final Journal journal : journals) {
        log.debug("getJournals(): Journal: key:" + journal.getKey() + ", eIssn:" + journal.getEIssn()
          + ", smartCollectionRules:" + journal.getSmartCollectionRules().toString()
          + ", simpleCollection:" + journal.getSimpleCollection().toString());
      }
    }

    return journals;
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
