/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. |
 */

package org.topazproject.ambra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.*;
import org.topazproject.ambra.testutils.DummyDataStore;
import org.topazproject.ambra.util.Pair;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

/**
 * Base class for tests of Ambra Service Beans.  This is provided just so they can all use the same applicationContext
 * xml file; Bean tests should just test methods of the interface and have an instance autowired (see {@link
 * org.topazproject.ambra.annotation.service.AnnotationServiceTest} for an example.
 *
 * @author Alex Kudlick Date: 4/29/11
 *         <p/>
 *         org.topazproject.ambra
 */
@ContextConfiguration(locations = "nonWebApplicationContext.xml")
@Test(singleThreaded = true)
public abstract class BaseTest extends AbstractTestNGSpringContextTests {

  private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  /**
   * Instance provided so that tests can store dummy data in the same test database that the autowired beans are using.
   * Tests should use this to seed the database with data to test.
   */
  @Autowired
  protected DummyDataStore dummyDataStore;
  public static final String DEFAULT_ADMIN_AUTHID = "AdminAuthorizationID";
  public static final String DEFUALT_USER_AUTHID = "DummyTestUserAuthorizationID";

  protected void compareSecondaryObjects(ObjectInfo actual, ObjectInfo expected) {
    assertEquals(actual.getId(), expected.getId(), "Secondary object had incorrect id");
    assertEquals(actual.geteIssn(), expected.geteIssn(), "Secondary object " + expected.getId() + " had incorrect eIssn");
    assertEquals(actual.getContextElement(), expected.getContextElement(),
        "Secondary object " + expected.getId() + " had incorrect context element");
    assertNotNull(actual.getIsPartOf(), "Secondary object " + expected.getId() + " had null isPartOf");
    assertEquals(actual.getIsPartOf().getId(), expected.getIsPartOf().getId(),
        "Secondary object " + expected.getId() + " had incorrect isPartOf");

    compareDublinCore(actual.getDublinCore(), expected.getDublinCore(), false);

    assertEquals(actual.getRepresentations().size(), expected.getRepresentations().size(),
        "Secondary object had incorrect number of representations");

    compareRepresentations(actual.getRepresentations(), expected.getRepresentations());
  }

  protected void compareDublinCore(DublinCore actual, DublinCore expected, boolean compareUserProfileLists) {
    assertEquals(actual.getIdentifier(), expected.getIdentifier(),
        "Returned incorrect DublinCore identifier");
    assertEquals(actual.getRights(), expected.getRights(),
        "Returned incorrect DublinCore rights");
    assertEquals(actual.getType(), expected.getType(),
        "Returned incorrect DublinCore type");
    assertEquals(actual.getLanguage(), expected.getLanguage(),
        "Returned incorrect DublinCore language");
    assertEquals(actual.getPublisher(), expected.getPublisher(),
        "Returned incorrect DublinCore publisher");
    assertEquals(actual.getFormat(), expected.getFormat(),
        "Returned incorrect DublinCore format");
    compareDublinCoreDateProperties(actual, expected);
    assertEquals(actual.getCopyrightYear(), expected.getCopyrightYear(),
        "Returned incorrect DublinCore copyrightyear");
    assertEquals(actual.getConformsTo(), expected.getConformsTo(),
        "Returned incorrect DublinCore conformsTo");
    assertEquals(actual.getTitle(), expected.getTitle(),
        "Returned incorrect DublinCore Title");
    assertEquals(actual.getDescription(), expected.getDescription(),
        "Returned incorrect DublinCore description");
    if (expected.getCreators() != null) {
      assertEqualsNoOrder(actual.getCreators().toArray(), expected.getCreators().toArray(),
          "Returned incorrect creator set");
    }
    if (expected.getContributors() != null) {
      assertEqualsNoOrder(actual.getContributors().toArray(), expected.getContributors().toArray(),
          "Returned incorrect contributor set");
    }
    if (expected.getSubjects() != null) {
      assertEqualsNoOrder(actual.getSubjects().toArray(), expected.getSubjects().toArray(),
          "Returned incorrect subjects set");
    }
    if (expected.getSummary() != null) {
      assertEqualsNoOrder(actual.getSummary().toArray(), expected.getSummary().toArray(),
          "Returned incorrect summary set");
    }
    if (expected.getBibliographicCitation() != null) {
      compareCitations(actual.getBibliographicCitation(), expected.getBibliographicCitation(), compareUserProfileLists);
    }
    if (expected.getReferences() != null) {
      assertEquals(actual.getReferences().size(), expected.getReferences().size(), "Returned incorrect number of references");
      for (int i = 0; i < actual.getReferences().size(); i++) {
        compareCitations(actual.getReferences().get(i), expected.getReferences().get(i), compareUserProfileLists);
      }
    } else {
      assertTrue(actual.getReferences() == null || actual.getReferences().size() == 0,
          "Returned non-empty references when none were expected");
    }
  }

