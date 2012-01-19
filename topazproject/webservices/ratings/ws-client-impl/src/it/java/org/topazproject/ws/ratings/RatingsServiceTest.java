/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.ratings;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

import org.topazproject.ws.users.DuplicateAuthIdException;
import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.ws.users.UserAccounts;
import org.topazproject.ws.users.UserAccountsClientFactory;

/**
 *
 */
public class RatingsServiceTest extends TestCase {
  private Ratings      service;
  private UserAccounts userService;
  private String[]     userId = new String[5];

  public RatingsServiceTest(String testName) {
    super(testName);
  }

  protected void setUp()
      throws MalformedURLException, ServiceException, DuplicateAuthIdException, RemoteException {
    String uri = "http://localhost:9997/ws-ratings/services/RatingsServicePort";
    service = RatingsClientFactory.create(uri);

    uri = "http://localhost:9997/ws-users/services/UserAccountsServicePort";
    userService = UserAccountsClientFactory.create(uri);

    // create a user
    userId[0] = userService.createUser("musterAuth");
  }

  protected void tearDown() throws RemoteException {
    for (int idx = 0; idx < userId.length; idx++) {
      if (userId[idx] == null)
        continue;

      try {
        service.setRatings(null, userId[idx], null, null);
      } catch (NoSuchUserIdException nsuie) {
        // looks like it was clean
      }

      try {
        userService.deleteUser(userId[idx]);
      } catch (NoSuchUserIdException nsuie) {
        // looks like it was clean
      }
    }
  }

  /**
   * Test NoSuchUserIdException.
   */
  public void testNSIE() throws RemoteException, NoSuchUserIdException, IOException {
    boolean gotE = false;
    try {
      service.setRatings("testApp1", "muster42", "foo:bar", null);
    } catch (RemoteException re) {
      if (re.getMessage().indexOf("IllegalArgumentException") < 0)
        throw re;
      gotE = true;
    }
    assertTrue("Failed to get expected IllegalArgumentException", gotE);

    gotE = false;
    try {
      service.getRatings("testApp1", "muster42", "foo:bar");
    } catch (RemoteException re) {
      if (re.getMessage().indexOf("IllegalArgumentException") < 0)
        throw re;
      gotE = true;
    }
    assertTrue("Failed to get expected IllegalArgumentException", gotE);

    gotE = false;
    try {
      service.setRatings("testApp1", "foo:muster42", "foo:bar", null);
    } catch (NoSuchUserIdException nsuie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);

    gotE = false;
    try {
      service.getRatings("testApp1", "foo:muster42", "foo:bar");
    } catch (NoSuchUserIdException nsuie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);
  }

