/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.authentication;

/**
 * A Factory to create client stubs.
 *
 * @author Pradeep Krishnan
 */
public interface StubFactory {
  /**
   * Creates a new client stub to talk to a service.
   *
   * @param service a service that requires authentication
   *
   * @return Returns the newly created client stub
   *
   * @throws Exception on an error in creating the client stub
   */
  public Object newStub(ProtectedService service) throws Exception;

  /**
   * Creates a new proxy client stub that puts a dynamic proxy wrapper on a client stub.
   *
   * @param stub the client stub for which the dynamic proxy wrapper needs to be created
   * @param service a service that requires authentication
   *
   * @return Returns the mewly created proxy instance.
   *
   */
  public Object newProxyStub(Object stub, ProtectedService service);

  /**
   * Rebuild a previously created client stub or create a new one.
   *
   * @param oldStub the stub that is to be rebuilt
   * @param service the service it connects to
   * @param fault the fault that is causing the rebuild
   *
   * @return Returns true if the exception is due to a failed authentication.
   *
   * @throws Exception on an error in creating the client stub
   */
  public Object rebuildStub(Object oldStub, ProtectedService service, Throwable fault)
                     throws Exception;
}
