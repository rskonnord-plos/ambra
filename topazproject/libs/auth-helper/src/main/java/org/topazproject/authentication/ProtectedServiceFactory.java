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

import java.io.IOException;

import java.net.URISyntaxException;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.Configuration;

/**
 * A factory class to create ProtectedService instances.
 *
 * @author Pradeep Krishnan
 */
public class ProtectedServiceFactory {
  /**
   * Creates a ProtectedService instance based on configuration.
   * 
   * <p>
   * The expected configuration is:
   * <pre>
   *   uri         = the service uri 
   *   auth-method = CAS, BASIC, or NONE
   *   userName    = userName for BASIC auth 
   *   password    = password for BASIC auth
   * </pre>
   * </p>
   *
   * @param config The service configuration.
   * @param session HttpSession to retrieve any run-time info (eg. CASReceipt)
   *
   * @return Returns the newly created instance
   *
   * @throws IOException if there is an error in acquiring auth credentials
   * @throws URISyntaxException thrown from service creation
   */
  public static ProtectedService createService(Configuration config, HttpSession session)
                                        throws IOException, URISyntaxException {
    String uri  = config.getString("uri");
    String auth = config.getString("auth-method");

    if ("CAS".equals(auth))
      return new CASProtectedService(uri, session);

    String userName = config.getString("userName");
    String password = config.getString("password");

    if ("BASIC".equals(auth))
      return new PasswordProtectedService(uri, userName, password);

    // Defaults to unprotected
    return new UnProtectedService(uri);
  }
}
