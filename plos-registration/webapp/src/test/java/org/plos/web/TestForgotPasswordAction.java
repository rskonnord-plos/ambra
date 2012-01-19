/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.web;

import org.plos.registration.User;
import org.plos.BasePlosoneRegistrationTestCase;
import com.opensymphony.xwork.Action;

public class TestForgotPasswordAction extends BasePlosoneRegistrationTestCase {
  public void testShouldFailToAcceptForgotPasswordEmailAsItIsNotRegistered() throws Exception {
    final String email = "viru-forgot-password-not-registered@home.com";

    final ForgotPasswordAction forgotPasswordAction = getForgotPasswordAction();
    forgotPasswordAction.setLoginName(email);
    assertEquals(Action.ERROR, forgotPasswordAction.execute());
  }

  public void testShouldSendEmailForForgotPasswordEmailEvenIfTheEmailItIsNotVerified() throws Exception {
    final String email = "viru-forgot-password-not-verified-yet@home.com";

    assertEquals(Action.SUCCESS, createUser(email, "virupasswd"));
    final User beforeVerificationUser = getRegistrationService().getUserWithLoginName(email);
    assertFalse(beforeVerificationUser.isVerified());

    final ForgotPasswordAction forgotPasswordAction = getForgotPasswordAction();
    forgotPasswordAction.setLoginName(email);
    assertEquals(Action.SUCCESS, forgotPasswordAction.execute());
    assertTrue(forgotPasswordAction.getActionErrors().isEmpty());
  }

  public void testShouldAcceptForgotPasswordRequestIfItIsNotActive() throws Exception {
    final String email = "viru-forgot-password-not-active-yet@home.com";
    createUser(email, "virupasswd");
    final User beforeVerificationUser = getRegistrationService().getUserWithLoginName(email);
    assertFalse(beforeVerificationUser.isActive());

    final ForgotPasswordAction forgotPasswordAction = getForgotPasswordAction();
    forgotPasswordAction.setLoginName(email);
    assertEquals(Action.SUCCESS, forgotPasswordAction.execute());
    assertTrue(forgotPasswordAction.getActionErrors().isEmpty());
  }

  public void testShouldSendEmailForForgotPasswordEmailIfTheEmailIsVerifiedAndActive() throws Exception {
    final String email = "viru-forgot-password-verified-and-active@home.com";
    createUser(email, "virupasswd");

    final User beforeVerificationUser = getRegistrationService().getUserWithLoginName(email);

    final ConfirmationAction confirmationAction = getConfirmationAction();
    confirmationAction.setLoginName(email);
    confirmationAction.setEmailVerificationToken(beforeVerificationUser.getEmailVerificationToken());

    assertEquals(Action.SUCCESS, confirmationAction.execute());

    final ForgotPasswordAction forgotPasswordAction = getForgotPasswordAction();
    forgotPasswordAction.setLoginName(email);
    assertEquals(Action.SUCCESS, forgotPasswordAction.execute());
    assertTrue(forgotPasswordAction.getActionErrors().isEmpty());
  }

  public void testShouldSendFailToVerifyForgotPasswordTokenIfItIsWrong() throws Exception {
    final String email = "viru-forgot-password-verified-and-active-number2@home.com";
    createUser(email, "virupasswd");

    final User beforeVerificationUser = getRegistrationService().getUserWithLoginName(email);

    final ConfirmationAction confirmationAction = getConfirmationAction();
    confirmationAction.setLoginName(email);
    confirmationAction.setEmailVerificationToken(beforeVerificationUser.getEmailVerificationToken());
    assertEquals(Action.SUCCESS, confirmationAction.execute());
    assertTrue(confirmationAction.getActionErrors().isEmpty());

    final ForgotPasswordAction forgotPasswordAction = getForgotPasswordAction();
    forgotPasswordAction.setLoginName(email);
    assertEquals(Action.SUCCESS, forgotPasswordAction.execute());
    assertTrue(forgotPasswordAction.getActionErrors().isEmpty());

    final User forgotPasswordUser = getRegistrationService().getUserWithLoginName(email);
    assertNotNull(forgotPasswordUser.getResetPasswordToken());
    assertTrue(forgotPasswordUser.getResetPasswordToken().length() > 0);
  }

}
