/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos;

import org.plos.service.RegistrationService;
import org.plos.service.password.PasswordDigestService;
import org.plos.web.ChangePasswordAction;
import org.plos.web.ConfirmationAction;
import org.plos.web.ForgotPasswordAction;
import org.plos.web.RegisterAction;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Base test case for the registration unit tests. It provides spring injection from one of its superclasses.
 *
 */
public abstract class BasePlosoneRegistrationTestCase extends AbstractDependencyInjectionSpringContextTests {
  protected RegistrationService registrationService;
  private ConfirmationAction confirmationAction;
  private RegisterAction registerAction;
  private PasswordDigestService passwordDigestService;
  private ChangePasswordAction changePasswordAction;

  protected String[] getConfigLocations() {
    return new String[] {"testApplicationContext.xml"};
  }

  public final void setRegistrationService(final RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  protected ConfirmationAction getConfirmationAction() {
    return confirmationAction;
  }

  public void setConfirmationAction(final ConfirmationAction confirmationAction) {
    this.confirmationAction = confirmationAction;
  }

  public RegistrationService getRegistrationService() {
    return registrationService;
  }

  protected RegisterAction getRegistrationAction() {
    return registerAction;
  }

  public void setRegisterAction(final RegisterAction registerAction) {
    this.registerAction = registerAction;
  }

  protected ForgotPasswordAction getForgotPasswordAction() {
    final ForgotPasswordAction forgotPasswordAction = new ForgotPasswordAction();
    forgotPasswordAction.setRegistrationService(getRegistrationService());
    return forgotPasswordAction;
  }

  public void setPasswordDigestService(final PasswordDigestService passwordDigestService) {
    this.passwordDigestService = passwordDigestService;
  }

  protected PasswordDigestService getPasswordDigestService() {
    return passwordDigestService;
  }

  protected ChangePasswordAction getChangePasswordAction() {
    return changePasswordAction;
  }

  public void setChangePasswordAction(final ChangePasswordAction changePasswordAction) {
    this.changePasswordAction = changePasswordAction;
  }

  protected final String createUser(final String email, final String password) throws Exception {
    final RegisterAction registerAction = getRegistrationAction();
    registerAction.setLoginName1(email);
    registerAction.setLoginName2(email);
    registerAction.setPassword1(password);
    registerAction.setPassword2(password);
    return registerAction.execute();
  }
}
