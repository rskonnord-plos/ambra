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
package org.ambraproject.permission.service;

import org.ambraproject.cache.Cache;
import org.ambraproject.models.UserProfile;
import org.ambraproject.models.UserRole;
import org.ambraproject.models.UserRole.Permission;
import org.ambraproject.service.HibernateServiceImpl;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Required;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple role based permissions service, roles are cached keyed off of authID
 *
 * @author Joe Osowski
 */
public class PermissionsServiceImpl extends HibernateServiceImpl implements PermissionsService {
  private static final String ROLES_LOCK = "RolesCache-Lock-";
  private Cache rolesCache;

  /**
   * Does the user associated with the current security principle have the given permission?
   * @param permission The permission to check for
   * @param authId the Authorization ID of the current user
   *
   * @throws SecurityException if the user doesn't have the permission
   */
  @SuppressWarnings("unchecked")
  public void checkPermission(final Permission permission, final String authId) throws SecurityException
  {
    if(authId == null || authId.trim().length() == 0) {
      throw new SecurityException("There is no current user.");
    }

    Set<UserRole> roles = getRoles(authId);

    if(roles.size() == 0) {
      throw new SecurityException("Current user does not have the defined permission of " + permission.toString());
    }

    for(UserRole role : roles) {
      Set<Permission> perms = role.getPermissions();

      if(perms == null) {
        throw new SecurityException("Current user does not have the defined permission of " + permission.toString());
      }

      for(Permission p : perms) {
        if(p.equals(permission)) {
          return;
        }
      }
    }

    throw new SecurityException("Current user does not have the defined permission of " + permission.toString());
  }

  public Set<Permission> getPermissions(final String authId) {
    Set<UserRole> roles = getRoles(authId);
    Set<Permission> result = new HashSet<Permission>();

    if(roles != null) {
      for(UserRole role : roles) {
        Set<Permission> perms = role.getPermissions();

        if(perms != null) {
          result.addAll(perms);
        }
      }
    }

    return result;
  }

  private Set<UserRole> getRoles(final String authId) {
    final Object lock = (ROLES_LOCK + authId).intern(); //lock @ Article level

    return rolesCache.get(authId,
      new Cache.SynchronizedLookup<Set<UserRole>, SecurityException>(lock) {
        public Set<UserRole> lookup() throws SecurityException {
          List<UserProfile> userProfiles =
            hibernateTemplate.findByCriteria(DetachedCriteria.forClass(UserProfile.class)
              .add(Restrictions.eq("authId", authId))
              .setFetchMode("roles", FetchMode.JOIN)
              .setFetchMode("permissions", FetchMode.JOIN)
              .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));

          if(userProfiles.size() == 0) {
            throw new SecurityException("No user found for authID: '" + authId + "'");
          }

          return userProfiles.get(0).getRoles();
        }
    });
  }

  public void checkLogin(String authId) throws SecurityException {
    if (authId != null) {
      return;
    }

    throw new SecurityException("Current user is not logged in");
  }

  /**
   * @param rolesCache The roles cache to use
   */
  @Required
  public void setRolesCache(Cache rolesCache) {
    this.rolesCache = rolesCache;
  }
}
