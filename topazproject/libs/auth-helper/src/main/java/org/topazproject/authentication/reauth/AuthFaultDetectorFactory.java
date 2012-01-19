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

/**
 * A factory class that can generate auth fault detectors.  Auth fault detectors can analyze an
 * exception and see if that is caused by an authentication failure. eg. for HTTP SOAP rpc calls,
 * the exceptions thrown from an axis client stub, can be analyzed for an HTTP status code of 401
 * to determine that the call failure was due to an authentication failure.
 *
 * @author Pradeep Krishnan
 */
public class AuthFaultDetectorFactory {
  // Known stub classes...
  private static Class[] classes = { org.apache.axis.client.Stub.class };

  // ... and the corresponding auth fault detectors
  private static AuthFaultDetector[] detectors = { new AxisAuthFaultDetector() };

  /**
   * Gets an auth fault detector for a protected service client stub.
   *
   * @param stub the client stub used to access a service that requires authentication
   *
   * @return Returns the detector or <code>null</code>
   */
  public static AuthFaultDetector getDetector(Object stub) {
    for (int i = 0; i < classes.length; i++)
      if (classes[i].isInstance(stub))
        return detectors[i];

    return null;
  }
}
