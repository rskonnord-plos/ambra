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
public class PasswordProtectedService implements ProtectedService {
  private String uri;
  private String userName;
  private String password;

  /**
   * Creates a new PasswordProtectedService object.
   *
   * @param uri the service uri
   * @param userName userName for authentication
   * @param password password for authentication
   */
  public PasswordProtectedService(String uri, String userName, String password) {
    this.uri        = uri;
    this.userName   = userName;
    this.password   = password;
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#getServiceUri
   */
  public String getServiceUri() {
    return uri;
  }

  /**
   * Checks to see if there is a username specified.
   *
   * @return Returns true if the username is non-null
   */
  public boolean requiresUserNamePassword() {
    return userName != null;
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#getUserName
   */
  public String getUserName() {
    return userName;
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#getUserName
   */
  public String getPassword() {
    return password;
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#hasRenewableCredentials
   */
  public boolean hasRenewableCredentials() {
    return false;
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#renew
   */
  public boolean renew() {
    return false;
  }
}
