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

import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

/**
 * A user's role.
 *
 * @author Ronald Tschalär
 */
@Entity(model = "users")
public class UserRole {
  @Id @GeneratedValue(uriPrefix = "info:doi/10.1371/roles/")
  private URI    id;
  @Predicate(uri = Rdf.topaz + "role")
  private String role;

  /**
   * Create new empty role.
   */
  public UserRole() {
  }

  /**
   * Create new role given role.
   */
  public UserRole(String role) {
    this.role = role;
  }

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
   * Get role.
   *
   * @return role as String.
   */
  public String getRole()
  {
      return role;
  }

  /**
   * Set role.
   *
   * @param role the value to set.
   */
  public void setRole(String role)
  {
      this.role = role;
  }
}
