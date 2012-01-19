/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.authentication.reauth;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.StubFactory;

/**
 * An abstract stub factory to create re-authenticating client stubs.
 * 
 * <p>
 * An example is the CAS single-signon tickets. If the http-session for this client stub on the
 * server expires, the client needs to obtain a new ticket from CAS server.
 * </p>
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractReAuthStubFactory implements StubFactory {
  private static Log log = LogFactory.getLog(AbstractReAuthStubFactory.class);

  /*
   * @see org.topazproject.authentication.StubFactpry#newStub
   */
  public abstract Object newStub(ProtectedService service)
                          throws Exception;

  /**
   * Creates a proxy stub that will detect authentication failures and retry the call one more time
   * with a new stub.
   *
   * @param stub the stub that is to be proxied
   * @param service a service that requires authentication
   *
   * @return returns a client stub to access the service
   */
  public Object newProxyStub(Object stub, ProtectedService service) {
    AuthFaultDetector detector = AuthFaultDetectorFactory.getDetector(stub);

    return ReAuthInvocationHandler.createProxy(stub, this, service, detector);
  }

  /*
   * @see org.topazproject.authentication.StubFactpry#rebuildStub
   */
  public Object rebuildStub(Object oldStub, ProtectedService service, Throwable reason)
                     throws Exception {
    return newStub(service);
  }

  /**
   * An invocation handler that will attempt to re-try a method call on an authentication failure
   * after renewing the credentials.
   *
   * @author Pradeep Krishnan
   */
  public static class ReAuthInvocationHandler implements InvocationHandler {
    private Object            target;
    private StubFactory       factory;
    private ProtectedService  service;
    private AuthFaultDetector detector;
    private boolean           retry;

    /**
     * Creates a re-authentication proxy.
     *
     * @param stub The client stub that we are proxying
     * @param factory The stub factory to use to create a new instance
     * @param service A service protected by authentication
     * @param detector The authentication fault detector
     *
     * @return Returns a proxy for the client stub usable to make calls to the service
     */
    public static Object createProxy(Object stub, StubFactory factory, ProtectedService service,
                                     AuthFaultDetector detector) {
      return Proxy.newProxyInstance(stub.getClass().getClassLoader(),
                                    stub.getClass().getInterfaces(),
                                    new ReAuthInvocationHandler(stub, factory, service, detector));
    }

    private ReAuthInvocationHandler(Object target, StubFactory factory, ProtectedService service,
                                    AuthFaultDetector detector) {
      this.target     = target;
      this.factory    = factory;
      this.service    = service;
      this.detector   = detector;
      this.retry      = (detector != null) && service.hasRenewableCredentials();

      if (retry == false)
        log.warn("Authentication retries will not be attempted for " + service.getServiceUri());
    }

    /*
     * @see java.lang.reflect.InvocationHandler#invoke
     */
    public Object invoke(Object proxy, Method method, Object[] args)
                  throws Throwable {
      try {
        return method.invoke(target, args);
      } catch (InvocationTargetException ite) {
        Throwable fault = ite.getCause();

        if (!retry || !detector.isAuthFault(fault) || !service.renew())
          throw fault;

        if (log.isDebugEnabled())
          log.debug("Detected an auth fault", fault);

        try {
          target = factory.rebuildStub(target, service, fault);
        } catch (Exception e) {
          retry = false;
          log.warn("Failed to create a new stub instance to connect to " + service.getServiceUri(),
                   e);
          throw fault;
        }

        if (log.isInfoEnabled())
          log.info("Retrying " + method.getName() + " on " + service.getServiceUri());

        // Now retry the method once more time
        try {
          return method.invoke(target, args);
        } catch (InvocationTargetException e) {
          throw e.getCause();
        }
      }
    }
  }
}
