/*
 * $HeadURL:
 * $Id:
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *
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
 * limitations under the License.
 */
package org.ambraproject.topazmigration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.*;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;

import static org.testng.Assert.*;

/**
 * Test for the topazmigration of {@link org.topazproject.ambra.models.Article} to a mysql database
 *
 * @author Alex Kudlick
 * @author Joe Osowski
 *
 * org.plos.topazMigration
 *
 */
public class ArticleMigrationTest extends BaseMigrationTest {
  private static final Logger log = LoggerFactory.getLogger(ArticleMigrationTest.class);
  //Number of articles to load up at once (want to periodically restart the topaz session, and a low increment forces that)
  private static final int INCREMENT_SIZE = 50;

  @DataProvider(name = "Articles")
  public Iterator<Object[]> requestArticles(Method testMethod) throws Exception {
    assertTrue(restartSessions());
    log.info("Running test method " + testMethod.getName());
    if (testMethod.getName().equals("compareObjectInfoProperties")
        || testMethod.getName().equals("compareParts")) {
      return new MigrationDataIterator(this, Article.class, 10);
    }
    return new MigrationDataIterator(this, Article.class, INCREMENT_SIZE);
  }

  @Test(dataProvider = "Articles", singleThreaded = false)
  public void diffArticles(Article mySqlArticle, Article topazArticle) {
    assertNotNull(topazArticle,"Topaz failed to return a(n) article; check the migration logs for more information");
    assertNotNull(mySqlArticle,"Mysql didn't return an article for id: " + topazArticle.getId());

    topazArticle = loadTopazObject(topazArticle.getId(), Article.class);

    assertMatchingSets(mySqlArticle.getArticleType(),topazArticle.getArticleType(),
        "Topaz and MySQL have differing article Types; article: " + mySqlArticle.getId());

    assertEquals(mySqlArticle.getState(), topazArticle.getState(),
        "Topaz and MySQL have differing article states; article: " + mySqlArticle.getId());
    assertEquals(mySqlArticle.getArchiveName(), topazArticle.getArchiveName(),
        "Topaz and MySQL have differing archive names; article: " + mySqlArticle.getId());
  }

  @Test(dataProvider = "Articles", singleThreaded = false)
  public void compareParts(Article mySqlArticle, Article topazArticle) {
    assertNotNull(topazArticle,"Topaz failed to return a(n) article; check the migration logs for more information");
    assertNotNull(mySqlArticle,"Mysql didn't return an article for id: " + topazArticle.getId());

    topazArticle = loadTopazObject(topazArticle.getId(), Article.class);

    List<ObjectInfo> mySqlParts = mySqlArticle.getParts();
    List<ObjectInfo> topazParts = topazArticle.getParts();

    if (mySqlParts.size() == 0) {
      assertTrue(topazParts == null || topazParts.size() == 0, "Topaz and MySQL have differing article parts;" +
          " article: " + mySqlArticle.getId());
    } else {
      for (int a = 0; a < mySqlParts.size(); a++) {
        URI id1 = mySqlParts.get(a).getId();

        for (int b = 0; b < topazParts.size(); b++) {
          URI id2 = topazParts.get(b).getId();

          if (id1.equals(id2)) {
            compareObjectInfoProperties(mySqlParts.get(a), topazParts.get(b));

            //Found matches, remove them from the lists
            mySqlParts.remove(a);
            topazParts.remove(b);

            //Reset counter to -1 as the last step of the loop increments +1
            a = -1;
            //Don't bother with the reset of loop b.
            break;
          }
        }
      }

      //If either list has any values left, there was an issue
      assertTrue(mySqlParts.size() == 0, "Topaz and MySQL have differing article parts;" +
          " article: " + mySqlArticle.getId());
      assertTrue(topazParts.size() == 0, "Topaz and MySQL have differing article parts;" +
          " article: " + mySqlArticle.getId());
    }
  }

