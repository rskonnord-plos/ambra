/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.web;

import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.service.NoUserFoundWithGivenLoginNameException;
import org.plos.service.RegistrationService;
import org.plos.service.UserAlreadyVerifiedException;
import org.plos.service.VerificationTokenInvalidException;

/**
 * Action for verifying a user.
 */
public class ConfirmationAction extends BaseAction {
  private String emailVerificationToken;
  private String loginName;
  private RegistrationService registrationService;

  private static final Log log = LogFactory.getLog(ConfirmationAction.class);

  public String execute() throws Exception {

    try {
      registrationService
              .verifyUser(loginName, emailVerificationToken);
    } catch (final UserAlreadyVerifiedException e) {
      addActionMessage("User already verified:" + loginName);
      return SUCCESS;
    } catch (final VerificationTokenInvalidException e) {
      final String message = "Verification token invalid:"+ emailVerificationToken+", e-mail:" + loginName;
      addActionError(message);
      log.trace(message, e);
      return ERROR;
    } catch (final NoUserFoundWithGivenLoginNameException e) {
      final String message = "No user found with given e-mail:"+ loginName;
      addActionError(message);
      log.trace(message, e);
      return ERROR;
    } catch (final ApplicationException e) {
      addActionError(e.getMessage());
      addFieldError("loginName", e.getMessage());
      log.warn(e, e);
      return ERROR;
    }
    return SUCCESS;
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