  public void testBasicRatings() throws RemoteException, NoSuchUserIdException, IOException {
    ObjectRating[] ratings, got, r2, exp;

    // test null ratings
    ratings = service.getRatings("testApp1", userId[0], "foo:bar");
    assertNull("non-null ratings, got " + ratings, ratings);

    ratings = service.getRatings(null, userId[0], "foo:bar");
    assertNull("non-null ratings, got " + ratings, ratings);

    service.setRatings("testApp1", userId[0], "foo:bar", null);

    ratings = service.getRatings("testApp1", userId[0], "foo:bar");
    assertNull("non-null ratings, got " + ratings, ratings);

    // test 0-length ratings
    service.setRatings("testApp1", userId[0], "foo:bar", new ObjectRating[0]);
    ratings = service.getRatings(null, userId[0], "foo:bar");
    assertNull("non-null ratings, got " + ratings, ratings);

    // test simple ratings
    ratings = new ObjectRating[3];
    ratings[0] = new ObjectRating();
    ratings[1] = new ObjectRating();
    ratings[2] = new ObjectRating();

    ratings[0].setCategory("length");
    ratings[0].setRating(1);
    ratings[1].setCategory("color");
    ratings[1].setRating(2.61f);
    ratings[2].setCategory("wellness");
    ratings[2].setRating(1.0f/3.0f);

    service.setRatings("testApp1", userId[0], "foo:bar", ratings);

    got = service.getRatings("testApp1", userId[0], "foo:bar");

    compare(got, ratings);

    ratings = new ObjectRating[2];
    ratings[0] = new ObjectRating();
    ratings[1] = new ObjectRating();

    ratings[0].setCategory("style");
    ratings[0].setRating(42);
    ratings[1].setCategory("geekiness");
    ratings[1].setRating(-3.117f);

    service.setRatings("testApp1", userId[0], "foo:bar", ratings);

    got = service.getRatings("testApp1", userId[0], "foo:bar");
    compare(got, ratings);

    // test app-ids
    got = service.getRatings(null, userId[0], "foo:bar");
    compare(got, ratings);

    r2 = new ObjectRating[3];
    r2[0] = new ObjectRating();
    r2[1] = new ObjectRating();
    r2[2] = new ObjectRating();

    r2[0].setCategory("obsequiousness");
    r2[0].setRating(5.2f);
    r2[1].setCategory("subservience");
    r2[1].setRating(-3);
    r2[2].setCategory("background-color");
    r2[2].setRating(9);
    service.setRatings("testApp2", userId[0], "foo:bar", r2);

    got = service.getRatings("testApp1", userId[0], "foo:bar");
    compare(got, ratings);

    got = service.getRatings("testApp2", userId[0], "foo:bar");
    compare(got, r2);

    got = service.getRatings(null, userId[0], "foo:bar");
    exp = new ObjectRating[5];
    exp[0] = ratings[0];
    exp[1] = ratings[1];
    exp[2] = r2[0];
    exp[3] = r2[1];
    exp[4] = r2[2];
    compare(got, exp);

    service.setRatings("testApp1", userId[0], "foo:bar", null);

    got = service.getRatings("testApp1", userId[0], "foo:bar");
    assertNull("non-null ratings, got " + got, got);

    got = service.getRatings("testApp2", userId[0], "foo:bar");
    compare(got, r2);

    got = service.getRatings(null, userId[0], "foo:bar");
    compare(got, r2);

    // test objects
    got = service.getRatings(null, userId[0], "foo:ji");
    assertNull("non-null ratings, got " + got, got);

    service.setRatings("testApp2", userId[0], "foo:ji", ratings);

    got = service.getRatings("testApp2", userId[0], "foo:ji");
    compare(got, ratings);

    got = service.getRatings("testApp2", userId[0], "foo:bar");
    compare(got, r2);

    got = service.getRatings(null, userId[0], "foo:ji");
    compare(got, ratings);

    got = service.getRatings(null, userId[0], "foo:bar");
    compare(got, r2);

    service.setRatings("testApp2", userId[0], "foo:bar", null);

    got = service.getRatings("testApp2", userId[0], "foo:bar");
    assertNull("non-null ratings, got " + got, got);

    got = service.getRatings("testApp2", userId[0], "foo:ji");
    compare(got, ratings);

    got = service.getRatings(null, userId[0], "foo:bar");
    assertNull("non-null ratings, got " + got, got);

    service.setRatings("testApp2", userId[0], "foo:bar", r2);

    got = service.getRatings("testApp2", userId[0], "foo:ji");
    compare(got, ratings);

    got = service.getRatings("testApp2", userId[0], "foo:bar");
    compare(got, r2);

    service.setRatings("testApp2", userId[0], null, null);

    got = service.getRatings(null, userId[0], "foo:bar");
    assertNull("non-null ratings, got " + got, got);

    got = service.getRatings(null, userId[0], "foo:ji");
    assertNull("non-null ratings, got " + got, got);

    // cleanup
    service.setRatings(null, userId[0], "foo:bar", null);
    service.setRatings(null, userId[0], "foo:ji", null);
  }

