/*
 * $HeadURL$
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

package org.topazproject.ambra.testutils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.models.AuthenticationId;
import org.topazproject.ambra.models.UserAccount;
import org.topazproject.ambra.models.UserProfile;
import org.topazproject.ambra.models.UserRole;
import org.topazproject.ambra.permission.service.PermissionsService;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;


/**
 * Session factory for tests using Hibernate service beans. This is provided so we can turn off foreign key constraints
 * in HSQLDB for testing
 *
 * @author Alex Kudlick Date: 5/4/11
 *         <p/>
 *         org.topazproject.ambra
 */
public class HibernateTestSessionFactory {

  private SessionFactory sessionFactory;

  public HibernateTestSessionFactory() throws SQLException {
    //Set the system property needed by the AmbraIdGenerator
    System.setProperty(ConfigurationStore.SYSTEM_OBJECT_ID_PREFIX, "test:doi/0.0/");

    Configuration configuration = new Configuration().configure();
    this.sessionFactory = configuration.buildSessionFactory();

    //Turn off foreign key constraints
    Connection connection = DriverManager.getConnection(
        configuration.getProperty("connection.url"),  //connection url
        configuration.getProperty("connection.username"), //username
        configuration.getProperty("connection.password")); //password

    Session session = sessionFactory.openSession();

    // Create an admin user to test admin functions
    UserAccount ua = new UserAccount();
    ua.setId(URI.create("AdminAccountID"));

    UserRole ur = new UserRole();
    ur.setRole(PermissionsService.ADMIN_ROLE);
    ua.getRoles().add(ur);
    UserProfile up = new UserProfile();
    up.setRealName("Foo user");
    ua.setProfile(up);
    ua.getAuthIds().add(new AuthenticationId("AdminAuthorizationID"));

    session.save(ua);
    session.save(up);
    session.save(ur);

    // Create a dummy joe blow user
    ua = new UserAccount();
    ua.setId(URI.create("DummyTestUserID"));
    up = new UserProfile();
    up.setRealName("Dummy user");
    up.setEmailFromString("testcase@topazproject.org");
    up.setCity("my city");
    ua.setProfile(up);
    ua.getAuthIds().add(new AuthenticationId("DummyTestUserAuthorizationID"));

    session.save(ua);
    session.save(up);
    session.flush();
    session.close();

    connection.createStatement().execute("SET REFERENTIAL_INTEGRITY FALSE;");
    connection.close();
  }

  public Session openSession() {
    return sessionFactory.openSession();
  }

  public SessionFactory getFactory() {
    return sessionFactory;
  }

  public void close() {
    sessionFactory.close();
  }

}
