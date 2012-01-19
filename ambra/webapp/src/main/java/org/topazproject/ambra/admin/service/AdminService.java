/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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

package org.topazproject.ambra.admin.service;

import org.topazproject.otm.Session;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Query;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.models.Volume;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.models.Issue;
import org.topazproject.ambra.models.DublinCore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * AdminService encapsulates the basic services needed by all administrative
 * actions.
 * 
 */
public class AdminService {

  // Services set by Spring
  private JournalService journalService;
  private Session        session;

  // Private fields
  private static final String SEPARATORS = "[,;]";
  private static final Log    log = LogFactory.getLog(AdminService .class);

  /**
   * Constructor
   */
  public AdminService() {

  }

  /**************************************************
   * Journal Management Methods                     *
   **************************************************/
  /**
   * Get current Journal.
   *
   * @return         the current journal for this context.
   */
  public Journal getJournal() {
    return journalService.getCurrentJournal();
  }

  /**
   * Get current Volumes for this Journal.
   *
   * @return   the current volume list for the journal.
   */
  public List<URI> getJrnlVolURIs() {
    return getJournal().getVolumes();
  }

  /**
   * Get a coppy of the Volume DOIs as a String List.
   *
   * @return   the current volume list for the journal.
   */
  public List<String> getJrnlVolDOIs() {
    List<URI> volURIs = getJrnlVolURIs();
    List<String> volStr = new ArrayList<String>();

    for(URI volURI : volURIs)
        volStr.add(volURI.toString());

    return volStr;
  }

  /**
   * Set current Volumes for this Journal.
   *
   * @param volURIs  the list of URIs to associate with this journal.
   */
  public void setJrnlVolURIs(List<URI> volURIs) {
    getJournal().setVolumes(volURIs);
    updateStore(getJournal());
  }

  /**
   * Add a volume URI to the journals volume URI list.
   *
   * @param volURI  URI of volume to add to journals list.
   */
  public void addJrnlVolURI(URI volURI) {
    List<URI> volURIs = getJrnlVolURIs();

    // If the URI is not already in the list, add it.
    if (!volURIs.contains(volURI)) {
      volURIs.add(volURI);
      updateStore(getJournal());
    }
  }

  /**
   * Give a SEPARATOR delimitted string of volume URIs convert them
   * to a list of separated URIs.
   *
   * @param csvStr    the list of string of URIs.
   *
   * @return          a list of URI created from the string csvStr .
   *
   * @throws URISyntaxException if a DOI cannot be converted to a vaild URI
   *                            a syntax exception is thrown.
   */
  public List<URI> parseCSV(String csvStr) throws URISyntaxException {
    List<URI> listURIs = new ArrayList<URI>();

    if ((csvStr != null) && (csvStr.length() > 0)) {
      String[] elements = csvStr.split(SEPARATORS);

      for(String element : elements) {
        URI uri = new URI(element.trim());
        listURIs.add(uri);
      }
    }
    return listURIs;
  }

  /**
   * Test a single URI for validity. Currently the only requirement is that
   * the URI must be absolute.
   *
   * @param uri     the URI to validate.
   *
   * @return        true if the URI is acceptable.
   *
   * @throws URISyntaxException if a DOI cannot be converted to a vaild URI
   *                            a syntax exception is thrown.
   */
  public Boolean validURI(URI uri) throws URISyntaxException {
    // Currently the only requirement is for the uri to be absolute.
    return uri.isAbsolute();
  }

  /**
   * Validate a list of URI to ensure that they conform to URI syntax and are
   * absolute. Return a list of URIs that didn't pass the validation.
   *
   * @param uris     the list of URIs to validate.
   *
   * @return         a list of URIs that are not absolute and therefore
   *                 not valid.
   *
   * @throws URISyntaxException if a DOI cannot be converted to a vaild URI
   *                            a syntax exception is thrown.
   */
  public List<URI> validateURIs(List<URI> uris) throws URISyntaxException {
    List<URI> badURIs = new ArrayList<URI>();

    for(URI testURI : uris)
      if (!validURI(testURI))
        badURIs.add(testURI);

    return badURIs; 
  }

