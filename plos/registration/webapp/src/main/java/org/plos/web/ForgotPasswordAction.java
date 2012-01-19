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

import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.service.NoUserFoundWithGivenLoginNameException;
import org.plos.service.RegistrationService;

/**
 * Used when a user makes a forgot password request.
 */
public class ForgotPasswordAction extends BaseAction {

  private RegistrationService registrationService;
  private String loginName;

  private static final Log log = LogFactory.getLog(ForgotPasswordAction.class);

  public String execute() throws Exception {
    try {
      registrationService.sendForgotPasswordMessage(loginName);
    } catch (final NoUserFoundWithGivenLoginNameException noUserEx) {
      final String message = "No user found for the given e-mail address:" + loginName;
      addActionError(noUserEx.getMessage());
      log.trace(message, noUserEx);
      addFieldError("loginName", message);
      return INPUT;
    } catch (final ApplicationException e) {
      addActionError(e.getMessage());
      log.error(e, e);
      addFieldError("loginName", e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * @return loginName
   */
  @EmailValidator(type=ValidatorType.SIMPLE, fieldName="loginName", message="Not a valid e-mail address")
  @RequiredStringValidator(message="E-mail address is required")
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

  /**
   * Set registrationService
   * @param registrationService registrationService
   */
  public void setRegistrationService(final RegistrationService registrationService) {
    this.registrationService = registrationService;
  }
}
