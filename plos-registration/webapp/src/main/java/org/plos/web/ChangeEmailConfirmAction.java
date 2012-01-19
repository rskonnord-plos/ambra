/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.ApplicationException;
import org.plos.service.NoUserFoundWithGivenLoginNameException;
import org.plos.service.RegistrationService;
import org.plos.service.VerificationTokenInvalidException;

import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;

/**
 * @author stevec
 *
 */
public class ChangeEmailConfirmAction extends BaseAction {
  private String emailVerificationToken;
  private String loginName;
  private RegistrationService registrationService;
  
  private static final Log log = LogFactory.getLog(ChangeEmailConfirmAction.class);

  public String execute() throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("Verifying change email request for user: " + loginName + " with token: " + emailVerificationToken);
    }
    try {
      registrationService.verifyChangeUser(loginName, emailVerificationToken);
    } catch (final VerificationTokenInvalidException e) {
      final String message = "Verification token invalid: "+ emailVerificationToken+", e-mail: " + loginName;
      addActionError(message);
      if (log.isTraceEnabled()) {
        log.trace(message, e);
      }
      return ERROR;
    } catch (final NoUserFoundWithGivenLoginNameException e) {
      final String message = "No user found with given e-mail address: "+ loginName;
      addActionError(message);
      addFieldError("login", message);
      if (log.isTraceEnabled()) {      
        log.trace(message, e);
      }
      return ERROR;
    } catch (final ApplicationException e) {
      addActionError(e.getMessage());
      addFieldError("loginName", e.getMessage());
      if (log.isWarnEnabled()) {      
        log.warn(e, e);
      }
      return ERROR;
    }
    return SUCCESS;
       
  } 
  
  
  /**
   * Get registrationService
   * @return registrationService
   */
  public RegistrationService getRegistrationService() {
    return this.registrationService;
  }
  
  /**
   * Set registrationService
   * @param registrationService registrationService
   */
  public void setRegistrationService(final RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  /**
   * @return emailVerificationToken
   */
  @RequiredStringValidator(message="Verification token missing")
  public String getEmailVerificationToken() {
    return emailVerificationToken;
  }

  /**
   * Set emailVerificationToken
   * @param emailVerificationToken emailVerificationToken
   */
  public void setEmailVerificationToken(final String emailVerificationToken) {
    this.emailVerificationToken = emailVerificationToken;
  }

  /**
   * @return loginName
   */
  @EmailValidator(type= ValidatorType.SIMPLE, fieldName="loginName", message="Not a valid e-mail address")
  @RequiredStringValidator(message="E-mail address not specified")
  public String getLoginName() {
    return loginName;
  }

  /**
   * Set loginName
   * @param loginName loginName
   */
  public void setLoginName(final String loginName) {
    this.loginName = loginName;
  }

}