  @Test(dataProvider = "Articles")
  public void compareCategories(Article sa, Article ta)
  {
    assertNotNull(ta,"Topaz failed to return a(n) article; check the migration logs for more information");
    assertNotNull(sa,"Mysql didn't return an article for id: " + ta.getId());

    ArrayList<Category> saCategories = new ArrayList<Category>(sa.getCategories());
    ArrayList<Category> taCategories = new ArrayList<Category>(ta.getCategories());

    for(int a = 0; a < saCategories.size(); a++)
    {
      URI id1 = saCategories.get(a).getId();

      for(int b = 0; b < taCategories.size(); b++)
      {
        URI id2 = taCategories.get(b).getId();

        if(id1.equals(id2)) {
          assertEquals(saCategories.get(a).getMainCategory(),taCategories.get(b).getMainCategory(),
            "Topaz and MySQL have differing categories; article: " + sa.getId());

          assertEquals(saCategories.get(a).getSubCategory(),taCategories.get(b).getSubCategory(),
            "Topaz and MySQL have differing categories; article: " + sa.getId());

          //Found matches, lets not compare them again
          saCategories.remove(a);
          taCategories.remove(b);

          //Reset counter to -1 as the last step of the loop increments +1
          a = -1;
          //Don't bother with the reset of loop b.
          break;
        }
      }
    }

    //If either list has any values left, there was an issue
    assertTrue(saCategories.size() == 0, "Topaz and MySQL have differing categories; article: " + sa.getId());
    assertTrue(taCategories.size() == 0, "Topaz and MySQL have differing categories; article: " + sa.getId());
  }

  @Test(dataProvider = "Articles")
  public void compareRelatedArticles(Article sa, Article ta)
  {
    assertNotNull(ta,"Topaz failed to return a(n) article; check the migration logs for more information");
    assertNotNull(sa,"Mysql didn't return an article for id: " + ta.getId());

    ArrayList<RelatedArticle> mySqlRels = new ArrayList<RelatedArticle>(sa.getRelatedArticles());

    if(mySqlRels.size() == 0)
    {
      assertNull(ta.getRelatedArticles(), "Topaz and MySQL have differing related articles; article: " + sa.getId());
    } else {
      ArrayList<RelatedArticle> topazRels = new ArrayList<RelatedArticle>(ta.getRelatedArticles());

      for(int a = 0; a < mySqlRels.size(); a++)
      {
        URI id1 = mySqlRels.get(a).getId();

        for(int b = 0; b < topazRels.size(); b++)
        {
          URI id2 = topazRels.get(b).getId();

          if(id1.equals(id2)) {
            assertEquals(mySqlRels.get(a).getArticle(), topazRels.get(b).getArticle(),
              "Topaz and MySQL have differing related articles article; id: " + sa.getId());
            assertEquals(mySqlRels.get(a).getId(), topazRels.get(b).getId(),
              "Topaz and MySQL have differing related articles ID; article: " + sa.getId());
            assertEquals(mySqlRels.get(a).getRelationType(), topazRels.get(b).getRelationType(),
              "Topaz and MySQL have differing related articles type; article: " + sa.getId());

            //Found matches, lets not compare them again
            mySqlRels.remove(a);
            topazRels.remove(b);

            //Reset counter to -1 as the last step of the loop increments +1
            a = -1;
            //Don't bother with the reset of loop b.
            break;
          }
        }
      }

      //If either list has any values left, there was an issue
      assertTrue(mySqlRels.size() == 0, "Topaz and MySQL have differing related articles;" +
          " article: " + sa.getId());
      assertTrue(topazRels.size() == 0, "Topaz and MySQL have differing related articles;" +
          " article: " + sa.getId());
    }
  }

  private static void compareLicenses(DublinCore mySqlDCore, DublinCore topazDCore, String msg)
  {
    ArrayList<License> mySqlLicenses = new ArrayList<License>(mySqlDCore.getLicense());

    if(mySqlDCore.getLicense().size() == 0) {
      assertNull(topazDCore.getLicense(), msg + " have differing dublincore licenses; id: " + mySqlDCore.getIdentifier());
    } else {
      ArrayList<License> topazLicenses = new ArrayList<License>(topazDCore.getLicense());

      for(int a = 0; a < mySqlLicenses.size(); a++)
      {
        URI id1 = mySqlLicenses.get(a).getId();

        for(int b = 0; b < topazLicenses.size(); b++)
        {
          URI id2 = topazLicenses.get(b).getId();

          if(id1.equals(id2)) {
            //URI is the only property of this object (nothing to compare besides ID)

            //Found matches, remove them from the lists
            mySqlLicenses.remove(a);
            topazLicenses.remove(b);

            //Reset counter to -1 as the last step of the loop increments +1
            a = -1;
            //Don't bother with the reset of loop b.
            break;
          }
        }
      }

      //If either list has any values left, there was an issue
      assertTrue(mySqlLicenses.size() == 0, msg + " have differing dublincore licenses; id: " + mySqlDCore.getIdentifier());
      assertTrue(topazLicenses.size() == 0, msg + " have differing dublincore licenses; id: " + mySqlDCore.getIdentifier());
    }
  }