  public void testRatingStats()
      throws RemoteException, NoSuchUserIdException, DuplicateAuthIdException, IOException {
    ObjectRatingStats[] got, exp;
    ObjectRating[]      ratings;

    // stuff should be empty
    got = service.getRatingStats("testApp2", "foo:ji");
    assertNull("non-null stats, got " + got, got);

    got = service.getRatingStats("testApp1", "foo:ji");
    assertNull("non-null stats, got " + got, got);

    got = service.getRatingStats(null, "foo:ji");
    assertNull("non-null stats, got " + got, got);

    got = service.getRatingStats(null, "foo:bar");
    assertNull("non-null stats, got " + got, got);

    // set one rating
    ratings = new ObjectRating[3];
    ratings[0] = new ObjectRating();
    ratings[1] = new ObjectRating();
    ratings[2] = new ObjectRating();

    ratings[0].setCategory("length");
    ratings[0].setRating(1);
    ratings[1].setCategory("color");
    ratings[1].setRating(2.61f);
    ratings[2].setCategory("wellness");
    ratings[2].setRating(1.0f/3.0f);

    service.setRatings("testApp1", userId[0], "foo:bar", ratings);

    got = service.getRatingStats("testApp1", "foo:bar");
    compare(got, calcStats(new ObjectRating[][] { ratings }));

    // add another rating
    userId[1] = userService.createUser("musterAuth2");

    ObjectRating[] r2 = new ObjectRating[3];
    r2[0] = new ObjectRating();
    r2[1] = new ObjectRating();
    r2[2] = new ObjectRating();

    r2[0].setCategory("length");
    r2[0].setRating(4.2f);
    r2[1].setCategory("color");
    r2[1].setRating(0.9f);
    r2[2].setCategory("wellness");
    r2[2].setRating(3.87f);

    service.setRatings("testApp1", userId[1], "foo:bar", r2);

    got = service.getRatingStats("testApp1", "foo:bar");
    compare(got, calcStats(new ObjectRating[][] { ratings, r2 }));

    // and another rating
    userId[2] = userService.createUser("musterAuth3");

    ObjectRating[] r3 = new ObjectRating[3];
    r3[0] = new ObjectRating();
    r3[1] = new ObjectRating();
    r3[2] = new ObjectRating();

    r3[0].setCategory("length");
    r3[0].setRating(2.2f);
    r3[1].setCategory("color");
    r3[1].setRating(10.7f);
    r3[2].setCategory("wellness");
    r3[2].setRating(8.34f);

    service.setRatings("testApp1", userId[2], "foo:bar", r3);

    got = service.getRatingStats("testApp1", "foo:bar");
    compare(got, calcStats(new ObjectRating[][] { ratings, r2, r3 }));

    // last one
    userId[3] = userService.createUser("musterAuth4");

    ObjectRating[] r4 = new ObjectRating[3];
    r4[0] = new ObjectRating();
    r4[1] = new ObjectRating();
    r4[2] = new ObjectRating();

    r4[0].setCategory("size");
    r4[0].setRating(1.5f);
    r4[1].setCategory("color");
    r4[1].setRating(4.98f);
    r4[2].setCategory("wellness");
    r4[2].setRating(1.1f);

    service.setRatings("testApp1", userId[3], "foo:bar", r4);

    got = service.getRatingStats("testApp1", "foo:bar");
    compare(got, calcStats(new ObjectRating[][] { ratings, r2, r3, r4 }));

    // lets remove one
    service.setRatings("testApp1", userId[1], "foo:bar", null);

    got = service.getRatingStats("testApp1", "foo:bar");
    compare(got, calcStats(new ObjectRating[][] { ratings, r3, r4 }));

    // lets remove another one
    service.setRatings("testApp1", userId[0], "foo:bar", null);

    got = service.getRatingStats("testApp1", "foo:bar");
    compare(got, calcStats(new ObjectRating[][] { r3, r4 }));

    // and another one
    service.setRatings("testApp1", userId[3], "foo:bar", null);

    got = service.getRatingStats("testApp1", "foo:bar");
    compare(got, calcStats(new ObjectRating[][] { r3 }));

    // and the last one
    service.setRatings("testApp1", userId[2], "foo:bar", null);

    got = service.getRatingStats("testApp1", "foo:bar");
    assertNull("non-null stats, got " + got, got);
  }

