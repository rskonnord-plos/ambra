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

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import static org.easymock.EasyMock.*;
import org.easymock.Capture;
import org.topazproject.otm.Session;
import org.topazproject.otm.OtmException;
import org.topazproject.ambra.models.Citation;
import org.topazproject.ambra.models.UserProfile;

import java.net.URI;
import java.util.ArrayList;

/**
 * @author Dragisa Krsmanovic
 */
public class CitationServiceTest {


  @Test
  public void testUpdateCitation() {
    String id = "CitationId";
    String title = "title2";
    String year = "year2";
    String journal = "journal2";
    String volume = "volume2";
    String issue = "issue2";
    String eLocationId = "eLocationID2";
    String doi = "doi2";

    Session session = createMock(Session.class);

    Citation citation = createCitation(id);

    expect(session.get(Citation.class, id)).andReturn(citation);

    replay(session);

    CitationService service = new CitationService();
    service.setOtmSession(session);
    service.updateCitation(id, title, year, journal, volume, issue, "  " + eLocationId + "  ", doi);

    verify(session);

    assertEquals(citation.getTitle(), title);
    assertEquals(citation.getDisplayYear(), year);
    assertEquals(citation.getVolume(), volume);
    assertEquals(citation.getIssue(), issue);
    assertEquals(citation.getJournal(), journal);
    assertEquals(citation.getELocationId(), eLocationId);
    assertEquals(citation.getDoi(), doi);
  }


  @Test(expectedExceptions = {OtmException.class})
  public void testUpdateCitationThatDoesNotExist() {
    String id = "CitationId";
    String title = "title2";
    String year = "year2";
    String journal = "journal2";
    String volume = "volume2";
    String issue = "issue2";
    String eLocationId = "eLocationID2";
    String doi = "doi2";

    Session session = createMock(Session.class);

    expect(session.get(Citation.class, id)).andReturn(null);

    replay(session);

    CitationService service = new CitationService();
    service.setOtmSession(session);
    service.updateCitation(id, title, year, journal, volume, issue, "  " + eLocationId + "  ", doi);
  }


  @Test
  public void testAddAuthor() {
    String id = "CitationId";
    Session session = createMock(Session.class);

    Citation citation = createCitation(id);
    int sizeBefore = citation.getAuthors().size();

    Capture<UserProfile> capture = new Capture<UserProfile>();

    expect(session.get(Citation.class, id)).andReturn(citation);
    expect(session.saveOrUpdate(capture(capture))).andReturn("newUserId");
    replay(session);

    CitationService service = new CitationService();
    service.setOtmSession(session);
    String surnames = "New Surname";
    String givenNames = "NewGivenName";
    String suffix = "NewSuffix";
    service.addAuthor(id, surnames, givenNames, suffix);

    verify(session);

    UserProfile newAuthor = capture.getValue();

    assertEquals(newAuthor.getSurnames(), surnames);
    assertEquals(newAuthor.getGivenNames(), givenNames);
    assertEquals(newAuthor.getSuffix(), suffix);

    assertEquals(citation.getAuthors().size(), sizeBefore + 1);
    assertTrue(citation.getAuthors().contains(newAuthor));
  }


  @Test
  public void testDeleteAuthor() {
    String id = "CitationId";
    Session session = createMock(Session.class);

    Citation citation = createCitation(id);
    int sizeBefore = citation.getAuthors().size();
    UserProfile author1 = citation.getAuthors().get(0);
    String authorId = author1.getId().toString();

    expect(session.get(Citation.class, id)).andReturn(citation);
    expect(session.get(UserProfile.class, authorId)).andReturn(author1);
    expect(session.delete(author1)).andReturn(authorId);
    replay(session);

    CitationService service = new CitationService();
    service.setOtmSession(session);
    service.deleteAuthor(id, authorId);

    verify(session);

    assertEquals(citation.getAuthors().size(), sizeBefore - 1);
    assertFalse(citation.getAuthors().contains(author1));
  }


  @Test(expectedExceptions = {OtmException.class})
  public void testDeleteAuthorThatDoesNotExist() {
    String id = "CitationId";
    String authorId = "WrongID";
    Session session = createMock(Session.class);

    Citation citation = createCitation(id);

    expect(session.get(Citation.class, id)).andReturn(citation);
    expect(session.get(UserProfile.class, authorId)).andReturn(null);
    replay(session);

    CitationService service = new CitationService();
    service.setOtmSession(session);
    service.deleteAuthor(id, authorId);
  }