  @SuppressWarnings("unchecked")
  private void compareDublinCoreDateProperties(DublinCore actual, DublinCore expected) {
    String[] fieldNames = new String[]{"available", "issued", "submitted", "accepted", "created", "modified"};
    int i = 0;
    for (Pair<Date, Date> dates : new Pair[]{
        new Pair(actual.getAvailable(), expected.getAvailable()),
        new Pair(actual.getIssued(), expected.getIssued()),
        new Pair(actual.getSubmitted(), expected.getSubmitted()),
        new Pair(actual.getAccepted(), expected.getAccepted()),
        new Pair(actual.getCreated(), expected.getCreated()),
        new Pair(actual.getModified(), expected.getModified())
    }) {
      if (dates.getSecond() != null) {
        assertNotNull(dates.getFirst(), "Returned null '" + fieldNames[i] + "' date property");
        assertEquals(dateFormatter.format(dates.getFirst()), dateFormatter.format(dates.getSecond()),
            "Returned incorrect '" + fieldNames[i] + "' date property");
      }
      i++;
    }
  }

  protected void compareCitations(Citation actual, Citation expected, boolean compareAuthorsAndEditors) {
    assertNotNull(actual, "Returned null citation");
    assertEquals(actual.getKey(), expected.getKey(), "Returned incorrect citation Key");

    assertEquals(actual.getYear(), expected.getYear(), "Returned incorrect citation Year; key: " + expected.getKey());
    assertEquals(actual.getDisplayYear(), expected.getDisplayYear(), "Returned incorrect citation Display Year; key: " + expected.getKey());
    assertEquals(actual.getMonth(), expected.getMonth(), "Returned incorrect citation Month; key: " + expected.getKey());
    assertEquals(actual.getDay(), expected.getDay(), "Returned incorrect citation Day; key: " + expected.getKey());

    assertEquals(actual.getVolumeNumber(), expected.getVolumeNumber(), "Returned incorrect citation Volume Number; key: " + expected.getKey());
    assertEquals(actual.getVolume(), expected.getVolume(), "Returned incorrect citation Volume; key: " + expected.getKey());
    assertEquals(actual.getIssue(), expected.getIssue(), "Returned incorrect citation Issue; key: " + expected.getKey());
    assertEquals(actual.getTitle(), expected.getTitle(), "Returned incorrect citation Title; key: " + expected.getKey());
    assertEquals(actual.getPublisherLocation(), expected.getPublisherLocation(), "Returned incorrect citation Publisher Location; key: " + expected.getKey());
    assertEquals(actual.getPublisherName(), expected.getPublisherName(), "Returned incorrect citation Publisher name; key: " + expected.getKey());

    assertEquals(actual.getPages(), expected.getPages(), "Returned incorrect citation Page; key: " + expected.getKey());
    assertEquals(actual.getELocationId(), expected.getELocationId(), "Returned incorrect citation eLocationId; key: " + expected.getKey());

    assertEquals(actual.getJournal(), expected.getJournal(), "Returned incorrect citation Journal; key: " + expected.getKey());
    assertEquals(actual.getNote(), expected.getNote(), "Returned incorrect citation Note; key: " + expected.getKey());

    if (compareAuthorsAndEditors) {
      if (expected.getReferencedArticleEditors() != null) {
        assertNotNull(actual.getReferencedArticleEditors(), "Citation " + actual.getId() + " had null editors list when non-null was expected");
        assertEquals(actual.getReferencedArticleEditors().size(), expected.getReferencedArticleEditors().size(), "returned incorrect number of editors");
        for (int i = 0; i < actual.getReferencedArticleEditors().size(); i++) {
          CitedPerson actualUserProfile = actual.getReferencedArticleEditors().get(i);
          CitedPerson expectedUserProfile = expected.getReferencedArticleEditors().get(i);
          assertEquals(actualUserProfile.getFullName(), expectedUserProfile.getFullName(), "Editor had incorrect Real Name");
          assertEquals(actualUserProfile.getGivenNames(), expectedUserProfile.getGivenNames(), "Editor had incorrect given name");
          assertEquals(actualUserProfile.getSurnames(), expectedUserProfile.getSurnames(), "Editor had incorrect surname");
          assertFalse(actualUserProfile.getIsAuthor(), "Editor profile had true value indicating it was an author");
        }
      }

      if (expected.getReferencedArticleAuthors() != null) {
        assertNotNull(actual.getReferencedArticleAuthors(), "Citation " + actual.getId() + " had null authors list when non-null was expected");
        assertEquals(actual.getReferencedArticleAuthors().size(), expected.getReferencedArticleAuthors().size(), "Returned incorrect citation number of authors; key: " + expected.getKey());
        for (int i = 0; i < actual.getReferencedArticleAuthors().size(); i++) {
          CitedPerson actualUserProfile = actual.getReferencedArticleAuthors().get(i);
          CitedPerson expectedUserProfile = expected.getReferencedArticleAuthors().get(i);
          assertEquals(actualUserProfile.getFullName(), expectedUserProfile.getFullName(), "Editor had incorrect Real Name");
          assertEquals(actualUserProfile.getGivenNames(), expectedUserProfile.getGivenNames(), "Editor had incorrect given name");
          assertEquals(actualUserProfile.getSurnames(), expectedUserProfile.getSurnames(), "Editor had incorrect surname");
          assertTrue(actualUserProfile.getIsAuthor(), "Author profile didn't indicate that it was an author");
        }
      }
    }

    assertEquals(actual.getCollaborativeAuthors(), expected.getCollaborativeAuthors(),
        "returned incorrect collaborative authors");
    assertEquals(actual.getUrl(), expected.getUrl(), "Returned incorrect citation URL; key: " + expected.getKey());
    assertEquals(actual.getDoi(), expected.getDoi(), "Returned incorrect citation doi'; key: " + expected.getKey());
    assertEquals(actual.getSummary(), expected.getSummary(), "Returned incorrect citation Summary; key: " + expected.getKey());
    assertEquals(actual.getCitationType(), expected.getCitationType(), "Returned incorrect citation Citation Type; key: " + expected.getKey());

  }

