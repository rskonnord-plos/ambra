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

import org.plos.BasePlosoneRegistrationTestCase;
import org.plos.registration.User;
import com.opensymphony.xwork2.Action;

public class TestConfirmationAction extends BasePlosoneRegistrationTestCase {
  public void testShouldSetUserAsVerified() throws Exception {
    final String email = "viru-verifying@home.com";
    final String password = "virupasswd";
    final User beforeVerificationUser = getRegistrationService().createUser(email, password);
    assertFalse(beforeVerificationUser.isVerified());

    final String emailVerificationToken = beforeVerificationUser.getEmailVerificationToken();
    assertNotNull(emailVerificationToken);
    assertTrue(emailVerificationToken.length() > 0);

    final ConfirmationAction confirmationAction = getConfirmationAction();
    confirmationAction.setLoginName(email);
    confirmationAction.setEmailVerificationToken(emailVerificationToken);
    assertEquals(Action.SUCCESS, confirmationAction.execute());

    final User verifiedUser = getRegistrationService().getUserWithLoginName(email);
    assertTrue(verifiedUser.isVerified());
  }

  public void testShouldNotVerifyUserAsVerificationTokenIsInvalid() throws Exception {
    final String email = "viru-verifying-another-time@home.com";
    final User beforeVerificationUser = getRegistrationService().createUser(email, "virupasswd");

    assertFalse(beforeVerificationUser.isVerified());
    final String emailVerificationToken = beforeVerificationUser.getEmailVerificationToken();

    assertNotNull(emailVerificationToken);
    assertTrue(emailVerificationToken.length() > 0);

    final ConfirmationAction confirmationAction = getConfirmationAction();
    confirmationAction.setLoginName(email);
    //change the verification token
    confirmationAction.setEmailVerificationToken(emailVerificationToken+"11");
    assertEquals(Action.ERROR, confirmationAction.execute());

    final User verifiedUser = getRegistrationService().getUserWithLoginName(email);
    assertFalse(verifiedUser.isVerified());
  }

  public void testVerifyUserShouldFailAsLoginNameDoesNotExist() throws Exception {
    final String email = "viru-verifying-a-loginnamethatdoes-notexist@home.com";

    assertNull(getRegistrationService().getUserWithLoginName(email));

    final ConfirmationAction confirmationAction = getConfirmationAction();
    confirmationAction.setLoginName(email);
    confirmationAction.setEmailVerificationToken("emailVerificationToken");
    assertEquals(Action.ERROR, confirmationAction.execute());
  }

  public void testShouldGiveErrorMessageAsUserIsAlreadyVerified() throws Exception {
    final String email = "viru-verifying-again@home.com";
    final String password = "virupasswd";

    createUser(email, password);
    final User beforeVerificationUser = getRegistrationService().getUserWithLoginName(email);
    assertFalse(beforeVerificationUser.isVerified());

    final String emailVerificationToken = beforeVerificationUser.getEmailVerificationToken();
    assertNotNull(emailVerificationToken);
    assertTrue(emailVerificationToken.length() > 0);

    final ConfirmationAction confirmationAction = getConfirmationAction();
    confirmationAction.setLoginName(email);
    confirmationAction.setEmailVerificationToken(emailVerificationToken);
    assertEquals(Action.SUCCESS, confirmationAction.execute());

    //try to verify the email address again
    assertEquals(Action.SUCCESS, confirmationAction.execute());

    final User verifiedUser = getRegistrationService().getUserWithLoginName(email);
    assertTrue(verifiedUser.isVerified());
  }

}