  private static ObjectRatingStats[] calcStats(ObjectRating[][] ratings) {
    // collect by category
    Map rlists = new HashMap();
    for (int idx = 0; idx < ratings.length; idx++) {
      for (int idx2 = 0; idx2 < ratings[idx].length; idx2++) {
        String cat = ratings[idx][idx2].getCategory();
        List r = (List) rlists.get(cat);
        if (r == null)
          rlists.put(cat, r = new ArrayList());
        r.add(new Float(ratings[idx][idx2].getRating()));
      }
    }

    // calc stats
    ObjectRatingStats[] stats = new ObjectRatingStats[rlists.size()];
    int idx = 0;
    for (Iterator iter = rlists.keySet().iterator(); iter.hasNext(); idx++) {
      String cat = (String) iter.next();
      List   r   = (List) rlists.get(cat);

      stats[idx] = createStatsObj(cat, r.size(), (float) avg(r), (float) var(r));
    }

    return stats;
  }

  private static final double avg(List elems) {
    double sum = 0;
    for (Iterator iter = elems.iterator(); iter.hasNext(); )
      sum += ((Float) iter.next()).floatValue();
    return sum / elems.size();
  }

  private static final double var(List elems) {
    double sum = 0;
    for (Iterator iter = elems.iterator(); iter.hasNext(); ) {
      float f = ((Float) iter.next()).floatValue();
      sum += f * f;
    }
    double avg = avg(elems);
    return sum / elems.size() - avg * avg;
  }

  private static ObjectRatingStats createStatsObj(String cat, int num, float avg, float var) {
    ObjectRatingStats s = new ObjectRatingStats();

    s.setCategory(cat);
    s.setNumberOfRatings(num);
    s.setAverage(avg);
    s.setVariance(var);

    return s;
  }

  private static void sort(ObjectRating[] ratings) {
    // sort by category
    Arrays.sort(ratings, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((ObjectRating) o1).getCategory().compareTo(((ObjectRating) o2).getCategory());
      }
    });
  }

  private static void sort(ObjectRatingStats[] stats) {
    // sort by category
    Arrays.sort(stats, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((ObjectRatingStats) o1).getCategory().compareTo(
               ((ObjectRatingStats) o2).getCategory());
      }
    });
  }

  private static void compare(ObjectRating[] got, ObjectRating[] exp) {
    assertNotNull("got null ratings", got);
    assertEquals("wrong number of ratings,", exp.length,  got.length);

    sort(got);
    sort(exp);

    for (int idx = 0; idx < got.length; idx++) {
      assertEquals("category mismatch", exp[idx].getCategory(), got[idx].getCategory());
      assertEquals("rating mismatch", exp[idx].getRating(), got[idx].getRating(), 0.0000001f);
    }
  }

  private static void compare(ObjectRatingStats[] got, ObjectRatingStats[] exp) {
    assertNotNull("got null stats", got);
    assertEquals("wrong number of stats,", exp.length,  got.length);

    sort(got);
    sort(exp);

    for (int idx = 0; idx < got.length; idx++) {
      assertEquals("category mismatch", exp[idx].getCategory(), got[idx].getCategory());
      assertEquals("num-ratings mismatch", exp[idx].getNumberOfRatings(),
                   got[idx].getNumberOfRatings());
      assertEquals("average mismatch", exp[idx].getAverage(), got[idx].getAverage(), 0.0000001f);
      assertEquals("variance mismatch", exp[idx].getVariance(), got[idx].getVariance(), 0.0000001f);
    }
  }
}