  protected void compareRepresentations(Set<Representation> actual, Set<Representation> expected) {
    assertEquals(actual.size(), expected.size(), "Returned incorrect number of representations");
    //Sort representations so we can compare element by element
    List<Representation> actualRepresentations = new ArrayList<Representation>(actual);
    List<Representation> expectedRepresentations = new ArrayList<Representation>(expected);
    Collections.sort(actualRepresentations, new Comparator<Representation>() {
      @Override
      public int compare(Representation representation, Representation representation1) {
        return Long.valueOf(representation.getSize()).compareTo(representation1.getSize());
      }
    });
    Collections.sort(expectedRepresentations, new Comparator<Representation>() {
      @Override
      public int compare(Representation representation, Representation representation1) {
        return Long.valueOf(representation.getSize()).compareTo(representation1.getSize());
      }
    });
    for (int i = 0; i < actualRepresentations.size(); i++) {
      Representation actualRepresentation = actualRepresentations.get(i);
      Representation expectedRepresentation = expectedRepresentations.get(i);
      assertEquals(actualRepresentation.getContentType(), expectedRepresentation.getContentType(),
          "representation had incorrect content type");
      assertEquals(actualRepresentation.getName(), expectedRepresentation.getName(),
          "representation had incorrect name");
      assertEquals(actualRepresentation.getSize(), expectedRepresentation.getSize(),
          "representation had incorrect size");
    }

  }

  /**
   * Helper method to compare dates.  This compares down to the minute, and checks that the seconds are within 1, since
   * rounding can occur when storing to an hsql db
   *
   * @param actual   - the date from mysql to compare
   * @param expected - the date from topaz to compare
   */
  protected static void assertMatchingDates(Date actual, Date expected) {
    if (actual == null || expected == null) {
      assertTrue(actual == null && expected == null);
    } else {
      Calendar actualCal = new GregorianCalendar();
      actualCal.setTime(actual);
      Calendar expectedCal = new GregorianCalendar();
      expectedCal.setTime(expected);
      assertEquals(actualCal.get(Calendar.YEAR), expectedCal.get(Calendar.YEAR), "Dates didn't have matching years");
      assertEquals(actualCal.get(Calendar.MONTH), expectedCal.get(Calendar.MONTH), "dates didn't have matching months");
      assertEquals(actualCal.get(Calendar.DAY_OF_MONTH), expectedCal.get(Calendar.DAY_OF_MONTH), "dates didn't have matching days of month");
      assertEquals(actualCal.get(Calendar.DAY_OF_WEEK), expectedCal.get(Calendar.DAY_OF_WEEK), "dates didn't have matching days of week");
      assertEquals(actualCal.get(Calendar.HOUR), expectedCal.get(Calendar.HOUR), "dates didn't have matching hours");
      assertEquals(actualCal.get(Calendar.MINUTE), expectedCal.get(Calendar.MINUTE), "dates didn't have matching minutes");
      int secondMin = expectedCal.get(Calendar.SECOND) - 1;
      int secondMax = expectedCal.get(Calendar.SECOND) + 1;
      int actualSecond = actualCal.get(Calendar.SECOND);
      assertTrue(secondMin <= actualSecond && actualSecond <= secondMax,
          "date didn't have correct second; expected something in [" + secondMin + "," + secondMax +
              "]; but got " + actualSecond);
    }
  }