  @Test(dataProvider = "Articles")
  public void compareObjectInfoProperties(ObjectInfo mysqlObjectInfo, ObjectInfo topazObjectInfo)
  {
    assertNotNull(topazObjectInfo,"Topaz failed to return a(n) article; check the migration logs for more information");
    assertNotNull(mysqlObjectInfo,"Mysql didn't return an article for id: " + topazObjectInfo.getId());
    //Reload object to fix lazy load issues
    topazObjectInfo = loadTopazObject(topazObjectInfo.getId(), ObjectInfo.class);

    assertEquals(mysqlObjectInfo.getId(), topazObjectInfo.getId(),
        "Topaz and MySQL have differing objectInfo ID");

    compareDublinCore(mysqlObjectInfo.getDublinCore(), topazObjectInfo.getDublinCore(),
        "Mysql and Topaz");

    //Don't introspect articles, just make sure the ID is the same
    if (mysqlObjectInfo.getIsPartOf() == null) {
      assertNull(topazObjectInfo.getIsPartOf(),
          "Topaz and MySQL have differing objectInfo isPartOf; id: " + mysqlObjectInfo.getId());
    } else if (topazObjectInfo.getIsPartOf() == null) {
      assertNull(mysqlObjectInfo.getIsPartOf(),
          "Topaz and MySQL have differing objectInfo isPartOf; id: " + mysqlObjectInfo.getId());
    } else {
      assertEquals(mysqlObjectInfo.getIsPartOf().getId(), topazObjectInfo.getIsPartOf().getId(),
          "Topaz and MySQL have differing objectInfo isPartOf; id: " + mysqlObjectInfo.getId());
    }

    //Don't bother comparing properties of representations (Representation test will do that),
    //  just check that their ids are equal
    assertMatchingSets(getRepresentationIds(mysqlObjectInfo.getRepresentations()),
        getRepresentationIds(topazObjectInfo.getRepresentations()),
        "Mysql and Topaz had differing Representations for ObjectInfos; id: " + mysqlObjectInfo.getId());

    assertEquals(mysqlObjectInfo.getContextElement(), topazObjectInfo.getContextElement(),
        "Topaz and MySQL have differing representation objectInfo contextelements; id: " + mysqlObjectInfo.getId());

    assertEquals(mysqlObjectInfo.geteIssn(), topazObjectInfo.geteIssn(),
        "Topaz and MySQL have differing objectInfo eIssn; id: " + mysqlObjectInfo.getId());
  }

  private Set<String> getRepresentationIds(Set<Representation> representations) {
    HashSet<String> ids = new HashSet<String>();
    if (representations != null) {
      for (Representation rep : representations) {
        ids.add(rep.getId());
      }
    }
    return ids;
  }

