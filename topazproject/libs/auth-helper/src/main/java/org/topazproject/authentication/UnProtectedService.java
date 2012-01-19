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
public class UnProtectedService implements ProtectedService {
  private String uri;

  /**
   * Creates a new UnProtectedService object.
   *
   * @param uri the service uri
   */
  public UnProtectedService(String uri) {
    this.uri = uri;
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#getServiceUri
   */
  public String getServiceUri() {
    return uri;
  }

  /**
   * Returns false always.
   *
   * @return Returns false
   */
  public boolean requiresUserNamePassword() {
    return false;
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#getUserName
   */
  public String getUserName() {
    return null;
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#getUserName
   */
  public String getPassword() {
    return null;
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
