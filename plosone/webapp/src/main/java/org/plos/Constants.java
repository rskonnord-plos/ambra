/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos;

import edu.yale.its.tp.cas.client.filter.CASFilter;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.Map;

/**
 * Some of the constants for the Plosone application.
 */
public interface Constants {
  String PLOS_ONE_USER_KEY = "PLOS_ONE_USER";
  String SINGLE_SIGNON_USER_KEY = CASFilter.CAS_FILTER_USER;
  String SINGLE_SIGNON_RECEIPT = CASFilter.CAS_FILTER_RECEIPT;
  /** Authentication method used for anonymous user, otherwise it is normally CAS */
  String ANONYMOUS_USER_AUTHENTICATION = "ANONYMOUS_USER_AUTHENTICATION";
  String AUTH_METHOD_KEY = "auth-method";
  String ROOT_PACKAGE = "org.plos";
  String ADMIN_ROLE = "admin";

  /**
   * Defines the length of various fields used by Webwork Annotations
   */
  interface Length {
    String EMAIL = "256";
    String PASSWORD = "256";
    String DISPLAY_NAME_MIN = "4";
    String DISPLAY_NAME_MAX = "18";
  }

  /**
   * Return Code to be used for WebWork actions
   */
  interface ReturnCode {
    String NEW_PROFILE = "new-profile";
    String UPDATE_PROFILE = "update-profile";
    String NOT_SUFFICIENT_ROLE = "role-insufficient";
  }

  /**
   * Masks used for denoting the state for annotations and replies
   */
  interface StateMask {
    int PUBLIC = 0x001; //binary 0001
    int FLAG = 0x002;   //binary 0010
    int DELETE = 0x004; //binary 0100
  }

  /**
   * Permission constants
   */
  public interface Permission {
    String ALL_PRINCIPALS = "http://rdf.topazproject.org/RDF/permissions#all";
  }

  public static class SelectValues {
    /** return a map of all url descriptions */
    public static Map<String, String> getAllUrlDescriptions() {
      final Map<String, String> allUrlDescriptions = new ListOrderedMap();
      allUrlDescriptions.put("", "Choose One");
      allUrlDescriptions.put("Personal", "Personal");
      allUrlDescriptions.put("Laboratory", "Laboratory");
      allUrlDescriptions.put("Departmental", "Departmental");
      allUrlDescriptions.put("Blog", "Blog");
      return allUrlDescriptions;
    }

    /** return a map of all position types */
    public static Map<String, String> getAllPositionTypes() {
      final Map<String, String> allPositionTypes = new ListOrderedMap();
      allPositionTypes.put("", "Choose One");
      allPositionTypes.put("Head of Department/Director", "Head of Department/Director");
      allPositionTypes.put("Professor/Group Leader", "Professor/Group Leader");
      allPositionTypes.put("Physician", "Physician");
      allPositionTypes.put("Post-Doctoral researcher", "Post-Doctoral researcher");
      allPositionTypes.put("Post-Graduate student", "Post-Graduate student");
      allPositionTypes.put("Undergraduate student", "Undergraduate student");
      allPositionTypes.put("Other", "Other");
      return allPositionTypes;
    }

    /** return a map of all Organization Types */
    public static Map<String, String> getAllOrganizationTypes() {
      final Map<String, String> allOrgTypes = new ListOrderedMap();
      allOrgTypes.put("", "Choose One");
      allOrgTypes.put("University", "University");
      allOrgTypes.put("Hospital", "Hospital");
      allOrgTypes.put("Industry", "Industry (Research)");
      allOrgTypes.put("Industry Non-research", "Industry (Non-research)");
      allOrgTypes.put("Government Non-research", "Government (Non-Research)");
      allOrgTypes.put("Library", "Library");
      allOrgTypes.put("School", "School");
      allOrgTypes.put("Private Address", "Private Address");
      return allOrgTypes;
    }

    /** return a map of all titles */
    public static Map<String, String> getAllTitles() {
      final Map<String, String> allTitles = new ListOrderedMap();
      allTitles.put("", "Choose One");
      allTitles.put("Professor", "Professor");
      allTitles.put("Dr", "Dr.");
      allTitles.put("Mr", "Mr.");
      allTitles.put("Mrs", "Mrs.");
      allTitles.put("Ms", "Ms.");
      allTitles.put("Other", "Other");
      return allTitles;
    }


  }
}
