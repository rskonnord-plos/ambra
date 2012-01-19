/* $HeadURL::                                                                            $
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
package org.topazproject.ambra.user;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;

import static org.topazproject.ambra.user.UsersPEP.FIND_USERS_BY_PROF;
import static org.topazproject.ambra.user.UsersPEP.GET_BIOGRAPHY;
import static org.topazproject.ambra.user.UsersPEP.GET_BIOGRAPHY_TEXT;
import static org.topazproject.ambra.user.UsersPEP.GET_CITY;
import static org.topazproject.ambra.user.UsersPEP.GET_COUNTRY;
import static org.topazproject.ambra.user.UsersPEP.GET_DISP_NAME;
import static org.topazproject.ambra.user.UsersPEP.GET_EMAIL;
import static org.topazproject.ambra.user.UsersPEP.GET_GENDER;
import static org.topazproject.ambra.user.UsersPEP.GET_GIVEN_NAMES;
import static org.topazproject.ambra.user.UsersPEP.GET_HOME_PAGE;
import static org.topazproject.ambra.user.UsersPEP.GET_INTERESTS;
import static org.topazproject.ambra.user.UsersPEP.GET_INTERESTS_TEXT;
import static org.topazproject.ambra.user.UsersPEP.GET_ORGANIZATION_NAME;
import static org.topazproject.ambra.user.UsersPEP.GET_ORGANIZATION_TYPE;
import static org.topazproject.ambra.user.UsersPEP.GET_POSITION_TYPE;
import static org.topazproject.ambra.user.UsersPEP.GET_POSTAL_ADDRESS;
import static org.topazproject.ambra.user.UsersPEP.GET_PUBLICATIONS;
import static org.topazproject.ambra.user.UsersPEP.GET_REAL_NAME;
import static org.topazproject.ambra.user.UsersPEP.GET_RESEARCH_AREAS_TEXT;
import static org.topazproject.ambra.user.UsersPEP.GET_SURNAMES;
import static org.topazproject.ambra.user.UsersPEP.GET_TITLE;
import static org.topazproject.ambra.user.UsersPEP.GET_WEBLOG;
import static org.topazproject.ambra.user.UsersPEP.SET_PROFILE;

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

  private static Collection<UserProfileGrant> selectProfileGrants(final String[] values,
                                                                  final Predicate predicate) {
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