  /**
   * Get current cross published Article URIs.
   *
   * @return    the  a list of cross published Article URIs.
   */
  public List<URI> getJrnlArticles() {
    return getJournal().getSimpleCollection();
  }

  /**
   * 
   */
  public void addXPubArticle(URI article) {
    List<URI> collection =  getJournal().getSimpleCollection();
    if (!collection.contains(article)) {
      collection.add(article);
      updateStore(getJournal());
    }
  }

  /**
   *
   */
  public void removeXPubArticle(URI article) {
    List<URI> collection =  getJournal().getSimpleCollection();
    if (collection.contains(article)) {
      collection.remove(article);
      updateStore(getJournal());
    }
  }

  /**
   * Set current Journal cross published article URI list.
   *
   * @param articles  the list of articles URIs.
   */
  public void setJrnlArticles(List<URI> articles) {
    getJournal().setSimpleCollection(articles);
  }

  /**
   * Get current Journal image URI.
   *
   * @return       the  image URI.
   */
  public URI getJrnlImageURI() {
    return getJournal().getImage();
  }

  /**
   * Set current Journal Image.
   *
   * @param imgURI  the image/article URI for the current journal.
   */
  public void setJrnlImageURI(URI imgURI) {
    URI newImage = ((imgURI != null) && (imgURI.toString().length() == 0)) ? null : imgURI;
    getJournal().setImage(newImage);
    updateStore(getJournal());
  }

  /**
   * Get current Journal issue.
   *
   * @return         the  image URI.
   */
  public URI getJrnlIssueURI() {
    return (getJournal() != null) ? getJournal().getCurrentIssue() : null;
  }

   /**
   * Set current Journal issue URI.
   *
   * @param issueURI   the URI of the current issue for the journal being modified.
   */
  public void setJrnlIssueURI(URI issueURI) {
    URI newImage = ((issueURI != null) && (issueURI.toString().length() == 0)) ? null : issueURI;
    getJournal().setCurrentIssue(newImage);
    updateStore(getJournal());
  }

  /**
   *  Update the persistant store with the new journal changes.
   *
   * @throws OtmException  if the sesion encounters an error during
   *                       the update.
   */
  public void updateStore(Object o) throws OtmException {
    session.saveOrUpdate(o);
  }

  /**
   *  Update the persistant store with the new journal changes.
   *
   * @throws OtmException  if the sesion encounters an error during
   *                       the update.
   */
  public void flushStore() throws OtmException {
    session.flush();
  }

  /**************************************************
   * Volume Management Methods                      *
   **************************************************/

  /**
   * Get the latest Volume for the current Journal
   * (current meaning the last volume added to the
   * journal).
   *
   * @return       the last volume in the journal's list.
   *               (can be null)
   *
   * @throws OtmException  throws OtmException if the session is unable
   *                       to get the uri provided by the current journal.
   */
  public Volume latestVolume() throws OtmException {
    URI uri = null;
    List<URI> volURIs = getJrnlVolURIs();

    if (volURIs.size() > 0) 
      uri = volURIs.get(volURIs.size() - 1);

    return (uri != null) ? session.get(Volume.class, uri.toString()) : null;
  }

  /**
   * Return a Volume object specified by URI.
   *
   * @param  volURI       the URI of the volume.
   * @return              the volume object requested.
   *
   * @throws OtmException throws OtmException if any one of the Volume URIs supplied
   *                      by the journal does not exist.
   */
  public Volume getVolume(URI volURI) throws OtmException {
    return session.get(Volume.class, volURI.toString());
  }

  /**
   * Uses the list of volume URIs maintained by the journal
   * to create a list of Volume objects.
   *
   * @return              the list of volumes for the current journal (never null)
   *
   * @throws OtmException throws OtmException if any one of the Volume URIs supplied
   *                      by the journal does not exist.
   */
  public List<Volume> getVolumes() throws OtmException {
    List<Volume> volumes = new ArrayList<Volume>();
    List<URI> volURIs = getJrnlVolURIs();

    for (final URI volUri : volURIs) {
      Volume volume = getVolume(volUri);

      if (volume != null) {
        volumes.add(volume);
      } else {
        log.error("getVolumes failed to retrieve: " + volUri);
      }
    }
    return volumes;
  }

