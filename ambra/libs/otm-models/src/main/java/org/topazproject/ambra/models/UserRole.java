/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
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

package org.topazproject.ambra.models;

import java.io.Serializable;
import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;

/**
 * A user's role.
 *
 * @author Ronald Tschalär
 */
@Entity(model = "users")
public class UserRole implements Serializable {
  @Id @GeneratedValue(uriPrefix = "id:roles/")
  private URI    id;
  @Predicate(uri = "topaz:role")
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
