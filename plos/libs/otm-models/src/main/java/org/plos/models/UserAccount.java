/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.models;

import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;

/**
 * A user's account.
 *
 * @author Ronald Tschalär
 */
@Entity(type = Rdf.foaf + "OnlineAccount", model = "users")
public class UserAccount implements Serializable {
  /** the state indicating the user account is active: {@value} */
  public static final int ACNT_ACTIVE    = 0;
  /** the state indicating the user account is suspended: {@value} */
  public static final int ACNT_SUSPENDED = 1;

  @Id @GeneratedValue(uriPrefix = "info:doi/10.1371/account/")
  private URI id;
  @Predicate(uri = Rdf.topaz + "accountState")
  private int state = ACNT_ACTIVE;
  @Predicate(uri = Rdf.topaz + "hasAuthId")
  private Set<AuthenticationId> authIds = new HashSet<AuthenticationId>();
  @Predicate(uri = Rdf.topaz + "hasRoles")
  private Set<UserRole>         roles = new HashSet<UserRole>();
  @Predicate(uri = Rdf.topaz + "hasPreferences", model="preferences")
  private Set<UserPreferences>  preferences = new HashSet<UserPreferences>();
  @Predicate(uri = Rdf.foaf + "holdsAccount", inverse = true, model="profiles")
  private UserProfile           profile;

  /**
   * @return the id
   */
  public URI getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(URI id) {
    this.id = id;
  }

  /**
   * Get the state.
   *
   * @return the state.
   */
  public int getState() {
    return state;
  }

  /**
   * Set the state.
   *
   * @param state the state.
   */
  public void setState(int state) {
    this.state = state;
  }

  /**
   * Get the authentication ids.
   *
   * @return the authentication ids.
   */
  public Set<AuthenticationId> getAuthIds() {
    return authIds;
  }

  /**
   * Set the authentication ids.
   *
   * @param authIds the authentication ids.
   */
  public void setAuthIds(Set<AuthenticationId> authIds) {
    this.authIds = authIds;
  }

  /**
   * Get the roles.
   *
   * @return the roles.
   */
  public Set<UserRole> getRoles() {
    return roles;
  }

  /**
   * Set the roles.
   *
   * @param roles the roles.
   */
  public void setRoles(Set<UserRole> roles) {
    this.roles = roles;
  }

  /**
   * Get the preferences.
   *
   * @return the preferences.
   */
  public Set<UserPreferences> getPreferences() {
    return preferences;
  }

  /**
   * Set the preferences.
   *
   * @param preferences the preferences.
   */
  public void setPreferences(Set<UserPreferences> preferences) {
    this.preferences = preferences;
  }

  /**
   * Get the preferences for a given application id.
   *
   * @param appId the application id
   * @return the preferences, or null if none found.
   */
  public UserPreferences getPreferences(String appId) {
    for (UserPreferences p : preferences) {
      if (p.getAppId().equals(appId))
        return p;
    }
    return null;
  }

  /**
   * Get the user's profile.
   *
   * @return the profile.
   */
  public UserProfile getProfile() {
    return profile;
  }

  /**
   * Set the user's profile.
   *
   * @param profile the profile.
   */
  public void setProfile(UserProfile profile) {
    this.profile = profile;
  }
}