  /**
   * Create a volume list that is a single comma delimitted string.
   *
   * @return              the list of volumes for the current journal (never null)
   *
   * @throws OtmException throws OtmException if any one of the Volume URIs supplied
   *                      by the journal does not exist.
   */
  public String getVolumesCSV() throws OtmException {
    String volCSV = "";
    List<URI> volumes = getJrnlVolURIs();

    Iterator iter = volumes.listIterator();
    while (iter.hasNext()) {
      volCSV = volCSV + iter.next().toString();
      if (iter.hasNext())
        volCSV = volCSV + ",";
    }
    return volCSV;
  }

  /**
   * Create a new Volume and add it to the current Journal's list
   * of volumes it contains.
   *
   * @param volURI     the uri of the new volume.
   * @param dsplyName  the display name of the volume.
   * @param issueList  a SPARATOR delimted list of issue doi's associated with
   *                       this volume.
   *
   * @return               the volume object that was created. ( returns null if there
   *                       is no journal or volURI already exists ).
   *
   * @throws OtmException  thrown when the Volume or Journal cannot be
   *                       saved or updated by the session.
   * @throws URISyntaxException thrown when values in issueList cannot be converted
   *                            to a URI
   */
  public Volume createVolume(URI volURI, String dsplyName, String issueList )
                  throws OtmException, URISyntaxException {

    String displayName = (dsplyName == null) ? "" : dsplyName;

    /* If there is no journal then don't
     * create an orphan volume : return null.
     */
    if (getJournal() == null)
      return null;

    // Volume URI already exist return null
    if (session.get(Volume.class, volURI.toString()) != null)
      return null;

    Volume newVol = new Volume();
    newVol.setId(volURI);

    // Create the DC metatdata.
    DublinCore newDC = new DublinCore();
    newDC.setCreated(new Date());
    newVol.setDublinCore(newDC);
    newVol.setDisplayName(displayName);

    /*
     * Issues come in as a SEPARATOR delimitted string
     * that is split into an ArrayList of strings.
     */
    if (issueList  != null && issueList .length() != 0) {
      List<URI> issues = new ArrayList<URI>();

      for (final String issueToAdd : issueList .split(SEPARATORS)) {
        if (issueToAdd.length() > 0)
          issues.add(URI.create(issueToAdd));
      }
      newVol.setIssueList(issues);
    }

    // save the new volume.
    updateStore(newVol);
    // Add this new volume URI to the Journal list
    addJrnlVolURI(volURI);
    updateStore(getJournal());
    
    return newVol;
  }

  /**
   * Delete the specified volume. Remove references to it from the journal
   * volume list.
   *
   * @param volume         the volume to delete.
   *
   * @throws OtmException  throws OtmException if session cannot
   *                       delete the volume.
   */
  public void deleteVolume(Volume volume) throws OtmException {
    // Update the object store
    session.delete(volume);
     // Update Journal
    List<URI> jrnlVols = getJrnlVolURIs();

    if (jrnlVols.contains(volume.getId())) {
      jrnlVols.remove(volume.getId());
      setJrnlVolURIs(jrnlVols);
      updateStore(getJournal());
    }
    flushStore();
  }

  /**
   * Delete a Volume using the volumes URI.  Remove references to it from the journal
   * volume list.
   *
   * @param volURI         the volume to delete.
   *
   * @throws OtmException  throws OtmException if session cannot
   *                       delete the volume.
   */
  public void deleteVolume(URI volURI) throws OtmException {
    // the Volume to update
    Volume volume = session.get(Volume.class, volURI.toString());
    deleteVolume(volume);
  }

