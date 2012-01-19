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

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.topazproject.ws.users.NoSuchUserIdException;

/** 
 * This defines the ratings service. Any object can be rated, and no restrictions on the ratings
 * themselves are enforced. The only restriction enforced by this service is a single set of ratings
 * per user and object.
 *
 * <p>In addition to per-user ratings, overall stats are kept on a per-object basis. These include
 * the average rating, standard-deviation, etc.
 * 
 * @author Ronald Tschalär
 */
public interface Ratings extends Remote {
  /**
   * Permissions associated with the ratings service.
   */
  public static interface Permissions {
    /** The action that represents the set-ratings operation in XACML policies. */
    public static final String SET_RATINGS = "ratings:setRatings";
    /** The action that represents the get-ratings operation in XACML policies. */
    public static final String GET_RATINGS = "ratings:getRatings";
    /** The action that represents the get-stats operation in XACML policies. */
    public static final String GET_STATS = "ratings:getStats";
  }

  /** 
   * Get a user's ratings for the specified object.
   * 
   * @param appId  the application id; may be null
   * @param userId the user's internal id
   * @param object the URI of the rated object
   * @return the user's ratings, or null if none exist for this user. Note that the order of the
   *         entries will be arbitrary.
   * @throws NoSuchUserIdException the user does not exist
   * @throws RemoteException if some error occured accessing the ratings
   */
  public ObjectRating[] getRatings(String appId, String userId, String object)
      throws NoSuchUserIdException, RemoteException;

  /** 
   * Set a user's ratings. This completely overrides any previous ratings by this user for this
   * object.
   * 
   * @param appId   the application id; may only be null if <var>ratings</var> is also null, which
   *                causes all ratings for the given user under all app-ids to be erased.
   * @param userId  the user's internal id
   * @param object  the URI of the rated object; may be null if <var>ratings</var> is also null,
   *                which causes all ratings for the given user for all objects to be erased.
   * @param ratings the user's ratings for the object; may be null in which case all ratings are
   *                erased. Note that the order will not be preserved.
   * @throws NoSuchUserIdException the user does not exist
   * @throws RemoteException if some error occured accessing the ratings
   */
  public void setRatings(String appId, String userId, String object, ObjectRating[] ratings)
      throws NoSuchUserIdException, RemoteException;

  /** 
   * Get a rating stats for the specified object.
   * 
   * @param appId  the application id; may be null
   * @param object the URI of the rated object
   * @return the rating stats, or null if there are no ratings for this object. There will be one
   *         item for each category which has at least one rating. Note that the order of the
   *         items will be arbitrary.
   * @throws RemoteException if some error occured accessing the ratings
   */
  public ObjectRatingStats[] getRatingStats(String appId, String object) throws RemoteException;
}
