/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.models;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateSystemException;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;

/**
 * @author Alex Kudlick 2/9/12
 */
public class UserRoleTest extends BaseHibernateTest {

  @Test
  public void testSaveRole() {
    UserRole role = new UserRole();
    role.setRoleName("admin");
    Serializable id = hibernateTemplate.save(role);

    UserRole storedRole = (UserRole) hibernateTemplate.get(UserRole.class, id);
    assertEquals(storedRole.getRoleName(), "admin", "stored role didn't have correct name");
  }

  @Test
  public void testSaveRoleWithPermissions() {
    final UserRole role = new UserRole("admin1",
        UserRole.Permission.CROSS_PUB_ARTICLES,
        UserRole.Permission.VIEW_UNPUBBED_ARTICLES,
        UserRole.Permission.MANAGE_ANNOTATIONS,
        UserRole.Permission.MANAGE_CACHES
    );
    final Serializable id = hibernateTemplate.save(role);

    UserRole storedRole = (UserRole) hibernateTemplate.get(UserRole.class, id);
    assertEquals(storedRole.getRoleName(), role.getRoleName(), "role had incorrect name");
    assertEqualsNoOrder(storedRole.getPermissions().toArray(), role.getPermissions().toArray(), "Role had incorrect permissions");
  }

  @Test
  public void testEditPermissions() {
    UserRole role = new UserRole("admin2",
        UserRole.Permission.CROSS_PUB_ARTICLES,
        UserRole.Permission.VIEW_UNPUBBED_ARTICLES,
        UserRole.Permission.MANAGE_ANNOTATIONS,
        UserRole.Permission.MANAGE_CACHES
    );
    final Serializable id = hibernateTemplate.save(role);
    UserRole storedRole = (UserRole) hibernateTemplate.get(UserRole.class, id);
    storedRole.getPermissions().remove(UserRole.Permission.CROSS_PUB_ARTICLES);
    storedRole.getPermissions().remove(UserRole.Permission.VIEW_UNPUBBED_ARTICLES);
    storedRole.getPermissions().add(UserRole.Permission.MANAGE_JOURNALS);

    hibernateTemplate.update(storedRole);

    UserRole storedRole2 = (UserRole) hibernateTemplate.get(UserRole.class, id);
    assertEqualsNoOrder(storedRole.getPermissions().toArray(), storedRole2.getPermissions().toArray(),
        "Permissions didn't get updated");
  }

  @Test(expectedExceptions = {DataIntegrityViolationException.class})
  public void testSaveWithNullRole() {
    hibernateTemplate.save(new UserRole());
  }

  @Test(expectedExceptions = {DataIntegrityViolationException.class})
  public void testUniqueRoleConstraint() {
    UserRole role1 = new UserRole();
    role1.setRoleName("foo");
    UserRole role2 = new UserRole();
    role2.setRoleName("foo");

    hibernateTemplate.save(role1);
    hibernateTemplate.save(role2);
  }
}