  /**
   * Update a Volume.
   *
   * @param volume    the volume to update.
   * @param dsplyName the display name for the volume.
   * @param issueList a SEPARATOR delimitted string of issue doi's.
   *
   * @return Volume   the update volume object.
   *
   * @throws OtmException throws and OtmException if the session is unable to
   *                      update the volume persistanct store.
   *
   */
  public Volume updateVolume(Volume volume, String dsplyName, List<URI> issueList)
                  throws OtmException, URISyntaxException {

    volume.setDisplayName(dsplyName);
    volume.setIssueList(issueList);
    updateStore(volume);

    return volume;
  }

  /**
   * Update a Volume using the URI. Retrieves volume from the persistant store
   * using the URI.
   *
   * @param volURI    the volume to update.
   * @param dsplyName the display name for the volume.
   * @param issueList a SEPARATOR delimitted string of issue doi's.
   *
   * @return Volume   the update volume object.
   *
   * @throws OtmException throws and OtmException if the session is unable to
   *                      update the volume persistanct store.
   *
   */
  public Volume updateVolume(URI volURI, String dsplyName, List<URI> issueList)
                  throws OtmException, URISyntaxException {
    // If the volume doesn't exist return null
    Volume volume = session.get(Volume.class, volURI.toString());
    
    if (volume != null)
      return updateVolume(volume, dsplyName, issueList);

    return null;
  }

  /**************************************************
   * Issue Management Methods                       *
   **************************************************/
 /**
  * Delete an Issue and remove it from each volume that references it.
  *
  * @param  issue   the issue that is to deleted.
  *
  * @throws OtmException         if session is not able to delete issue
  */
  public void deleteIssue(Issue issue)
                throws OtmException {

    URI issueURI = issue.getId();
    session.delete(issue);
    // Get all volumes that have this issue in their issueList
    List<Volume> containerVols = getIssueParents(issueURI);

    for (Volume vol : containerVols) {
      vol.getIssueList().remove(issueURI);
      updateStore(vol);
    }
   
    flushStore();
  }

  /**
   * Delete an Issue specified by URI. Remove it from each volume that references it.
   *
   * @param issueURI   the uri of the issue to delete.
   *
   * @throws OtmException         if session is not able to delete issue
   */
  public void deleteIssue(URI issueURI) throws OtmException {
    // the Volume to update
    Issue issue = session.get(Issue.class, issueURI.toString());
    deleteIssue(issue);
  }

  /**
   * Get an Issue specified by URI.
   *
   * @param issueURI    the issue's URI.
   *
   * @return          the Issue object specified by URI.
   *
   * @throws  OtmException  if the session get incounters an error.
   *
   */
  public Issue getIssue(URI issueURI) throws OtmException {
    return session.get(Issue.class, issueURI.toString());
  }

  /**
   * Get a list of issues from the specified volume.
   *
   * @param volumeURI    the volume of interest.
   *
   * @return          the list of issues associated with the volume (never null).
   *
   * @throws  OtmException  if the session get incounters an error.
   *
   */
  public List<Issue> getIssues(URI volumeURI) throws OtmException {
    Volume volume = getVolume(volumeURI);
    return getIssues(volume);
  }

  /**
   * Get a list of issues from the specified volume.
   *
   * @param volume    the volume of interest.
   *
   * @return          the list of issues associated with the volume (never null).
   *
   * @throws  OtmException  if the session get incounters an error.
   *
   */
  public List<Issue> getIssues(Volume volume) throws OtmException {
    List<Issue> issues = new ArrayList<Issue>();

    if (volume.getIssueList() != null) {
      for (final URI issueURI : volume.getIssueList()) {
        final Issue issue = getIssue(issueURI);

        if (issue != null)
          issues.add(issue);
        else
          log.error("Error getting issue: " + issueURI.toString());
      }
    }
    return issues;
  }

  /**
   * Get a list of issues from the specified volume.
   *
   * @param volume    the volume of interest.
   *
   * @return          the list of issues associated with the volume (never null).
   *
   * @throws  OtmException  if the session get incounters an error.
   *
   */
  public String getIssuesCSV(Volume volume) throws OtmException {
    StringBuilder issCSV = new StringBuilder();
    List<Issue> issues = getIssues(volume);
    Iterator iter = issues.listIterator();

    while(iter.hasNext()) {
      Issue i = (Issue)iter.next();
      issCSV.append(i.getId().toString());
      if (iter.hasNext())
        issCSV.append(',');
    }
    return issCSV.toString();
  }

