package org.ambraproject.topazmigration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topazproject.ambra.models.*;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.query.Results;

import java.net.URI;
import java.util.*;

/**
 * Iterator to be used in DataProvider methods.  Loads the objects to tests in blocks of a specified number, and returns
 * them in an object array with the mysql object first, the topaz object second
 *
 * @author Alex Kudlick Date: 5/19/11
 *         <p/>
 *         org.ambraproject.topazmigration
 */
@SuppressWarnings("unchecked")
public class MigrationDataIterator implements Iterator<Object[]> {
  /**
   * System property to specify a limit to the number of objects to test
   */
  public static final String TEST_LIMIT_PROPERTY = "topaz.test.limit";
  public static final String TEST_LIMIT_ENV_VARIABLE = "TOPAZ_TEST_LIMIT";
  private static final Logger log = LoggerFactory.getLogger(MigrationDataIterator.class);
  private static final Set<Class> entitiesWithStringId = new HashSet<Class>();

  static {
    entitiesWithStringId.add(AnnotationBlob.class);
    entitiesWithStringId.add(DublinCore.class);
    entitiesWithStringId.add(RatingContent.class);
    entitiesWithStringId.add(RatingSummaryContent.class);
    entitiesWithStringId.add(ReplyBlob.class);
    entitiesWithStringId.add(TrackbackContent.class);
    entitiesWithStringId.add(Representation.class);
  }

  //block-specific params
  private Iterator<Object[]> objects;
  private int offset = 0;
  private boolean lastBlock = false;
  //immutable params
  private final int incrementSize;
  private final Class clazz;
  private final BaseMigrationTest parent;
  private final boolean stringIds;

  /**
   * Construct a new Iterator to chunk over data to test.  Optionally, you can set the system property {@link
   * MigrationDataIterator#TEST_LIMIT_PROPERTY}=<var>LIMIT</var> or set the system environment variable {@link
   * MigrationDataIterator#TEST_LIMIT_ENV_VARIABLE}=<var>LIMIT</var> to limit the number of objects tested to <var>LIMIT</var>.
   *
   * @param parent        - the test object using this iterator
   * @param clazz         - the class of objects to load up
   * @param incrementSize - the number of objects to load in each chunk
   */
  public MigrationDataIterator(BaseMigrationTest parent, Class clazz, int incrementSize) {
    if (System.getProperty(TEST_LIMIT_PROPERTY) != null || System.getenv().containsKey(TEST_LIMIT_ENV_VARIABLE)) {
      //if we've been passed a limit, just do it in one block
      this.incrementSize = System.getProperty(TEST_LIMIT_PROPERTY) != null ?
          Integer.valueOf(System.getProperty(TEST_LIMIT_PROPERTY))
          : Integer.valueOf(System.getenv().get(TEST_LIMIT_ENV_VARIABLE));
      lastBlock = true;
      log.info("Starting up data iterator with limit of " + this.incrementSize + " " + clazz.getSimpleName() + "s");
    } else {
      log.info("Starting up data iterator to test all " + clazz.getSimpleName() + "s");
      this.incrementSize = incrementSize;
    }
    this.parent = parent;
    this.clazz = clazz;
    this.stringIds = entitiesWithStringId.contains(clazz);
    loadNextBlock();
  }

  /**
   * Helper method to load the next block of ids and objects.  This increments the offset and checks to see if we're in
   * the last block
   */
  private void loadNextBlock() {
    log.info("testing next batch of " + clazz.getSimpleName() + "s (" + offset + " to " + (offset + incrementSize) + ")");
    List<Object[]> objectList = new ArrayList<Object[]>(incrementSize);

    Results results = null;
    int retry = 5;
    while (retry > 0) {
      try {
        parent.restartSessions();
        results = parent.topazSession.createQuery(
            "select c.id from " + clazz.getSimpleName() + " c limit " + incrementSize + " offset " + offset + ";"
        ).execute();
        break;
      } catch (OtmException e) {
        retry = retry - 1;
      }
    }

    int count = 0;
    while (results.next()) {
      count++;
      Object id = results.get(0);
      Object topazObject;
      Object mysqlObject;

      //load up the objects
      if (stringIds) {
        topazObject = parent.loadTopazObject(id.toString(), clazz);
        mysqlObject = parent.loadMySQLObject(id.toString(), clazz);
      } else {
        topazObject = parent.loadTopazObject((URI) id, clazz);
        mysqlObject = parent.loadMySQLObject((URI) id, clazz);
      }
      objectList.add(new Object[]{mysqlObject, topazObject});
    }
    //if we loaded less ids than the limit, we must be on the last block
    if (count < incrementSize) {
      lastBlock = true;
    }
    offset += incrementSize;
    objects = objectList.iterator();
  }

  public boolean hasNext() {
    if (objects.hasNext()) {
      return true; //still more objects in this block
    } else if (!lastBlock) {
      loadNextBlock(); //out of objects in the block, but we've got more blocks to do
      return true;
    } else {
      log.info("finished testing " + clazz.getSimpleName() + "s");
      return false; //all done!
    }
  }

  public Object[] next() {
    return objects.next();
  }

  public void remove() {
    objects.remove();
  }
}