  protected void compareCategories(Article actual, Article expected) {
    assertNotNull(actual.getCategories(), "Returned article with null category set");
    assertEquals(actual.getCategories().size(), expected.getCategories().size(), "returned incorrect number of categories");
    //Put the results in order, so we can compare them element by element
    List<Category> actualCategories = new ArrayList<Category>(actual.getCategories());
    List<Category> expectedCategories = new ArrayList<Category>(expected.getCategories());
    Collections.sort(actualCategories, new Comparator<Category>() {
      @Override
      public int compare(Category category, Category category1) {
        return category.getMainCategory().compareTo(category1.getMainCategory());
      }
    });
    Collections.sort(expectedCategories, new Comparator<Category>() {
      @Override
      public int compare(Category category, Category category1) {
        return category.getMainCategory().compareTo(category1.getMainCategory());
      }
    });

    for (int i = 0; i < actualCategories.size(); i++) {
      Category actualCategory = actualCategories.get(i);
      Category expectedCategory = expectedCategories.get(i);
      assertEquals(actualCategory.getMainCategory(), expectedCategory.getMainCategory(),
          "Category had incorrect 'Main Category'; id: " + actualCategory.getId());
      assertEquals(actualCategory.getSubCategory(), expectedCategory.getSubCategory(),
          "Category had incorrect 'Sub Category'; id: " + actualCategory.getId());
    }
  }

  protected void compareArticles(Article actualArticle, Article expectedArticle) {
    assertNotNull(actualArticle, "returned null article");
    assertEquals(actualArticle.getId(), expectedArticle.getId(), "Article had incorrect id");
    assertNotNull(actualArticle.getDublinCore(), "returned article with null dublin core");
    compareDublinCore(actualArticle.getDublinCore(), expectedArticle.getDublinCore(), false);

    Set<URI> types = actualArticle.getArticleType();
    assertNotNull(types, "returned article with null Article Type set");
    assertEquals(types, expectedArticle.getArticleType(), "returned incorrect article types");

    assertEquals(actualArticle.geteIssn(), expectedArticle.geteIssn(), "returned incorrect eIssn");
    compareCategories(actualArticle, expectedArticle);
    compareRepresentations(actualArticle.getRepresentations(), expectedArticle.getRepresentations());

    if (expectedArticle.getAuthors() != null) {
      assertNotNull(actualArticle.getAuthors(), "returned null author list");
      assertEquals(actualArticle.getAuthors().size(), expectedArticle.getAuthors().size(),
          "returned incorrect number of authors");
      for (int i = 0; i < expectedArticle.getAuthors().size(); i++) {
        compareArticleContributors(actualArticle.getAuthors().get(i), expectedArticle.getAuthors().get(i));
      }
    }
    if (expectedArticle.getEditors() != null) {
      assertNotNull(actualArticle.getEditors(), "returned null editor list");
      assertEquals(actualArticle.getEditors().size(), expectedArticle.getEditors().size(),
          "returned incorrect number of editors");
      for (int i = 0; i < expectedArticle.getEditors().size(); i++) {
        compareArticleContributors(actualArticle.getEditors().get(i), expectedArticle.getEditors().get(i));
      }
    }
  }

  private void compareArticleContributors(ArticleContributor actual, ArticleContributor expected) {
    assertEquals(actual.getFullName(), expected.getFullName(), "Author / editor profile had incorrect full name");
    assertEquals(actual.getSuffix(), expected.getSuffix(), "Author / editor profile had incorrect suffix");
    assertEquals(actual.getGivenNames(), expected.getGivenNames(), "Author / editor profile had incorrect given name(s)");
    assertEquals(actual.getSurnames(), expected.getSurnames(), "Author / editor profile had incorrect surname(s)");
  }

}