   /**
   * Get a list of issues from the specified volume.
   *
   * @param volURI    the volume of interest.
   *
   * @return          the list of issues associated with the volume (never null).
   *
   * @throws  OtmException  if the session get incounters an error.
   *
   */
  public String getIssuesCSV(URI volURI) throws OtmException {
    Volume volume = getVolume(volURI);

    return getIssuesCSV(volume);
  }

  /**
   * Create an Issue. When an issue is created new DublinCore meta-data needs
   * to be attached to the issue. The data consists of a string list of doi's
   * delimited by SEPARATOR. The new issue is attached to the lastest volume
   * for the journal context.
   *
   * @param issueURI    the issue to update.
   * @param imgURI      a URI for the article/image associated with this volume.
   * @param dsplyName   the display name for the volume.
   * @param articleList a SEPARATOR delimitted string of article doi's.
   *
   * @return          the issue created or null if unable to create the issue
   *                  or the issue exist.
   *
   * @throws OtmException  throws OtmException if the session fails to save the
   *                       issue or update the volume.
   */
  public Issue createIssue(Volume vol, URI issueURI, URI imgURI, String dsplyName,
      String articleList) throws OtmException {

    /*
     * Return null if issue exist.
     */
    if (session.get(Issue.class, issueURI.toString()) != null)
      return null;

    Issue newIssue = new Issue();
    newIssue.setId(issueURI);

    DublinCore newDublinCore = new DublinCore();
    newDublinCore.setCreated(new Date());
    newIssue.setDublinCore(newDublinCore);
    newIssue.setDisplayName(dsplyName);
   
    if (imgURI.toString().equals(""))
      newIssue.setImage(null);
    else
      newIssue.setImage(imgURI);

    /*
     * Articles are specified in a SEPARATOR delimited
     * string of the doi's for each article associated
     * with the issue.
     */
    if (articleList != null && articleList.length() != 0) {
      for (final String articleToAdd : articleList.split(SEPARATORS)) {
        if (articleToAdd.length() > 0)
          newIssue.addArticle(URI.create(articleToAdd.trim()));
      }
    }
    // Default respect order to false.
    newIssue.setRespectOrder(false);
    updateStore(newIssue);

    // Update the volume.
    vol.getIssueList().add(issueURI);
    updateStore(vol);
    flushStore();

    return newIssue;
  }

  /**
   * Update an Issue. Since this is an update it is assumed the issue is already
   * associated with aa volume.
   *
   * @param issueURI     the issue to update.
   * @param imgURI       a URI for the article/image associated with this volume.
   * @param dsplyName    the display name for the volume.
   * @param articleList  a SEPARATOR delimitted string of article doi's.
   * @param respectOrder respect the order manual ordering of articles within
   *                     articleTypes.
   *
   * @return            the updated issue or null if the issue does not exist.
   *
   * @throws  OtmException throws OtmException if session cannot update the issue.
   *
   */
  @SuppressWarnings("unchecked")
  public Issue updateIssue(URI issueURI, URI imgURI, String dsplyName,
    List<URI> articleList, Boolean respectOrder) throws OtmException, URISyntaxException {

    // the Issue to update
    Issue issue = session.get(Issue.class, issueURI.toString());

    // If the issue doesn't exist then return null.
    if (issue == null)
      return null;

    if (!dsplyName.equals(issue.getDisplayName()))
      issue.setDisplayName(dsplyName);

    issue.setArticleList(articleList);
    issue.setRespectOrder(respectOrder);

    if (imgURI.toString().equals(""))
      issue.setImage(null);
    else
      issue.setImage(imgURI);
    
    updateStore(issue);
    flushStore();
    
    return issue;
  }

 /*
  *
  */
  public Issue removeArticle(Issue issue, URI articleURI) throws OtmException {
    issue.removeArticle(articleURI);
    updateStore(issue);

    return issue;
  }

