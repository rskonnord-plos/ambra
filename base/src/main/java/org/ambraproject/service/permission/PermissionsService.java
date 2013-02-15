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
package org.ambraproject.service.permission;

import org.ambraproject.models.UserRole.Permission;
import java.util.Set;

/**
 * A simple role based permissions service, roles are cached keyed off of authID
 *
 * @author Joe Osowski
 */
public interface PermissionsService {
  @Deprecated
  public static String ADMIN_ROLE = "admin";

  /**
   * Does the user associated with the current security principle have the given permission?
   * @param permission The permission to check for
   * @param authId the Authorization ID of the current user
   *
   * @throws SecurityException if the user doesn't have the permission
   */
  public void checkPermission(final Permission permission, final String authId) throws SecurityException;

  /**
   * Get all the permissions for the given authID
   *
   * @param authId the authID of the current user
   *
   * @return the complete set of permissions
   */
  public Set<Permission> getPermissions(final String authId);

  public void checkLogin(String authId) throws SecurityException;

  /**
   * Clear the cache of all roles
   */
  public void clearCache();
}
