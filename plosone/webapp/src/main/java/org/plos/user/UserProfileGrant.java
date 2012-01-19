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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import static org.topazproject.ws.pap.Profiles.Permissions.FIND_USERS_BY_PROF;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_BIOGRAPHY;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_BIOGRAPHY_TEXT;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_CITY;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_COUNTRY;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_DISP_NAME;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_EMAIL;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_GENDER;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_GIVEN_NAMES;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_HOME_PAGE;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_INTERESTS;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_INTERESTS_TEXT;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_ORGANIZATION_NAME;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_ORGANIZATION_TYPE;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_POSITION_TYPE;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_POSTAL_ADDRESS;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_PUBLICATIONS;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_REAL_NAME;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_RESEARCH_AREAS_TEXT;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_SURNAMES;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_TITLE;
import static org.topazproject.ws.pap.Profiles.Permissions.GET_WEBLOG;
import static org.topazproject.ws.pap.Profiles.Permissions.SET_PROFILE;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Enums for fieldnames and their corresponding grants to read the values.
 */
public enum UserProfileGrant {
  EMAIL ("email", GET_EMAIL),
  REAL_NAME ("realName", GET_REAL_NAME),
  DISPLAY_NAME ("displayName", GET_DISP_NAME),
  POSITION_TYPE ("positionType", GET_POSITION_TYPE),
  POSTAL_ADDRESS ("postalAddress", GET_POSTAL_ADDRESS),
  RESEARCH_AREAS_TEXT ("researchAreasText", GET_RESEARCH_AREAS_TEXT),
  GIVENNAMES ("givenNames", GET_GIVEN_NAMES),
  SURNAMES ("surnames", GET_SURNAMES),
  TITLE ("title", GET_TITLE),
  GENDER ("gender", GET_GENDER),
  ORGANIZATION_NAME ("organizationName", GET_ORGANIZATION_NAME),
  ORGANIZATION_TYPE ("organizationType", GET_ORGANIZATION_TYPE),
  COUNTRY ("country", GET_COUNTRY),
  CITY ("city", GET_CITY),
  HOME_PAGE ("homePage", GET_HOME_PAGE),
  WEBLOG ("weblog", GET_WEBLOG),
  BIOGRAPHY ("biography", GET_BIOGRAPHY),
  INTERESTS ("interests", GET_INTERESTS),
  PUBLICATIONS ("publications", GET_PUBLICATIONS),
  BIOGRAPHY_TEXT ("biographyText", GET_BIOGRAPHY_TEXT),
  INTERESTS_TEXT ("interestsText", GET_INTERESTS_TEXT),
  PROFILE ("profile", SET_PROFILE),
  FIND_USERS_BY_PROF_FIELD ("findUsersByProf", FIND_USERS_BY_PROF);

  private final String fieldName; //name of the field
  private final String grant; //grant to read this field
  private static Collection<UserProfileGrant> sortedProfileGrants;

  UserProfileGrant(final String fieldName, final String permission) {
    this.fieldName = fieldName;
    this.grant = permission;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getGrant() {
    return grant;
  }

  /**
   * Return the UserProfileGrant for a list of grants.
   * @param grants grants
   * @return collection of UserProfileGrant's
   */
  public static Collection<UserProfileGrant> getProfileGrantsForGrants(final String[] grants) {
    final Predicate predicate = new Predicate() {
      public boolean evaluate(Object object) {
        final UserProfileGrant permEnum = (UserProfileGrant) object;
        return ArrayUtils.contains(grants, permEnum.getGrant());
      }
    };

    return selectProfileGrants(grants, predicate);
  }

  /**
   * Return the UserProfileGrant for a list of fields.
   * @param fields fields
   * @return collection of UserProfileGrant's
   */
  public static Collection<UserProfileGrant> getProfileGrantsForFields(final String[] fields) {
    final Predicate predicate = new Predicate() {
      public boolean evaluate(Object object) {
        final UserProfileGrant permEnum = (UserProfileGrant) object;
        return ArrayUtils.contains(fields, permEnum.getFieldName());
      }
    };

    return selectProfileGrants(fields, predicate);
  }

  private static Collection<UserProfileGrant> selectProfileGrants(final String[] values, final Predicate predicate) {
    if (null == sortedProfileGrants) {
      sortedProfileGrants = sortProfileGrants();
    }

    if (null == values) return Collections.EMPTY_LIST;

    Arrays.sort(values);

    return CollectionUtils.select(sortedProfileGrants, predicate);
  }

  private static Collection<UserProfileGrant> sortProfileGrants() {
    final List<UserProfileGrant> list = Arrays.asList(values());
    Collections.sort(list, new Comparator<UserProfileGrant>() {
      public int compare(final UserProfileGrant o1, final UserProfileGrant o2) {
        return o1.getGrant().compareTo(o2.getGrant());
      }
    });
    return list;
  }
}
