/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
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

package org.topazproject.ambra.annotation.service;

import org.topazproject.ambra.models.Citation;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.Retraction;
import org.topazproject.ambra.models.UserProfile;
import org.topazproject.otm.Session;

import java.util.ArrayList;

/**
 * @author Alex Kudlick Date: 4/18/11
 *         <p/>
 *         org.topazproject.ambra.annotation.service
 */
public class TopazAnnotationUtil {
  /**
   * Create default citation on formal correction based on article's citation.
   * @param correction Formal correction
   * @param session OTM session
   * @throws Exception if migration fails
   */
  public static void createDefaultCitation(FormalCorrection correction, Citation articleCitation, Session session) throws Exception {
    Citation citation = createFormalCorrectionCitation(correction.getId().toString(), articleCitation, session);
    session.saveOrUpdate(citation);
    correction.setBibliographicCitation(citation);
  }

  /**
   * Create default citation on retraction based on article's citation.
   * @param retraction Retraction
   * @param session OTM session
   * @throws Exception if migration fails
   */
  public static void createDefaultCitation(Retraction retraction, Citation articleCitation, Session session) throws Exception {
    Citation citation = createRetractionCitation(retraction.getId().toString(), articleCitation, session);
    session.saveOrUpdate(citation);
    retraction.setBibliographicCitation(citation);
  }

  private static Citation createFormalCorrectionCitation(String annotationId, Citation articleCitation, Session session)
      throws Exception {

    Citation citation = new Citation();
    citation.setTitle("Correction: " + articleCitation.getTitle());
    copyCommonProperties(annotationId, articleCitation, citation, session);
    return citation;
  }

  private static Citation createRetractionCitation(String annotationId, Citation articleCitation, Session session)
      throws Exception {

    Citation citation = new Citation();
    citation.setTitle("Retraction: " + articleCitation.getTitle());
    copyCommonProperties(annotationId, articleCitation, citation, session);
    return citation;
  }

  private static void copyCommonProperties(String annotationId, Citation articleCitation, Citation citation, Session session)
      throws Exception {
    citation.setJournal(articleCitation.getJournal());
    citation.setYear(articleCitation.getYear());
    citation.setDisplayYear("(" + articleCitation.getDisplayYear() + ")");
    citation.setMonth(articleCitation.getMonth());
    citation.setDay(articleCitation.getDay());
    citation.setCitationType(articleCitation.getCitationType());
    citation.setDoi(annotationId.replaceFirst("info:doi/",""));
    citation.setELocationId(null);
    citation.setVolume(null);
    citation.setVolumeNumber(null);
    citation.setIssue(null);
    citation.setUrl(articleCitation.getUrl());
    citation.setSummary(articleCitation.getSummary());
    citation.setPublisherName(articleCitation.getPublisherName());
    citation.setPublisherLocation(articleCitation.getPublisherLocation());
    citation.setPages(articleCitation.getPages());
    citation.setKey(articleCitation.getKey());
    citation.setNote(articleCitation.getNote());

    if (articleCitation.getAuthors() != null) {
      citation.setAuthors(new ArrayList<UserProfile>());
      for (UserProfile userProfile : articleCitation.getAuthors()) {
        UserProfile newProfile = userProfile.clone();
        newProfile.setId(null);
        session.saveOrUpdate(newProfile);
        citation.getAuthors().add(newProfile);
      }
    }

    citation.setCollaborativeAuthors(articleCitation.getCollaborativeAuthors());

    if (articleCitation.getEditors() != null) {
      citation.setEditors(new ArrayList<UserProfile>());
      for (UserProfile editor : articleCitation.getEditors()) {
        UserProfile newProfile = editor.clone();
        newProfile.setId(null);
        session.saveOrUpdate(newProfile);
        citation.getEditors().add(newProfile);
      }
    }
  }
}
