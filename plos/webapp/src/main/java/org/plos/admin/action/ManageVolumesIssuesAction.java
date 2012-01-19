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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.journal.JournalService;
import org.plos.models.DublinCore;
import org.plos.models.Issue;
import org.plos.models.Journal;
import org.plos.models.Volume;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.util.TransactionHelper;

/**
 * Allow Admin to Manage Volumes/ Issues.
 *
 * @author Jeff Suttor
 */
public class ManageVolumesIssuesAction extends BaseAdminActionSupport {

  public static final String CREATE_VOLUME = "CREATE_VOLUME";
  public static final String UPDATE_VOLUME = "UPDATE_VOLUME";
  public static final String CREATE_ISSUE  = "CREATE_ISSUE";
  public static final String UPDATE_ISSUE  = "UPDATE_ISSUE";

  private static final String SEPARATORS = "[ ,;]";

  private String manageVolumesIssuesAction;
  private Journal journal;
  private List<Volume> volumes = new ArrayList();
  private List<Issue> issues = new ArrayList();
  private URI doi;
  private String displayName;
  private URI image;
  private String aggregation;
  private URI aggregationToDelete;

  private Session session;
  private JournalService journalService;

  private static final Log log = LogFactory.getLog(ManageVolumesIssuesAction.class);

  /**
   * Manage Volumes/Issues.  Display Volumes/Issues and processes all adds/modifications/deletes.
   */
  @Override
  public String execute() throws Exception  {

    if (manageVolumesIssuesAction != null) {
      if (manageVolumesIssuesAction.equals(CREATE_VOLUME)) {
        createVolume();
      } else if (manageVolumesIssuesAction.equals(UPDATE_VOLUME)) {
        updateVolume();
      } else if (manageVolumesIssuesAction.equals(CREATE_ISSUE)) {
        createIssue();
      } else if (manageVolumesIssuesAction.equals(UPDATE_ISSUE)) {
        updateIssue();
      }
    }

    // JournalService, OTM usage wants to be in a Transaction
    TransactionHelper.doInTx(session,
      new TransactionHelper.Action<Void>() {

        public Void run(Transaction tx) {

          // get the Journal
          journal = journalService.getJournal();
          if (journal == null) {
            final String errorMessage = "Error getting current Journal";
            addActionError(errorMessage);
            log.error(errorMessage);
            return null;
          }

          // get Volumes for this Journal
          volumes.clear();
          for (final URI volumeDoi : journal.getVolumes()) {
            final Volume volume = session.get(Volume.class, volumeDoi.toString());
            if (volume != null) {
                volumes.add(volume);
            } else {
              final String errorMessage = "Error getting volume: " + volumeDoi;
              addActionError(errorMessage);
              log.error(errorMessage);
            }
          }
          if (log.isDebugEnabled()) {
            log.debug(volumes.size() + " Volume(s) for Journal " + journal.getEIssn());
          }

          // get Issues for this Journal
          issues.clear();
          for (final Volume volume : volumes) {
            for (final URI issueDoi : volume.getSimpleCollection()) {
              final Issue issue = session.get(Issue.class, issueDoi.toString());
                if (issue != null) {
                    issues.add(issue);
                } else {
                  final String errorMessage = "Error getting issue: " + issueDoi;
                  addActionError(errorMessage);
                  log.error(errorMessage);
                }
            }
          }
          if (log.isDebugEnabled()) {
            log.debug(volumes.size() + " Volume(s), " + issues.size() + " Issue(s) for Journal "
                    + journal.getEIssn());
          }

          return null;
        }
      });

    // default action is just to display the template
    return SUCCESS;
  }

