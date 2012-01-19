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

import org.plos.service.NoUserFoundWithGivenLoginNameException;
import org.plos.service.PasswordInvalidException;
import org.plos.service.RegistrationMailer;
import org.plos.service.RegistrationService;
import org.plos.service.UserAlreadyExistsException;
import org.plos.service.password.PasswordServiceException;

import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.FieldExpressionValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.StringLengthFieldValidator;




/**
 * @author stevec
 *
 */
public class ChangeEmailAction extends BaseAction {

  private String login;
  private String password;
  private String newLogin1;
  private String newLogin2;

  private RegistrationMailer registrationVerificationMailer;
  private RegistrationService registrationService;
  private static final Log log = LogFactory.getLog(ResendRegistrationAction.class);
  
  
  public String execute() throws Exception {
    try {
      registrationService.changeLogin(login, password, newLogin1);
    } catch (NoUserFoundWithGivenLoginNameException noUserFoundEx) { 
      if (log.isDebugEnabled()) {
        log.debug ("No user found with login: " + login, noUserFoundEx);
      } 
      addFieldError("login", "No user found for this e-mail/password pair");
      return INPUT;
    } catch (PasswordInvalidException pie) {
      if (log.isDebugEnabled()) {
        log.debug ("No user found with login: " + login + " and password", pie);
      } 
      addFieldError("login", "No user found for this e-mail/password pair");
      return INPUT;
    } catch (PasswordServiceException pse) {
      if (log.isDebugEnabled()) {
        log.debug ("Unable to check password for user: + login", pse);
      } 
      addFieldError("login", "Error checking password.");
      return ERROR;
    } catch (UserAlreadyExistsException uaee) {
      if (log.isDebugEnabled()) {
        log.debug ("User with login: " + newLogin1 + " already exists", uaee);
      } 
      addFieldError("newLogin1", newLogin1 + " is already in use.");
      return INPUT;
    }
    return SUCCESS;
  }
  
  /**
   * @return Returns the login.
   */
  @RequiredStringValidator(message="You must enter your original e-mail address")
  public String getLogin() {
    return login;
  }
  
  /**
   * @param login The login to set.
   */
  public void setLogin(String login) {
    if (login != null) {
      login = login.trim();
    }
    this.login = login;
  }
  
  
  
  
  /**
   * @return Returns the newLogin1.
   */
  @EmailValidator(message="You must enter a valid e-mail address")
  @RequiredStringValidator(message="You must enter an e-mail address")
  @FieldExpressionValidator(fieldName="newLogin2", expression = "newLogin1==newLogin2", message="E-mail addresses must match")
  @StringLengthFieldValidator(maxLength = "256", message="E-mail address must be less than 256 characters")
  public String getNewLogin1() {
    return newLogin1;
  }
  
  /**
   * @param newLogin1 The newLogin1 to set.
   */
  public void setNewLogin1(String newLogin1) {
    if (newLogin1 != null) {
      newLogin1 = newLogin1.trim();
    }
    this.newLogin1 = newLogin1;
  }
  
  /**
   * @return Returns the newLogin2.
   */
  public String getNewLogin2() {
    return newLogin2;
  }
  
  /**
   * @param newLogin2 The newLogin2 to set.
   */
  public void setNewLogin2(String newLogin2) {
    if (newLogin2 != null) {
      newLogin2 = newLogin2.trim();
    }
    this.newLogin2 = newLogin2;
  }
  
  /**
   * @return Returns the password.
   */
  @RequiredStringValidator(message="You must enter a password", shortCircuit=true)
  public String getPassword() {
    return password;
  }
  
  /**
   * @param password The password to set.
   */
  public void setPassword(String password) {
    this.password = password;
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
