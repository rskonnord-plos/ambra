/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.annotations.EmailValidator;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork.validator.annotations.ValidatorType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.registration.User;
import org.plos.service.NoUserFoundWithGivenLoginNameException;
import org.plos.service.RegistrationService;

/**
 * Used when a user makes a forgot password request.
 */
public class ForgotPasswordAction extends BaseAction {

  private RegistrationService registrationService;

  private String loginName;

  private static final Log log = LogFactory.getLog(ForgotPasswordAction.class);

  /**
   * @deprecated
   * to be removed when we change the forgot-password-success.jsp so that it does not display the forgot password link
   */
  private User user;

  public String execute() throws Exception {
    try {
      registrationService.sendForgotPasswordMessage(loginName);

      //TODO to be removed when we change the forgot-password-success.jsp so that it does not display the forgot password link
      {
        final User user = registrationService.getUserWithLoginName(loginName);
        setUser(user);
      }

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
   * Set the user.
   * @param user user
   */
  // TODO to be removed when we change the forgot-password-success.jsp so that it does not display the forgot password link
  private void setUser(final User user) {
    this.user = user;
  }

  /**
   * @return user
   */
  // TODO to be removed when we change the forgot-password-success.jsp so that it does not display the forgot password link
  public User getUser() {
    return user;
  }

  /**
   * Set registrationService
   * @param registrationService registrationService
   */
  public void setRegistrationService(final RegistrationService registrationService) {
    this.registrationService = registrationService;
  }
}