  /*
   *
   */
  @SuppressWarnings("unchecked")
  public Issue addArticle(Issue issue, URI articleURI) throws OtmException {
    issue.addArticle(articleURI);
    updateStore(issue);

    return issue;
  }

  /**************************************************
   *                OTM queries.                    *
   **************************************************/
  /**
   * Get a list of volume URIs for this journal context.
   *
   * @param maxResults   the maximum number of URIs to put into the list.
   *                     maxResults = 0 will return all URIs.
   * @param ascending    sort URI's in ascending order if true.
   *
   * @return             a list of URIs for volumes associated with this
   *                     journal (never null).
   *
   * @throws OtmException if session is not able to create or execute a query.
   */
  @Transactional(readOnly = true)
  public List<URI> getVolumeURIs(int maxResults, Boolean ascending) throws OtmException {
    StringBuilder qry = new StringBuilder();

    qry.append("select v.id id from Volume v ");
    // add ordering and limit
    qry.append("order by id ").append(ascending ? "asc" : "desc");

    if (maxResults > 0)
      qry.append(" limit ").append(maxResults);

    qry.append(";");

    List<URI> uriRslt = new ArrayList<URI>();
    // create the query, applying parameters
    Query q = session.createQuery(qry.toString());

    Results r = q.execute();
    while (r.next()) 
      uriRslt.add(r.getURI(0));

    return uriRslt;
  }
   /**
   * Get a list of volume URIs for this journal context.
   *
   * @param maxResults   the maximum number of URIs to put into the list.
   *                     maxResults = 0 will return all URIs.
   * @param ascending    sort URI's in ascending order if true.
   *
   * @return             a list of volumes associated with this
   *                     journal (never null).
   *
   * @throws OtmException if session is not able create and execute a query.
   */
  @Transactional(readOnly = true)
  public List<Volume> getVolumes(int maxResults, Boolean ascending) throws OtmException {
    StringBuilder qry = new StringBuilder();

    qry.append("select v, v.id id from Volume v ");
    // add ordering and limit
    qry.append("order by id ").append(ascending ? "asc" : "desc");

    if (maxResults > 0)
      qry.append(" limit ").append(maxResults);

    qry.append(";");

    List<Volume> volRslt = new ArrayList<Volume>();
    // create the query, applying parameters
    Query q = session.createQuery(qry.toString());

    Results r = q.execute();
    while (r.next())
      volRslt.add((Volume) r.get(0));

    return volRslt;
  }

  /**
   * Get a list of issue URIs for this journal context.
   *
   * @param maxResults   the maximum number of URIs to put into the list.
   *                     maxResults = 0 will return all URIs.
   * @param ascending    sort URI's in ascending order if true.
   *
   * @return             the list of issue URIs for this journal context.
   * 
   * @throws OtmException if session is not able create or execute the query.
   */
  @Transactional(readOnly = true)
  public List<URI> getIssueURIs(int maxResults, Boolean ascending)
                        throws OtmException {
    StringBuilder qry = new StringBuilder();

    qry.append("select i.id id from Issue i ");
    // add ordering and limit
    qry.append("order by id ").append(ascending ? "asc" : "desc");

    if (maxResults > 0)
      qry.append(" limit ").append(maxResults);

    qry.append(";");

    List<URI> uriRslt = new ArrayList<URI>();
    // create the query, applying parameters
    Query q = session.createQuery(qry.toString());

    Results r = q.execute();
    while (r.next())
      uriRslt.add(r.getURI(0));

    return uriRslt;
  }

  /**
   * Get a list of issues for this journal context.
   *
   * @param maxResults   the maximum number of URIs to put into the list.
   *                     maxResults = 0 will return all URIs.
   * @param ascending    sort URI's in ascending order if true.
   *
   * @return             the list of issue URIs for this journal context.
   *
   * @throws OtmException if session is not able create or execute the query.
   */
  @Transactional(readOnly = true)
  public List<Issue> getIssues(int maxResults, Boolean ascending) throws OtmException {
    StringBuilder qry = new StringBuilder();

    qry.append("select i, i.id id from Issue i ");
    // add ordering and limit
    qry.append("order by id ").append(ascending ? "asc" : "desc");

    if (maxResults > 0)
      qry.append(" limit ").append(maxResults);

    qry.append(";");

    List<Issue> issueRslt = new ArrayList<Issue>();
    // create the query, applying parameters
    Query q = session.createQuery(qry.toString());

    Results r = q.execute();
    while (r.next())
      issueRslt.add((Issue)r.get(0));

    return issueRslt;
  }