  /**
   * Create a Volume.
   *
   * Volume values taken from Struts Form.
   */
  private String createVolume() {

    // OTM usage wants to be in a Transaction
    TransactionHelper.doInTx(session,
      new TransactionHelper.Action<String>() {

        public String run(Transaction tx) {

          // the DOI must be unique
          if (session.get(Volume.class, doi.toString()) != null) {
            addActionError("Duplicate DOI, Volume, " + doi + ", already exists.");
            return ERROR;
          }

          Volume newVolume = new Volume();
          newVolume.setId(doi);
          DublinCore newDublinCore = new DublinCore();
          newDublinCore.setCreated(new Date());
          newVolume.setDublinCore(newDublinCore);
          newVolume.setDisplayName(displayName);
          newVolume.setImage(image);

          // process Issues
          if (aggregation != null && aggregation.length() != 0) {
            List<URI> issues = new ArrayList();
            for (final String issueToAdd : aggregation.split(SEPARATORS)) {
              if (issueToAdd.length() == 0) { continue; }
              issues.add(URI.create(issueToAdd.trim()));
            }
            newVolume.setSimpleCollection(issues);
          }

          session.saveOrUpdate(newVolume);

          addActionMessage("Created Volume: " + newVolume.toString());

          // add Volume to current Journal
          Journal currentJournal = journalService.getJournal();
          currentJournal.getVolumes().add(doi);
          session.saveOrUpdate(currentJournal);
          journalService.journalWasModified(currentJournal);
          addActionMessage("Volume was added to current Journal: " + currentJournal);

          return null;
        }
      });

    return SUCCESS;
  }

  /**
   * Update a Volume.
   *
   * Volume values taken from Struts Form.
   */
  private String updateVolume() {

    // OTM usage wants to be in a Transaction
    return TransactionHelper.doInTx(session,
      new TransactionHelper.Action<String>() {

        public String run(Transaction tx) {

          // the Volume to update
          Volume volume = session.get(Volume.class, doi.toString());

          // delete the Volume?
          if (aggregationToDelete != null && aggregationToDelete.toString().length() != 0) {
            session.delete(volume);
            addActionMessage("Deleted Volume: " + volume);
            // update Journal?
            Journal currentJournal = journalService.getJournal();
            List<URI> currentVolumes = currentJournal.getVolumes();
            if (currentVolumes.contains(doi)) {
              currentVolumes.remove(doi);
              session.saveOrUpdate(currentJournal);
              journalService.journalWasModified(currentJournal);
              addActionMessage("Deleted Volume from Journal: " + currentJournal);
            }
            return SUCCESS;
          }

          // assume updating the Volume
          volume.setDisplayName(displayName);
          volume.setImage(image);

          // process Issues
          List<URI> volumeIssues = new ArrayList();
          if (aggregation != null && aggregation.length() != 0) {
            for (final String issueToAdd : aggregation.split(SEPARATORS)) {
              if (issueToAdd.length() == 0) { continue; }
              volumeIssues.add(URI.create(issueToAdd.trim()));
            }
          } else {
            volumeIssues = null;
          }
          volume.setSimpleCollection(volumeIssues);

          session.saveOrUpdate(volume);

          addActionMessage("Updated Volume: " + volume.toString());

          return SUCCESS;
        }
      });
  }

  /**
   * Create a Issue.
   *
   * Issue values taken from Struts Form.
   */
  private String createIssue() {

    // OTM usage wants to be in a Transaction
    TransactionHelper.doInTx(session,
      new TransactionHelper.Action<String>() {

        public String run(Transaction tx) {

          // the DOI must be unique
          if (session.get(Issue.class, doi.toString()) != null) {
            addActionError("Duplicate DOI, Issue, " + doi + ", already exists.");
            return ERROR;
          }

          Issue newIssue = new Issue();
          newIssue.setId(doi);
          DublinCore newDublinCore = new DublinCore();
          newDublinCore.setCreated(new Date());
          newIssue.setDublinCore(newDublinCore);
          newIssue.setDisplayName(displayName);
          newIssue.setImage(image);

          // process Articles
          if (aggregation != null && aggregation.length() != 0) {
            List<URI> articles = new ArrayList();
            for (final String articleToAdd : aggregation.split(SEPARATORS)) {
              if (articleToAdd.length() == 0) { continue; }
              articles.add(URI.create(articleToAdd.trim()));
            }
            newIssue.setSimpleCollection(articles);
          }

          session.saveOrUpdate(newIssue);

          addActionMessage("Created Issue: " + newIssue.toString());

          // add Issue to latest Volume
          final Journal currentJournal = journalService.getJournal();
          Volume latestVolume = session.get(Volume.class, currentJournal.getVolumes()
                  .get(currentJournal.getVolumes().size() - 1).toString());
          latestVolume.getSimpleCollection().add(doi);
          session.saveOrUpdate(latestVolume);
          addActionMessage("Added Issue to Volume: " + latestVolume);
          journalService.journalWasModified(currentJournal);
          addActionMessage("Updated Journal: " + currentJournal);

          return null;
        }
      });

    return SUCCESS;
  }

