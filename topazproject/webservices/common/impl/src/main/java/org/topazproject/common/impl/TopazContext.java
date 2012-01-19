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
 * The context for Topaz API calls.
 *
 * @author Pradeep Krishnan
 */
public interface TopazContext {
  /**
   * Lifecycle init.
   *
   * @param object a context specific handle
   */
  public void init(Object object);

  /**
   * Lifecycle destroy.
   */
  public void destroy();

  /**
   * Lifecycle activate. 
   */
  public void activate();

  /**
   * Lifecycle passive. Cancels a previous call to {@link #activate}. Some methods may not be
   * callable weithout being activated.
   */
  public void passivate();

  /**
   * Test to see if context is activated.
   *
   * @return true if activated, false otherwise.
   */
  public boolean isActive();

  /**
   * Gets the servlet context associated with the web-application
   *
   * @return the servlet context
   */
  public ServletContext getServletContext();

  /**
   * Gets the user principal associated with the current Topaz API call.
   *
   * @return the user principal or <code>null</code>
   *
   * @throws IllegalStateException if context is not {@link #activate activate}d
   */
  public Principal getUserPrincipal() throws IllegalStateException;

  /**
   * Gets the HttpSession associated with the current Topaz API call.
   *
   * @return the HttpSession or <code>null</code>
   *
   * @throws IllegalStateException if context is not {@link #activate activate}d
   */
  public HttpSession getHttpSession() throws IllegalStateException;

  /**
   * The username of the user associated with the current Topaz API call.  It is a wrapper around
   * the {@link #getUserPrincipal} method.
   *
   * @return the username or <code>null</code>
   *
   * @throws IllegalStateException if context is not {@link #activate activate}d
   */
  public String getUserName() throws IllegalStateException;

  /**
   * Gets the hostname of the server running this service.
   *
   * @return the server hostname
   */
  public String getServerName();

  /**
   * Gets the base uri for object creation.
   *
   * @return the base uri
   */
  public URI getObjectBaseUri();

  /**
   * Gets the base uri for fedora representation of objects.
   *
   * @return the base uri
   */
  public URI getFedoraBaseUri();

  /**
   * Gets the ItqlHelper handle.
   *
   * @return the ItqlHelper
   *
   * @throws RemoteException if there is an error in creating the handle
   * @throws IllegalStateException if context is not {@link #activate activate}d
   */
  public ItqlHelper getItqlHelper() throws RemoteException, IllegalStateException;

  /**
   * Gets the fedora APIM handle.
   *
   * @return the fedora APIM handle
   *
   * @throws RemoteException if there is an error in creating the handle
   * @throws IllegalStateException if context is not {@link #activate activate}d
   */
  public FedoraAPIM getFedoraAPIM() throws RemoteException, IllegalStateException;

  /**
   * Gets the fedora uploader handle.
   *
   * @return the fedora uploader handle
   *
   * @throws RemoteException if there is an error in creating the handle
   * @throws IllegalStateException if context is not {@link #activate activate}d
   */
  public Uploader getFedoraUploader() throws RemoteException, IllegalStateException;

  /**
   * Gets the fedor APIA handle.
   *
   * @return the fedora APIA handle
   *
   * @throws RemoteException if there is an error in creating the handle
   * @throws IllegalStateException if context is not {@link #activate activate}d
   */
  public FedoraAPIA getFedoraAPIA() throws RemoteException, IllegalStateException;
}
