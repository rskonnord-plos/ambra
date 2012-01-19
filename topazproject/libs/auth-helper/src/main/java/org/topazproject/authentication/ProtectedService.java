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
 * An interface to abstract out the authentication mechanism used to access a service.
 *
 * @author Pradeep Krishnan
 */
public interface ProtectedService {
  /**
   * Gets the Uri of the service.
   *
   * @return Returns the service uri
   */
  public String getServiceUri();

  /**
   * Tests to see if this service requires a username/password pair.
   *
   * @return Returns true if this service requires a user-name and password.
   */
  public boolean requiresUserNamePassword();

  /**
   * Gets the username to use to authenticate with the service.
   *
   * @return Returns the username or null
   */
  public String getUserName();

  /**
   * Gets the password to use to authenticate with the service.
   *
   * @return Returns the password or null
   */
  public String getPassword();

  /**
   * Tests to see if this service has credentials that may need to be renewed either after a
   * certain time or after a certain number of uses.
   * 
   * <p>
   * eg. CAS single signon tickets are valid only for a single use. However if the server is
   * maintaining an HTTP session, the credentials are valid as long as the session is alive. But
   * if the session goes down, a new ticket is needed from CAS server
   * </p>
   *
   * @return Returns true if the credentials are renewable, false otherwise
   */
  public boolean hasRenewableCredentials();

  /**
   * Renew any expired authentication credentials.
   *
   * @return Returns true if the credentials are renewed. false if there is no change.
   */
  public boolean renew();
}