  /**
   *
   */
  @Transactional(readOnly = true)
  public List<URI> getIssueParentURIs(URI issueURI) throws OtmException {
    StringBuilder qry = new StringBuilder();

    qry.append("select v from Volume v where v.issueList = :uri; ");

    List<URI> volRslt = new ArrayList<URI>();
    // create the query, applying parameters
    Query q = session.createQuery(qry.toString());
    q.setParameter("uri", issueURI);

    Results r = q.execute();
    while (r.next())
      volRslt.add(r.getURI(0));

    return volRslt;
  }

  /**
   * Get a list of volume URIs that reference this issue.
   *
   * @param issueURI      URI of issue to find parents for.
   *
   * @return              the list of parent volumes that refernce this issue.
   * @throws OtmException if session is not able create or execute the query.
   */
  @Transactional(readOnly = true)
  public List<Volume> getIssueParents(URI issueURI) throws OtmException {
    StringBuilder qry = new StringBuilder();

    qry.append("select v from Volume v where v.issueList = :uri; ");

    List<Volume> volRslt = new ArrayList<Volume>();
    // create the query, applying parameters
    Query q = session.createQuery(qry.toString());
    q.setParameter("uri", issueURI);

    Results r = q.execute();
    while (r.next())
      volRslt.add((Volume) r.get(0));

    return volRslt;
  }

  /**************************************************
   * Spring managed setter/getters for fields       *
   **************************************************/
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
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * A faux journal object that can be accessed by Freemarjer Templates.
   */
  public static final class JournalInfo {
    private String key, eissn;
    private String smartCollectionRulesDescriptor;
    private String image, currentIssue;
    private List<String> volumes;
    private List<String> simpleCollection;

    public String getKey() {
      return key;
    }
    public void setKey(String key) {
      this.key = key;
    }
    public String geteIssn() {
      return eissn;
    }
    public void seteIssn(String eissn) {
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
    public List<String> getVolumes() {
      return volumes;
    }
    public void setVolumes(List<String> volumes) {
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

  /**
   * A faux Journal object that can be accessed by the freemarker
   * template.
   *
   * @return       faux Journal object JournalInfo.
   */
  @Transactional(readOnly = true)
  public JournalInfo createJournalInfo() {
    JournalInfo jrnlInfo = new JournalInfo();

    // If the is no current journal the return null
    if (getJournal() == null)
      return jrnlInfo;

    jrnlInfo.setKey(getJournal().getKey());
    jrnlInfo.seteIssn(getJournal().geteIssn());

    URI uri = (getJournal().getCurrentIssue() == null) ? null : getJournal().getCurrentIssue();
    jrnlInfo.setCurrentIssue((uri != null) ? uri.toString() : null);

    uri = (getJournal().getImage() == null) ? null : getJournal().getImage();
    jrnlInfo.setImage((uri != null) ? uri.toString() : null);

    List<URI> jscs = getJournal().getSimpleCollection();
    if(jscs != null) {
      List<String> slist = new ArrayList<String>(jscs.size());

      for(URI u : jscs)
        slist.add(u.toString());

      jrnlInfo.setSimpleCollection(slist);
    }

    List<DetachedCriteria> dclist = getJournal().getSmartCollectionRules();

    if(dclist != null && dclist.size() > 0) {
      StringBuilder sb = new StringBuilder();

      for(DetachedCriteria dc : getJournal().getSmartCollectionRules())
        sb.append(", ").append(dc.toString());

      jrnlInfo.setSmartCollectionRulesDescriptor(sb.substring(2));
    }
    jrnlInfo.volumes = getJrnlVolDOIs();
    return jrnlInfo;
  }
}
