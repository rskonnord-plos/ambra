/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.common.impl;

import java.net.URI;

import java.rmi.RemoteException;

import java.security.Principal;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.topazproject.fedora.client.FedoraAPIA;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.Uploader;

import org.topazproject.mulgara.itql.ItqlHelper;

/**
 * A simple context instance for testing etc.
 *
 * @author Pradeep Krishnan
 */
public class SimpleTopazContext implements TopazContext {
  private ItqlHelper itql;
  private FedoraAPIM apim;
  private Uploader   upld;

  /**
   * Creates a new SimpleTopazContext object.
   *
   * @param itql the itql handl
   * @param apim the apim client stub
   * @param upld the upld handle
   */
  public SimpleTopazContext(ItqlHelper itql, FedoraAPIM apim, Uploader upld) {
    this.itql   = itql;
    this.apim   = apim;
    this.upld   = upld;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public void init(Object object) {
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public void destroy() {
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public void activate() {
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public void passivate() {
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public boolean isActive() {
    return true;
  }

  /**
   * Gets the servlet context associated with the web-application
   *
   * @return the servlet context
   */

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public ServletContext getServletContext() {
    return null;
  }

  /**
   * Gets the user principal associated with the current Topaz API call.
   *
   * @return the user principal or <code>null</code>
   *
   * @throws IllegalStateException if context is not {@link #activate activate}d
   */

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public Principal getUserPrincipal() throws IllegalStateException {
    return null;
  }

  /**
   * Gets the HttpSession associated with the current Topaz API call.
   *
   * @return the HttpSession or <code>null</code>
   *
   * @throws IllegalStateException if called outside a Topaz API call context
   */

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public HttpSession getHttpSession() throws IllegalStateException {
    return null;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public String getUserName() throws IllegalStateException {
    return null;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public String getServerName() {
    return null;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public URI getObjectBaseUri() {
    return null;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public URI getFedoraBaseUri() {
    return null;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public ItqlHelper getItqlHelper() throws RemoteException, IllegalStateException {
    return itql;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public FedoraAPIM getFedoraAPIM() throws RemoteException, IllegalStateException {
    return apim;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public Uploader getFedoraUploader() throws RemoteException, IllegalStateException {
    return upld;
  }

  /*
   * @see org.topazproject.common.impl.TopazContext
   */
  public FedoraAPIA getFedoraAPIA() throws RemoteException, IllegalStateException {
    return null;
  }
}
