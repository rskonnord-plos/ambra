/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.user;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import org.topazproject.xacml.AbstractSimplePEP;
import org.plos.ApplicationException;
import org.plos.xacml.XacmlUtil;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PDP;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The XACML PEP for the user accounts manager.
 *
 * @author Ronald Tschalär
 */
public class UsersPEP extends AbstractSimplePEP {
  // Permissions associated with the user-accounts service.
  /** The action that represents a user account creation operation in XACML policies. */
  public static final String CREATE_USER = "userAccounts:createUser";

  /** The action that represents a delete user account operation in XACML policies. */
  public static final String DELETE_USER = "userAccounts:deleteUser";

  /** The action that represents a get-state operation in XACML policies. */
  public static final String GET_STATE = "userAccounts:getState";

  /** The action that represents a set-state operation in XACML policies. */
  public static final String SET_STATE = "userAccounts:setState";

  /** The action that represents a get-authentication-ids operation in XACML policies. */
  public static final String GET_AUTH_IDS = "userAccounts:getAuthIds";

  /** The action that represents a set-authentication-ids operation in XACML policies. */
  public static final String SET_AUTH_IDS = "userAccounts:setAuthIds";

  /** The action that represents a look-up-user operation in XACML policies. */
  public static final String LOOKUP_USER = "userAccounts:lookUpUser";

  // Permissions associated with the profiles service.
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

  // Permissions associated with the preferences service.
  /** The action that represents a write operation in XACML policies. */
  public static final String SET_PREFERENCES = "preferences:setPreferences";

  /** The action that represents a read operation in XACML policies. */
  public static final String GET_PREFERENCES = "preferences:getPreferences";

  // Permissions associated with user-roles service.
  /** The action that represents a get-roles operation in XACML policies. */
  public static final String GET_ROLES = "userRoles:getRoles";

  /** The action that represents a set-roles operation in XACML policies. */
  public static final String SET_ROLES = "userRoles:setRoles";

  /** The action that represents a list-users-in-role operation in XACML policies. */
  public static final String LIST_USERS_IN_ROLE = "userRoles:listUsersInRole";

  /** The list of all supported actions */
  protected static final String[] SUPPORTED_ACTIONS = new String[] {
      // user-account perms
      CREATE_USER, DELETE_USER, GET_STATE, SET_STATE, GET_AUTH_IDS, SET_AUTH_IDS, LOOKUP_USER,
      // profile perms
      GET_DISP_NAME, GET_REAL_NAME, GET_GIVEN_NAMES, GET_SURNAMES, GET_TITLE, GET_GENDER,
      GET_POSITION_TYPE, GET_ORGANIZATION_NAME, GET_ORGANIZATION_TYPE, GET_POSTAL_ADDRESS,
      GET_COUNTRY, GET_CITY, GET_EMAIL, GET_HOME_PAGE, GET_WEBLOG, GET_BIOGRAPHY, GET_INTERESTS,
      GET_PUBLICATIONS, GET_BIOGRAPHY_TEXT, GET_INTERESTS_TEXT, GET_RESEARCH_AREAS_TEXT,
      SET_PROFILE, FIND_USERS_BY_PROF,
      // preferences perms
      SET_PREFERENCES, GET_PREFERENCES,
      // roles perms
      GET_ROLES, SET_ROLES,
  };

  /** The list of all supported obligations */
  protected static final String[][] SUPPORTED_OBLIGATIONS = null;

  static {
    init(UsersPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
  }

  /** 
   * Create a new users pep. 
   * 
   * @throws IOException if an error occurred trying to get the PDP
   */
  public UsersPEP() throws IOException {
    super(getPDP(), XacmlUtil.createSubjAttrs());
  }

  private static final PDP getPDP() throws IOException {
    try {
      return XacmlUtil.lookupPDP("topaz.users.pdpName");
    } catch (ParsingException pe) {
      throw (IOException) new IOException("Error creating users-pep").initCause(pe);
    } catch (UnknownIdentifierException uie) {
      throw (IOException) new IOException("Error creating users-pep").initCause(uie);
    }
  }

  /** 
   * Check whether the given action is allowed on the given resource by the current user. 
   * This is the same as {@link AbstractSimplePEP#checkAccess AbstractSimplePEP.checkAccess}
   * except that a {@link java.lang.SecurityException SecurityException} is converted into an
   * {@link org.plos.ApplicationException ApplicationException}.
   * 
   * @param action   the action to check
   * @param resource the resource to check
   * @return the obligations
   * @throws ApplicationException if access is denied
   */
  public Set checkAccessAE(String action, URI resource) throws ApplicationException {
    try {
      return super.checkAccess(action, resource);
    } catch (SecurityException se) {
      throw new ApplicationException("access denied: action=" + action + ", resource='" +
                                     resource + "'", se);
    }
  }
}
