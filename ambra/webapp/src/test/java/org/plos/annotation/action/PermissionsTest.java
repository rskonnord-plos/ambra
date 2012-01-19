/* $HeadURL::                                                                            $
 * $Id:PermissionsTest.java 722 2006-10-02 16:42:45Z viru $
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
package org.plos.annotation.action;

import org.plos.BasePlosoneTestCase;
import org.plos.permission.service.PermissionsService;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;

public class PermissionsTest extends BasePlosoneTestCase {
  private String      resource = "foo:bar";
  private String[]    grants   = new String[] { "annotation:visibility-private", "foo:create", "foo:get", "foo:set", "foo:delete"  };
  private String[]    revokes    = new String[] { "foo:list-all", "foo:purge" };
  private String[]    principals = new String[] { "user:viru" };

  private PermissionsService service;

  protected void onSetUp() throws MalformedURLException, ServiceException, RemoteException {
    service = getPermissionsService();
    clearAll();
  }

  public void test1Grants() throws RemoteException {
    service.grant(resource, grants, principals);

    for (final String principal : principals) {
      String[] l = service.listGrants(resource, principal);
      assertEquals(new HashSet(Arrays.asList(grants)), new HashSet(Arrays.asList(l)));
    }
  }

  /**
   * Test for cancel grants.
   *
   * @throws RemoteException on error
   */
  public void test2CancelGrants() throws RemoteException {
    service.grant(resource, grants, principals);
    service.cancelGrants(resource, grants, principals);

    for (final String principal : principals) {
      String[] l = service.listGrants(resource, principal);
      assertEquals(0, l.length);
    }

    // and again
    service.cancelGrants(resource, grants, principals);

    for (String principal1 : principals) {
      String[] l = service.listGrants(resource, principal1);
      assertEquals(0, l.length);
    }
  }

  public void test3Revokes() throws RemoteException {
    service.revoke(resource, revokes, principals);

    for (String principal : principals) {
      String[] l = service.listRevokes(resource, principal);
      assertEquals(new HashSet(Arrays.asList(revokes)), new HashSet(Arrays.asList(l)));
    }
  }

  public void test4CancelRevokes() throws RemoteException {
    service.revoke(resource, grants, principals);
    service.cancelRevokes(resource, grants, principals);

    for (String principal : principals) {
      String[] l = service.listRevokes(resource, principal);
      assertEquals(0, l.length);
    }

    // and again
    service.cancelRevokes(resource, grants, principals);

    for (String principal1 : principals) {
      String[] l = service.listRevokes(resource, principal1);
      assertEquals(0, l.length);
    }
  }

  private void clearAll() throws RemoteException {
    for (final String principal : principals) {
      String[] grants = service.listGrants(resource, principal);

      if (grants.length > 0)
        service.cancelGrants(resource, grants, new String[]{principal});

      grants = service.listRevokes(resource, principal);

      if (grants.length > 0)
        service.cancelRevokes(resource, grants, new String[]{principal});
    }
  }
}
