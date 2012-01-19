/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.service;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.Constants;
import org.plos.web.UserContext;
import org.topazproject.authentication.CASProtectedService;
import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Base service class to be subclassed by any services which have common configuration requirements.
 */
public class BaseConfigurableService {
  private Configuration configuration;
  private UserContext userContext;
  private boolean initCalledInsideUserThread;

  private static final Log log = LogFactory.getLog(BaseConfigurableService.class);

  protected BaseConfigurableService () {
    if (log.isDebugEnabled()) {
      log.debug ("Service constructed: " + this.getClass());
   //   log.debug("", new Exception ());
    }
    
  }
  
  /**
   * @param configuration configuration
   * @return an instance of protected service
   * @throws IOException IOException
   * @throws URISyntaxException URISyntaxException
   */
  protected ProtectedService createProtectedService(Configuration configuration) throws IOException, URISyntaxException {
    if (log.isDebugEnabled()) {
      log.debug("createProctectedService called by " + this.getClass());
      //log.debug("my hash = " + this.hashCode() + " toString: " + this.toString());
    }
    final Map sessionMap = getSessionMap();
    String memberUser = null;
    if (null != sessionMap) {
      memberUser = (String) sessionMap.get(Constants.SINGLE_SIGNON_USER_KEY);
      if (log.isDebugEnabled()) {
        log.debug("creating protected service memberUser = " + memberUser);
      }
    }
    if ((null == sessionMap) || (null == memberUser)) {
      if (log.isDebugEnabled()){
        log.debug("session map = " + sessionMap + " AND memberUser = " + memberUser);
      }
      configuration = new MapConfiguration(cloneConfigMap(configuration));
      configuration.setProperty(Constants.AUTH_METHOD_KEY, Constants.ANONYMOUS_USER_AUTHENTICATION);
      
    }

    return ProtectedServiceFactory.createService(configuration, userContext.getHttpSession());
  }

  private Map<String, String> cloneConfigMap(final Configuration configuration) {
    return new HashMap<String, String>(((MapConfiguration)configuration).getMap());
  }

  /**
   * @return session variables in a map
   */
  public Map getSessionMap() {
    return userContext.getSessionMap();
  }

  /**
   * Set the user's context which can be used to obtain user's session values/attributes
   * @param userContext userContext
   */
  public void setUserContext(final UserContext userContext) {
    this.userContext = userContext;
  }

  /**
   * @return get user context
   */
  public UserContext getUserContext() {
    return userContext;
  }

  /**
   * Create a configuration map initialized with the given config map
   * @param configMap configMap
   * @return a config map
   */
  protected MapConfiguration createMapConfiguration(final Map configMap) {
    return new MapConfiguration(configMap);
  }

  /**
   * @return the configuration info
   */
  public Configuration getConfiguration() {
    return this.configuration;
  }

  /**
   * Set the initial configuration properties
   * @param configMap configMap
   */
  public void setConfigurationMap(final Map configMap) {
    configuration = createMapConfiguration(configMap);
  }

  /**
   * A subclass may want to provide it's own init method
   * @throws java.io.IOException IOException
   * @throws javax.xml.rpc.ServiceException ServiceException
   * @throws java.net.URISyntaxException URISyntaxException
   */
  protected void init() throws IOException, URISyntaxException, ServiceException {
  }

  protected void ensureInitGetsCalledWithUsersSessionAttributes() throws InvalidProxyTicketException {
    log.debug("ensureInit called by : " + this.getClass() + " initCalledInside is : " + initCalledInsideUserThread);
    if (!initCalledInsideUserThread) {
      try {
        init();
      } catch (final CASProtectedService.NoProxyTicketException ex) {
        log.error("No proxy ticket exception thrown for " + userContext.getSessionMap().get(Constants.SINGLE_SIGNON_RECEIPT), ex);
        throw new InvalidProxyTicketException(ex); 
      } catch (final Exception e) {
        log.error("Init failed for service:" + getClass().getName(), e);
        throw new RuntimeException("Init failed for service:" + getClass().getName(), e);
      }
      initCalledInsideUserThread = true;
    }
  }

  protected ProtectedService getProtectedService() throws IOException, URISyntaxException {
    return createProtectedService(this.configuration);
  }
}
