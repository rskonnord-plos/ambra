package org.ambraproject.migrationtests;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.Representation;

import java.util.Iterator;

import static org.testng.Assert.*;

/**
 * @author Alex Kudlick Date: Mar 25, 2011
 *         <p/>
 *         org.ambraproject.topazmigration
 */
public class RepresentationMigrationTest extends BaseMigrationTest {

  private static final int INCREMENT_SIZE = 100;

  @DataProvider(name = "representations")
  public Iterator<Object[]> userProfiles() {
    return new MigrationDataIterator(this, Representation.class,INCREMENT_SIZE);
  }

  @Test(dataProvider = "representations")
  public void diffRepresentations(Representation mysqlRepresentation, Representation topazRepresentation) {
    assertNotNull(topazRepresentation, "Topaz returned a null representation");
    assertNotNull(mysqlRepresentation, "Mysql didn't return a representation for id: " + topazRepresentation.getId());

    assertEquals(mysqlRepresentation.getName(), topazRepresentation.getName(),
        "MYSQL and topaz representations didn't have matching names; id: " + mysqlRepresentation.getId());
    assertEquals(mysqlRepresentation.getContentType(), topazRepresentation.getContentType(),
        "MYSQL and topaz representations didn't have matching content types; id: " + mysqlRepresentation.getId());
    assertEquals(mysqlRepresentation.getSize(), topazRepresentation.getSize(),
        "MYSQL and topaz representations didn't have matching sizes; id: " + mysqlRepresentation.getId());
    assertMatchingDates(topazRepresentation.getLastModified(), mysqlRepresentation.getLastModified(),
        "MYSQL and topaz representations didn't have matching last modified dates; id: " + topazRepresentation.getId());

    //Just compare Id's of the ObjectInfo property
    if (mysqlRepresentation.getObject() == null) {
      assertNull(topazRepresentation.getObject(),
          "Mysql representation had a null Object property, but Topaz's was non-null; id: " + mysqlRepresentation.getId());
    } else if (topazRepresentation.getObject() == null) {
      assertNull(mysqlRepresentation,
          "Topaz representation had a null Object property, but Mysql's wasn't; id: " + mysqlRepresentation.getId());
    } else {
      assertEquals(mysqlRepresentation.getObject().getId(), topazRepresentation.getObject().getId(),
          "Mysql and Topaz representation didn't have matching Object properties; id: " + mysqlRepresentation.getId());      
    }
  }
}