  public static void compareDublinCore(DublinCore mysqlDCore, DublinCore topazDCore, String msg)
  {
    assertEquals(mysqlDCore.getTitle(), topazDCore.getTitle(),
      msg + " have differing DublinCore Tiles; id: " + mysqlDCore.getIdentifier());
    assertEquals(mysqlDCore.getDescription(), topazDCore.getDescription(),
      msg + " have differing DublinCore descriptions; id: " + mysqlDCore.getIdentifier());
    assertMatchingSets(mysqlDCore.getCreators(),topazDCore.getCreators(),
        "Mysql and topaz had differing creator sets for Dublin Core objects; id: " + mysqlDCore.getIdentifier());
    assertEquals(mysqlDCore.getDate(), topazDCore.getDate(),
      msg + " have differing DublinCore dates; id: " + mysqlDCore.getIdentifier());
    if (topazDCore.getIdentifier() != null) { //Sometimes topaz dub cores have null identifiers
      assertEquals(mysqlDCore.getIdentifier(), topazDCore.getIdentifier(),
        msg + " have differing DublinCore identifiers");
    }
    assertEquals(mysqlDCore.getRights(), topazDCore.getRights(),
      msg + " have differing DublinCore rights; id: " + mysqlDCore.getIdentifier());

    assertEquals(mysqlDCore.getType(), topazDCore.getType(),
      msg + " have differing DublinCore types; id: " + mysqlDCore.getIdentifier());
    assertMatchingSets(mysqlDCore.getContributors(),topazDCore.getContributors(),
        "Mysql and Topaz had differing contributor sets for Dublin Core objects; id: " + mysqlDCore.getIdentifier());
    assertMatchingSets(mysqlDCore.getSubjects(),topazDCore.getSubjects(),
        "Mysql and Topaz had differing subjects for Dublin Core objects; id: " + mysqlDCore.getIdentifier());
    assertEquals(mysqlDCore.getLanguage(), topazDCore.getLanguage(),
      msg + " have differing DublinCore languages; id: " + mysqlDCore.getIdentifier());
    assertEquals(mysqlDCore.getPublisher(), topazDCore.getPublisher(),
      msg + " have differing DublinCore publishers; id: " + mysqlDCore.getIdentifier());
    assertEquals(mysqlDCore.getFormat(), topazDCore.getFormat(),
      msg + " have differing DublinCore formats; id: " + mysqlDCore.getIdentifier());

    //TODO: Flesh out when more data is available (Source is of type 'Object')
    assertEquals(mysqlDCore.getSource(), topazDCore.getSource(),
      msg + " have differing DublinCore sources; id: " + mysqlDCore.getIdentifier());

    assertMatchingDates(mysqlDCore.getAvailable(), topazDCore.getAvailable(),
      msg + " have differing DublinCore available; id: " + mysqlDCore.getIdentifier());
    assertMatchingDates(mysqlDCore.getIssued(), topazDCore.getIssued(),
      msg + " have differing DublinCore issued; id: " + mysqlDCore.getIdentifier());
    assertMatchingDates(mysqlDCore.getSubmitted(), topazDCore.getSubmitted(),
      msg + " have differing DublinCore submitted; id: " + mysqlDCore.getIdentifier());
    assertMatchingDates(mysqlDCore.getAccepted(), topazDCore.getAccepted(),
      msg + " have differing DublinCore accepted; id: " + mysqlDCore.getIdentifier());

    assertEquals(mysqlDCore.getCopyrightYear(), topazDCore.getCopyrightYear(),
      msg + " have differing DublinCore copyrightyear; id: " + mysqlDCore.getIdentifier());

    assertMatchingSets(mysqlDCore.getSummary(), topazDCore.getSummary(),
        "Mysql and topaz had differing summary sets for Dublin Core objects; id: " + mysqlDCore.getIdentifier());

    if(mysqlDCore.getBibliographicCitation() == null) {
      assertNull(topazDCore.getBibliographicCitation(), msg + " have differing DublinCore bibcitations; id: " + mysqlDCore.getIdentifier());
    } else if (topazDCore.getBibliographicCitation() == null) {
      assertNull(mysqlDCore.getBibliographicCitation(), msg + " have differing DublinCore bibcitations; id: " + mysqlDCore.getIdentifier());
    } else {
      //Don't bother checking citation content (the content should be checked by the CitationMigrationTest
      //But do check that the URI's are the same
      assertEquals(mysqlDCore.getBibliographicCitation().getId(), topazDCore.getBibliographicCitation().getId(),
          msg + " have differing DublinCore bibcitations; id: " + mysqlDCore.getIdentifier());
    }

    assertMatchingDates(mysqlDCore.getCreated(), topazDCore.getCreated(),
      msg + " have differing DublinCore created; id: " + mysqlDCore.getIdentifier());

    compareLicenses(mysqlDCore, topazDCore, msg);

    assertEquals(mysqlDCore.getModified(), topazDCore.getModified(),
      msg + " have differing DublinCore modified; id: " + mysqlDCore.getIdentifier());

    //Don't bother comparing properties of References (Citation Test will do that), just check that the ids are the same
    assertMatchingLists(getCitationIds(mysqlDCore.getReferences()), getCitationIds(topazDCore.getReferences()),
        "Mysql and Topaz had differing references for Dublin Core objects; id: " + mysqlDCore.getIdentifier());

    assertEquals(mysqlDCore.getConformsTo(), topazDCore.getConformsTo(),
      msg + " have differing DublinCore conformsTo; id: " + mysqlDCore.getIdentifier());
  }

  private static List<URI> getCitationIds(List<Citation> references) {
    List<URI> ids = new ArrayList<URI>();
    if (references != null) {
      try {
        for (Citation citation : references) {
          ids.add(citation.getId());
        }
      } catch (Exception e) {
        //Ignore
      }
    }
    return ids;
  }
}
