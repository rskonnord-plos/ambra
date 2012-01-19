/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.service;

/**
 * Provides service implementations for all kinds of services.
 */
public class ServiceFactory {
  private PersistenceService persistenceService;
  private RegistrationService registrationService;

  /**
   * @return registrationService
   */
  public RegistrationService getRegistrationService() {
    return registrationService;
  }

  /**
   * @param registrationService registrationService to set
   */
  public void setRegistrationService(final RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  /**
   * @return persistenceService
   */
  public PersistenceService getPersistenceService() {
    return persistenceService;
  }

  /**
   * @param persistenceService persistenceService to set
   */
  public void setPersistenceService(final PersistenceService persistenceService) {
    this.persistenceService = persistenceService;
  }

}
