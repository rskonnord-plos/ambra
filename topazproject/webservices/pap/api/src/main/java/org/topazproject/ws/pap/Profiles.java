/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap;

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.topazproject.ws.users.NoSuchUserIdException;

/** 
 * This defines the profiles service. It allows a single profile to be associated with each user.
 * 
 * @author Ronald Tschalär
 */
public interface Profiles extends Remote {
  /**
   * Permissions associated with the profiles service.
   */
  public static interface Permissions {
    /** The action that represents a get-display-name operation in XACML policies: {@value}. */
    public static final String GET_DISP_NAME = "profiles:getDisplayName";

    /** The action that represents a get-real-name operation in XACML policies: {@value}. */
    public static final String GET_REAL_NAME = "profiles:getRealName";

    /** The action that represents a get-given-names operation in XACML policies: {@value}. */
    public static final String GET_GIVEN_NAMES = "profiles:getGivenNames";

    /** The action that represents a get-surnnames operation in XACML policies: {@value}. */
    public static final String GET_SURNAMES = "profiles:getSurnames";

    /** The action that represents a get-title operation in XACML policies: {@value}. */
    public static final String GET_TITLE = "profiles:getTitle";

    /** The action that represents a get-gender operation in XACML policies: {@value}. */
    public static final String GET_GENDER = "profiles:getGender";

    /** The action that represents a get-position-type operation in XACML policies: {@value}. */
    public static final String GET_POSITION_TYPE = "profiles:getPositionType";

    /** The action that represents a get-organization-name operation in XACML policies: {@value}. */
    public static final String GET_ORGANIZATION_NAME = "profiles:getOrganizationName";

    /** The action that represents a get-organization-type operation in XACML policies: {@value}. */
    public static final String GET_ORGANIZATION_TYPE = "profiles:getOrganizationType";

    /** The action that represents a get-postal-address operation in XACML policies: {@value}. */
    public static final String GET_POSTAL_ADDRESS = "profiles:getPostalAddress";

    /** The action that represents a get-country operation in XACML policies: {@value}. */
    public static final String GET_COUNTRY = "profiles:getCountry";

    /** The action that represents a get-city operation in XACML policies: {@value}. */
    public static final String GET_CITY = "profiles:getCity";

    /** The action that represents a get-email operation in XACML policies: {@value}. */
    public static final String GET_EMAIL = "profiles:getEmail";

    /** The action that represents a get-home-page operation in XACML policies: {@value}. */
    public static final String GET_HOME_PAGE = "profiles:getHomePage";

    /** The action that represents a get-weblog operation in XACML policies: {@value}. */
    public static final String GET_WEBLOG = "profiles:getWeblog";

    /** The action that represents a get-biography operation in XACML policies: {@value}. */
    public static final String GET_BIOGRAPHY = "profiles:getBiography";

    /** The action that represents a get-interests operation in XACML policies: {@value}. */
    public static final String GET_INTERESTS = "profiles:getInterests";

    /** The action that represents a get-publications operation in XACML policies: {@value}. */
    public static final String GET_PUBLICATIONS = "profiles:getPublications";

    /** The action that represents a get-biography-text operation in XACML policies: {@value}. */
    public static final String GET_BIOGRAPHY_TEXT = "profiles:getBiographyText";

    /** The action that represents a get-interests-text operation in XACML policies: {@value}. */
    public static final String GET_INTERESTS_TEXT = "profiles:getInterestsText";

    /** The action that represents a get-research-areas-text op in XACML policies: {@value}. */
    public static final String GET_RESEARCH_AREAS_TEXT = "profiles:getResearchAreasText";

    /** The action that represents a set-profile operation in XACML policies: {@value}. */
    public static final String SET_PROFILE = "profiles:setProfile";

    /** The action that represents a find-users-by-profile operation in XACML policies: {@value}. */
    public static final String FIND_USERS_BY_PROF = "profiles:findUsersByProfile";
  }

  /** 
   * Get a user's profile. Note that all fields in the profile are subject to access-control checks,
   * and any field to which the user calling this service does not have access will be null'd out.
   * 
   * @param userId  the user's internal id
   * @return the user's profile, or null if the user does not have a profile
   * @throws NoSuchUserIdException if the user does not exist
   * @throws RemoteException if some other error occured
   */
  public UserProfile getProfile(String userId) throws NoSuchUserIdException, RemoteException;

  /** 
   * Set a user's profile. This completely replaces the current profile (if any) with the given one.
   * 
   * @param userId  the user's internal id
   * @param profile the user's new profile; may be null in which case the profile is erased
   * @throws NoSuchUserIdException if the user does not exist
   * @throws RemoteException if some other error occured
   */
  public void setProfile(String userId, UserProfile profile)
      throws NoSuchUserIdException, RemoteException;

  /** 
   * Find a user by information from their profile. This is uses a query-by-example approach.
   * 
   * @param templates  the templates to use for matching. For a template to match, all non-null
   *                   fields in the template must match exactly or modulo case (depending on the
   *                   <var>ignoreCase</var> flag), and all null fields are ignored. In boolean
   *                   terms, the fields are AND'd and the templates are OR'd.
   * @param ignoreCase whether the template fields should be matched case-insensitively or exactly.
   *                   This array have as many entries as there are templates; the n'th entry in
   *                   this array affects the matching of the n'th template.
   * @return the list user-ids of users whose profile matches one of the templates; this will be the
   *         empty list if no matches are found. The list is filtered so that only those users are
   *         returned for whom the current user has read-permission on all non-null template fields.
   * @throws RemoteException if some error occured
   */
  public String[] findUsersByProfile(UserProfile[] templates, boolean[] ignoreCase)
      throws RemoteException;
}