  /**
   * Update an Issue.
   *
   * Issue values taken from Struts Form.
   */
  private String updateIssue() {

    // OTM usage wants to be in a Transaction
    return TransactionHelper.doInTx(session,
      new TransactionHelper.Action<String>() {

        public String run(Transaction tx) {

          // the Issue to update
          Issue issue = session.get(Issue.class, doi.toString());

          // delete the Issue?
          if (aggregationToDelete != null && aggregationToDelete.toString().length() != 0) {
            session.delete(issue);
            addActionMessage("Deleted Issue: " + issue);

            // update Volume?
            List<Volume> containingVolumes = session.createCriteria(Volume.class)
                    .add(Restrictions.eq("simpleCollection", doi)).list();
            if (containingVolumes.size() > 0) {
              for (Volume containingVolume : containingVolumes) {
                containingVolume.getSimpleCollection().remove(doi);
                session.saveOrUpdate(containingVolume);
                addActionMessage("Deleted Issue from Volume: " + containingVolume);
              }
              // XXX: assume current Journal needs updating, should be smarter
              final Journal currentJournal = journalService.getJournal();
              journalService.journalWasModified(currentJournal);
              addActionMessage("Updated Journal: " + currentJournal);
            }
            return SUCCESS;
          }

          // assume updating the Issue
          issue.setDisplayName(displayName);
          issue.setImage(image);

          // process Issues
          List<URI> issueArticles = new ArrayList();
          if (aggregation != null && aggregation.length() != 0) {
            for (final String articleToAdd : aggregation.split(SEPARATORS)) {
              if (articleToAdd.length() == 0) { continue; }
              issueArticles.add(URI.create(articleToAdd.trim()));
            }
          } else {
            issueArticles = null;
          }
          issue.setSimpleCollection(issueArticles);

          session.saveOrUpdate(issue);

          addActionMessage("Updated Issue: " + issue.toString());

          return SUCCESS;
        }
      });
  }

  /**
   * Gets all Volumes for the Journal.
   *
   * @return all Volumes for the Journal.
   */
  public List<Volume> getVolumes() {

    return volumes;
  }

  /**
   * Gets all Issues for a Journal.
   *
   * @return all Issues for the Journal.
   */
  public List<Issue> getIssues() {

    return issues;
  }

  /**
   * Set manageVolumesIssuesAction of Form.
   *
   * Enable Struts Form to set the manageVolumesIssuesAction.
   *
   * @param manageVolumesIssuesAction form action.
   */
  public void setManageVolumesIssuesAction(String manageVolumesIssuesAction) {
    this.manageVolumesIssuesAction = manageVolumesIssuesAction;
  }

  /**
   * Set Aggregation to delete.
   *
   * Enable Struts Form to set the Aggregation to delete as a String.
   *
   * @param aggregationToDelete the Aggregation to delete.
   */
  public void setAggregationToDelete(String aggregationToDelete) {
    this.aggregationToDelete = URI.create(aggregationToDelete);
  }

  /**
   * Get the Journal.
   *
   * @return the Journal.
   */
  public Journal getJournal() {
    return journal;
  }

  /**
   * Set DOI.
   *
   * Enable Struts Form to set the DOI as a String.
   * The DOI is arbitrary, treated as opaque and encouraged to be human friendly.
   *
   * @param doi DOI.
   */
  public void setDoi(String doi) {
    this.doi = URI.create(doi);
  }

  /**
   * Set display name.
   *
   * Enable Struts Form to set the display name.
   *
   * @param displayName display name.
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Set image.
   *
   * Enable Struts Form to set the image URI as a String.
   *
   * @param image the image for this journal.
   */
  public void setImage(String image) {

    if (image == null || image.length() == 0) {
      this.image = null;
    } else {
      this.image = URI.create(image);
    }
  }

  /**
   * Set aggregation, comma separated list of Issue DOIs.
   *
   * Enable Struts Form to set the aggregation as a String.
   * Note that toString() artifacts, "[]" may exist, trim them.
   *
   * @param aggregation comma separated list of Issue DOIs.
   */
  public void setAggregation(String aggregation) {

    // check for both pre/postfix, e.g. user may delete one or another
    if (aggregation.startsWith("[")) {
      aggregation = aggregation.substring(1);
    }
    if (aggregation.endsWith("]")) {
      aggregation = aggregation.substring(0, aggregation.length() - 1);
    }

    this.aggregation = aggregation;
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
}