  @Test(expectedExceptions = {OtmException.class})
  public void testDeleteAuthorThatIsNotInTheCitation() {
    String id = "CitationId";
    Session session = createMock(Session.class);

    Citation citation = createCitation(id);
    UserProfile author1 = createUser("Dummyid", "Donald", "Duck", null);
    String authorId = author1.getId().toString();

    expect(session.get(Citation.class, id)).andReturn(citation);
    expect(session.get(UserProfile.class, authorId)).andReturn(author1);
    replay(session);

    CitationService service = new CitationService();
    service.setOtmSession(session);
    service.deleteAuthor(id, authorId);
  }


  @Test
  public void testUpdateAuthor() {
    Session session = createMock(Session.class);

    String userId = "UserId";
    UserProfile author = createUser(userId, "Nobody", "That", "We know");

    expect(session.get(UserProfile.class, userId)).andReturn(author);
    replay(session);

    CitationService service = new CitationService();
    service.setOtmSession(session);
    String surnames = "New Surname";
    String givenNames = "NewGivenName";
    String suffix = "         ";
    service.updateAuthor(userId, " " + surnames + " ", givenNames, suffix);

    verify(session);

    assertEquals(author.getSurnames(), surnames);
    assertEquals(author.getGivenNames(), givenNames);
    assertNull(author.getSuffix());
  }


  @Test
  public void testAddCollaborativeAuthor() {
    String id = "CitationId";
    Session session = createMock(Session.class);

    Citation citation = createCitation(id);
    int sizeBefore = citation.getCollaborativeAuthors().size();

    expect(session.get(Citation.class, id)).andReturn(citation);
    replay(session);

    CitationService service = new CitationService();
    service.setOtmSession(session);
    String newCollabAuthor = "newCollabAuthor";
    service.addCollaborativeAuthor(id, newCollabAuthor);

    verify(session);

    assertEquals(citation.getCollaborativeAuthors().size(), sizeBefore + 1);
    assertTrue(citation.getCollaborativeAuthors().contains(newCollabAuthor));
  }


  @Test
  public void testDeleteCollaborativeAuthor() {
    String id = "CitationId";
    Session session = createMock(Session.class);

    Citation citation = createCitation(id);
    int sizeBefore = citation.getCollaborativeAuthors().size();
    String author = citation.getCollaborativeAuthors().get(0);

    expect(session.get(Citation.class, id)).andReturn(citation);
    replay(session);

    CitationService service = new CitationService();
    service.setOtmSession(session);
    service.deleteCollaborativeAuthor(id, 0);

    verify(session);

    assertEquals(citation.getCollaborativeAuthors().size(), sizeBefore - 1);
    assertFalse(citation.getCollaborativeAuthors().contains(author));
  }

  @Test
  public void testUpdateCollaborativeAuthor() {
    String id = "CitationId";
    Session session = createMock(Session.class);

    Citation citation = createCitation(id);
    int sizeBefore = citation.getCollaborativeAuthors().size();

    expect(session.get(Citation.class, id)).andReturn(citation);
    replay(session);

    CitationService service = new CitationService();
    service.setOtmSession(session);
    String updatedAuthor = "UpdatedAuthor";
    service.updateCollaborativeAuthor(id, 0, " " + updatedAuthor + " ");

    verify(session);

    assertEquals(citation.getCollaborativeAuthors().size(), sizeBefore);
    assertTrue(citation.getCollaborativeAuthors().contains(updatedAuthor));
  }


  private Citation createCitation(String id) {
    Citation citation = new Citation();
    citation.setId(URI.create(id));
    citation.setTitle("title");
    citation.setDisplayYear("(2009)");
    citation.setELocationId("eLocationID");
    citation.setVolume(null);
    citation.setIssue(null);
    citation.setJournal("Journal");
    citation.setDoi("doi");

    citation.setAuthors(new ArrayList<UserProfile>());

    citation.getAuthors().add(createUser("Auth1", "John", "Doe", null));
    citation.getAuthors().add(createUser("Auth2", "Jane", "Smith", "Jr."));

    citation.setCollaborativeAuthors(new ArrayList<String>());
    citation.getCollaborativeAuthors().add("Collab Author 1");

    return citation;
  }


  private UserProfile createUser(String id, String givenNames, String surnames, String suffix) {
    UserProfile user = new UserProfile();
    user.setId(URI.create(id));
    user.setGivenNames(givenNames);
    user.setSurnames(surnames);
    user.setSuffix(suffix);
    return user;
  }
}
