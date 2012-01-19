/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.permissions;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.rmi.RemoteException;

import java.util.Arrays;
import java.util.HashSet;

import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

/**
 * Simple tests for the permissions service
 *
 * @author Pradeep Krishnan
 */
public class PermissionsServiceTest extends TestCase {
  private Permissions service;
  private String      resource = "foo:bar";
  private String[]    grants   = new String[] { "foo:create", "foo:get", "foo:set", "foo:delete" };
  private String[]    revokes    = new String[] { "foo:list-all", "foo:purge" };
  private String[]    principals =
    new String[] { "user:joe", "group:joe-friends", "group:joe-family" };
  private String      permission      = "foo:set";
  private String[]    implied         = new String[] { "bar:get", "bar:set" };
  private String[]    propagated1     = new String[] { "foo:foo" };
  private String[]    propagated2     = new String[] { "bar:foo", "bar:bar" };
  private String[]    transPropagated = new String[] { "foo:foo", "bar:foo", "bar:bar" };

  /**
   * Creates a new PermissionsServiceTest object.
   *
   * @param testName junit test name
   */
  public PermissionsServiceTest(String testName) {
    super(testName);
  }

  /*
   * Test setup
   *
   */
  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    String uri = "http://localhost:9997/ws-permissions/services/PermissionsServicePort";
    service = PermissionsClientFactory.create(uri);

    //
    clearAll();
  }

  /**
   * Test for grants.
   *
   * @throws RemoteException on error
   */
  public void test1Grants() throws RemoteException {
    service.grant(resource, grants, principals);

    for (int i = 0; i < principals.length; i++) {
      String[] l = service.listGrants(resource, principals[i]);
      assertEquals("grants equal", new HashSet(Arrays.asList(l)), new HashSet(Arrays.asList(grants)));
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

    for (int i = 0; i < principals.length; i++) {
      String[] l = service.listGrants(resource, principals[i]);
      assertEquals("grants empty", l.length, 0);
    }

    // and again
    service.cancelGrants(resource, grants, principals);

    for (int i = 0; i < principals.length; i++) {
      String[] l = service.listGrants(resource, principals[i]);
      assertEquals("grants empty", l.length, 0);
    }
  }

  /**
   * Test for revokes.
   *
   * @throws RemoteException on error
   */
  public void test3Revokes() throws RemoteException {
    service.revoke(resource, revokes, principals);

    for (int i = 0; i < principals.length; i++) {
      String[] l = service.listRevokes(resource, principals[i]);
      assertEquals("revokes equal", new HashSet(Arrays.asList(l)),
                   new HashSet(Arrays.asList(revokes)));
    }
  }

  /**
   * Test for cancel revokes.
   *
   * @throws RemoteException on error
   */
  public void test4CancelRevokes() throws RemoteException {
    service.revoke(resource, grants, principals);
    service.cancelRevokes(resource, grants, principals);

    for (int i = 0; i < principals.length; i++) {
      String[] l = service.listRevokes(resource, principals[i]);
      assertEquals("revokes empty", l.length, 0);
    }

    // and again
    service.cancelRevokes(resource, grants, principals);

    for (int i = 0; i < principals.length; i++) {
      String[] l = service.listRevokes(resource, principals[i]);
      assertEquals("revokes empty", l.length, 0);
    }
  }

  /**
   * Test for imply permissions.
   *
   * @throws RemoteException on error
   */
  public void test5ImplyPermissions() throws RemoteException {
    service.implyPermissions(permission, implied);

    String[] l = service.listImpliedPermissions(permission, false);
    arraysEqual(implied, l);
  }

  /**
   * Test for cancel implies.
   *
   * @throws RemoteException on error
   */
  public void test6CancelImplyPermissions() throws RemoteException {
    service.implyPermissions(permission, implied);
    service.cancelImplyPermissions(permission, implied);

    String[] l = service.listImpliedPermissions(permission, false);
    assertEquals(0, l.length);
  }

  /**
   * Test for propagagate permissions.
   *
   * @throws RemoteException on error
   */
  public void test7PropagatePermissions() throws RemoteException {
    service.propagatePermissions(resource, propagated1);

    for (int i = 0; i < propagated1.length; i++)
      service.propagatePermissions(propagated1[i], propagated2);

    String[] l = service.listPermissionPropagations(resource, false);
    arraysEqual(propagated1, l);

    l = service.listPermissionPropagations(resource, true);
    arraysEqual(transPropagated, l);
  }

  /**
   * Test for cancel propagate permissions.
   *
   * @throws RemoteException on error
   */
  public void test8CancelPropagatePermissions() throws RemoteException {
    service.propagatePermissions(resource, propagated1);

    for (int i = 0; i < propagated1.length; i++)
      service.propagatePermissions(propagated1[i], propagated2);

    service.cancelPropagatePermissions(resource, propagated1);

    for (int i = 0; i < propagated1.length; i++)
      service.cancelPropagatePermissions(propagated1[i], propagated2);

    String[] l = service.listPermissionPropagations(resource, false);
    assertEquals(0, l.length);

    l = service.listPermissionPropagations(resource, true);
    assertEquals(0, l.length);
  }

  /**
   * Test for isGranted and isRevoked.
   *
   * @throws RemoteException on error
   */
  public void test9EffectivePermission() throws RemoteException {
    service.grant(resource, grants, principals);
    service.revoke(resource, revokes, principals);
    service.propagatePermissions(resource, propagated1);

    for (int i = 0; i < propagated1.length; i++)
      service.propagatePermissions(propagated1[i], propagated2);

    service.implyPermissions(permission, implied);

    assertTrue(service.isGranted("bar:bar", "bar:set", principals[0]));
    assertTrue(service.isRevoked("bar:bar", "foo:purge", principals[0]));
    assertTrue(!service.isGranted("bar:bar", "foo:purge", "some:user"));
    assertTrue(!service.isRevoked("bar:bar", "foo:purge", "some:user"));
  }

  private void clearAll() throws RemoteException {
    for (int i = 0; i < principals.length; i++) {
      String[] l = service.listGrants(resource, principals[i]);

      if (l.length > 0)
        service.cancelGrants(resource, l, new String[] { principals[i] });

      l = service.listRevokes(resource, principals[i]);

      if (l.length > 0)
        service.cancelRevokes(resource, l, new String[] { principals[i] });
    }

    service.cancelImplyPermissions(resource, implied);
    service.cancelPropagatePermissions(resource, propagated1);

    for (int i = 0; i < propagated1.length; i++)
      service.cancelPropagatePermissions(propagated1[i], propagated2);
  }

  private void arraysEqual(Object[] s, Object[] r) {
    assertEquals(s.length, r.length);
    Arrays.sort(s);
    Arrays.sort(r);

    for (int i = 0; i < s.length; i++)
      assertEquals(s[i], r[i]);
  }
}
