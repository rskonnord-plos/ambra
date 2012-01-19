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

import org.plos.registration.User;
import org.plos.BasePlosoneRegistrationTestCase;
import com.opensymphony.xwork2.Action;

public class TestChangePasswordAction extends BasePlosoneRegistrationTestCase {

  public void testShouldChangeUserPassword() throws Exception {
    final String email = "user-changing-their-oldPassword@home.com";
    final String oldPassword = "changethispassword";

    createUser(email, oldPassword);

    final User beforeVerificationUser = getRegistrationService().getUserWithLoginName(email);
    getRegistrationService().verifyUser(email, beforeVerificationUser.getEmailVerificationToken());

    final ChangePasswordAction changePasswordAction = getChangePasswordAction();
    changePasswordAction.setLoginName(email);
    changePasswordAction.setOldPassword(oldPassword);
    changePasswordAction.setNewPassword1("new"+oldPassword);
    assertEquals(Action.SUCCESS, changePasswordAction.execute());
    assertEquals(0, changePasswordAction.getFieldErrors().size());
  }

  public void testShouldFailToChangeUserPasswordIfUserNotVerified() throws Exception {
    final String email = "unverified-user-changing-their-oldPassword@home.com";
    final String oldPassword = "changethispassword";

    createUser(email, oldPassword);

    final ChangePasswordAction changePasswordAction = getChangePasswordAction();
    changePasswordAction.setLoginName(email);
    changePasswordAction.setOldPassword(oldPassword);
    changePasswordAction.setNewPassword1("new"+oldPassword);
    changePasswordAction.setNewPassword2("new"+oldPassword);    
    assertEquals(Action.INPUT, changePasswordAction.execute());
    assertEquals(1, changePasswordAction.getFieldErrors().size());
  }

  public void testShouldFailToChangeUserPasswordIfOldPasswordIsWrong() throws Exception {
    final String email = "testShouldFailToChangeUserPasswordIfOldPasswordIsWrong@home.com";
    final String oldPassword = "changethispassword";

    createUser(email, oldPassword);
    final User beforeVerificationUser = getRegistrationService().getUserWithLoginName(email);
    getRegistrationService().verifyUser(email, beforeVerificationUser.getEmailVerificationToken());

    final ChangePasswordAction changePasswordAction = getChangePasswordAction();
    changePasswordAction.setLoginName(email);
    changePasswordAction.setOldPassword(oldPassword+"change");
    changePasswordAction.setNewPassword1("new"+oldPassword);
    changePasswordAction.setNewPassword2("new"+oldPassword);
    assertEquals(Action.INPUT, changePasswordAction.execute());
    assertEquals(1, changePasswordAction.getFieldErrors().size());
  }

  public void testShouldFailToChangeUserPasswordIfUserNotFound() throws Exception {
    final String email = "testShouldFailToChangeUserPasswordIfUserNotFound@home.com";
    final String oldPassword = "changethispassword";

    final ChangePasswordAction changePasswordAction = getChangePasswordAction();
    changePasswordAction.setLoginName(email);
    changePasswordAction.setOldPassword(oldPassword);
    changePasswordAction.setNewPassword1("new"+oldPassword);
    changePasswordAction.setNewPassword2("new"+oldPassword);
    assertEquals(Action.INPUT, changePasswordAction.execute());
    assertEquals(1, changePasswordAction.getFieldErrors().size());
  }

}
