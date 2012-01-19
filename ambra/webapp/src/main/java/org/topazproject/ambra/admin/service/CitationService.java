/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.otm.Session;
import org.topazproject.otm.OtmException;
import org.topazproject.ambra.models.Citation;
import org.topazproject.ambra.models.UserProfile;

/**
 * Service class for managing citations
 * @author Dragisa Krsmanovic
 */
public class CitationService {

  private Session session;

  /**
   * Update visible fields in citation object.
   * @param citationId Citation ID
   * @param title Title
   * @param displayYear Year. Annotation citations have parenthesis around. For example: (2009)
   * @param journal Journal name
   * @param volume Volume (null by default)
   * @param issue Issue (null by default)
   * @param eLocationId ELocationID (null by default)
   * @param doi DOI. Default:  Article or annotation id without beggining info:doi/. For example: 10.1371/annotation/02eb93ff-2c25-44e7-8580-0388b665ea9e 
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void updateCitation(String citationId, String title, String displayYear, String journal,
                             String volume, String issue, String eLocationId, String doi) {
    Citation citation = getCitation(citationId);
    citation.setTitle(emptyStringToNull(title));
    citation.setDisplayYear(emptyStringToNull(displayYear));
    citation.setJournal(emptyStringToNull(journal));
    citation.setVolume(emptyStringToNull(volume));
    citation.setIssue(emptyStringToNull(issue));
    citation.setELocationId(emptyStringToNull(eLocationId));
    citation.setDoi(emptyStringToNull(doi));
  }

  /**
   * Add author to citation.
   * @param citationId Citation ID
   * @param surnames Author surname
   * @param givenNames Author given name
   * @param suffix Author suffix
   * @return New author ID
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String addAuthor(String citationId, String surnames, String givenNames, String suffix) {

    Citation citation = getCitation(citationId);

    UserProfile newAuthor = new UserProfile();
    newAuthor.setSurnames(emptyStringToNull(surnames));
    newAuthor.setGivenNames(emptyStringToNull(givenNames));
    newAuthor.setSuffix(emptyStringToNull(suffix));
    String authorId = session.saveOrUpdate(newAuthor);
    citation.getAuthors().add(newAuthor);
    return authorId;
  }

  /**
   * Delete author from citation
   * @param citationId Citation ID
   * @param authorId Author ID that is being deleted
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void deleteAuthor(String citationId, String authorId) {

    Citation citation = getCitation(citationId);
    UserProfile author = getAuthor(authorId);

    if (!citation.getAuthors().remove(author))
      throw new OtmException("Author <" + authorId + "> not found in citation <" + citationId + ">.");

    session.delete(author);
  }

  /**
   * Update author
   * @param authorId Author ID
   * @param surnames Author surname
   * @param givenNames Author given name
   * @param suffix Author suffix
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void updateAuthor(String authorId, String surnames, String givenNames, String suffix) {
    UserProfile author = getAuthor(authorId);

    author.setSurnames(emptyStringToNull(surnames));
    author.setGivenNames(emptyStringToNull(givenNames));
    author.setSuffix(emptyStringToNull(suffix));
  }

  /**
   * Add collaborative author
   * @param citationId Citation ID
   * @param collaborativeAuthor Collaborative author
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void addCollaborativeAuthor(String citationId, String collaborativeAuthor) {
    if (collaborativeAuthor != null && !collaborativeAuthor.trim().equals("")) {
      Citation citation = getCitation(citationId);
      citation.getCollaborativeAuthors().add(collaborativeAuthor.trim());
    }
  }

  /**
   * Remove collaborative author
   * @param citationId Citation ID
   * @param authorIndex Index of the collaborative author in the list
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void deleteCollaborativeAuthor(String citationId, int authorIndex) {
    Citation citation = getCitation(citationId);
    citation.getCollaborativeAuthors().remove(authorIndex);
  }

  /**
   * Update collaborative author at authorIndex position
   * @param citationId Citation ID
   * @param authorIndex Index of the collaborative author in the list
   * @param collaborativeAuthor Collaborative author
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void updateCollaborativeAuthor(String citationId, int authorIndex, String collaborativeAuthor) {
    if (collaborativeAuthor != null && !collaborativeAuthor.trim().equals("")) {
      Citation citation = getCitation(citationId);
      citation.getCollaborativeAuthors().set(authorIndex, collaborativeAuthor.trim());
    }
  }

  private UserProfile getAuthor(String authorId) {
    UserProfile author = session.get(UserProfile.class, authorId);
    if (author == null)
      throw new OtmException("Author <" + authorId + "> not found.");
    return author;
  }

  private Citation getCitation(String citationId) {
    Citation citation = session.get(Citation.class, citationId);
    if (citation == null)
      throw new OtmException("Citation <" + citationId + "> not found.");
    return citation;
  }


  private String emptyStringToNull(String text) {
    if (text == null || text.trim().equals(""))
      return(null);
    else
      return(text.trim());
  }

  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }
}
