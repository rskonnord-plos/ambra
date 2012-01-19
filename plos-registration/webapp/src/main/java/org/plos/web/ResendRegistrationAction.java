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

import org.plos.service.NoUserFoundWithGivenLoginNameException;
import org.plos.service.RegistrationMailer;
import org.plos.service.UserAlreadyVerifiedException;
import org.plos.service.RegistrationService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;


/**
 * Action that takes in a user's login id and looks him up.  If he is found, then the
 * address confirmation email is sent.  If the user is already verified, it will inform
 * the user.
 * 
 * @author stevec
 *
 */
public class ResendRegistrationAction extends BaseAction {

  private String loginName;
  
  private RegistrationService registrationService;
  private RegistrationMailer registrationVerificationMailer;
  private static final Log log = LogFactory.getLog(ResendRegistrationAction.class);
  
  public String execute() throws Exception {
    try {
      registrationService.sendRegistrationEmail(loginName);
    } catch (final NoUserFoundWithGivenLoginNameException noUserEx) {
      final String message = "No user found for the given e-mail address: " + loginName;
      addActionError(noUserEx.getMessage());
      log.trace(message, noUserEx);
      addFieldError("loginName", message);
      return INPUT;
    } catch (final UserAlreadyVerifiedException uave) {
      if (log.isDebugEnabled()) {
        log.debug("User " + loginName + " is already verified.");
      }
      addFieldError("loginName", loginName + " has already been verified.");
      return INPUT;
    }
    return SUCCESS;
  }

  /**
   * @return Returns the loginName.
   */
  @RequiredStringValidator(message="E-mail address is required")
  public String getLoginName() {
    return loginName;
  }

  /**
   * @param loginName The loginName to set.
   */
  public void setLoginName(String loginName) {
    if (loginName != null) {
      loginName = loginName.trim();
    }
    this.loginName = loginName;
  }

  /**
   * @param registrationService The registrationService to set.
   */
  public void setRegistrationService(RegistrationService registrationService) {
    this.registrationService = registrationService;
  }
  
  /**
   * @return Returns the registrationMailer.
   */
  public RegistrationMailer getRegistrationVerificationMailer() {
    return registrationVerificationMailer;
  }


  /**
   * @param registrationMailer The registrationMailer to set.
   */
  public void setRegistrationVerificationMailer(RegistrationMailer registrationMailer) {
    this.registrationVerificationMailer = registrationMailer;
  }

}
