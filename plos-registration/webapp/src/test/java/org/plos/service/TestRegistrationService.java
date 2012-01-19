/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.service;

import org.plos.BasePlosoneRegistrationTestCase;
import org.plos.registration.User;

import java.sql.Timestamp;

public class TestRegistrationService extends BasePlosoneRegistrationTestCase {

  public void testCreateNewUserWithExpectedValues() throws Exception {
    final String loginName = "fedoraAdmin";
    final User user = getRegistrationService().createUser(loginName, "fedoraAdmin");
    assertNotNull(user.getId());
    assertEquals(user.getLoginName(), loginName);
    assertNotNull(user.getPassword());
    assertFalse(user.isVerified());
    assertFalse(user.isActive());
  }

  public void testVerifyUser() throws Exception {
    final String username = "viru@home.com";
    final String password = "virender";
    final User user = getRegistrationService().createUser(username, password);
    getRegistrationService().setVerified(user);
    assertTrue(getPasswordDigestService()
                .verifyPassword(
                    password,
                    getRegistrationService().getUserWithLoginName(username).getPassword()));
    assertTrue(user.isVerified());
    assertTrue(user.isActive());
  }

  public void testDeactivatedUser() throws Exception {
    final String email = "susie@home.com";
    getRegistrationService().createUser(email, "susan");
    final User user = getRegistrationService().getUserWithLoginName(email);
    getRegistrationService().deactivate(user);
    assertFalse(user.isVerified());
    assertFalse(user.isActive());
  }

  public void testUpdateUpdatesUpdatedTime() throws Exception {
    final String email = "updatetimestamptest@home.com";
    final User user = getRegistrationService().createUser(email, "updatepasswd");
    final Timestamp initialUpdatedOn = user.getUpdatedOn();
    assertNotNull(user.getId());
    Thread.sleep(2);
    getRegistrationService().deactivate(user);
    final User updatedUser = getRegistrationService().getUserWithLoginName(email);
    final Timestamp newUpdatedOn = updatedUser.getUpdatedOn();
    assertFalse(initialUpdatedOn.after(newUpdatedOn));
  }

  public void testUserPasswdSavedInDatabaseShouldBeDifferentFromWhatUserEntered() throws Exception {
    final String email = "viru-verifying-for-password-digest@home.com";
    final String password = "virupasswd";
    final User saveUser = getRegistrationService().createUser(email, password);
    assertTrue(getPasswordDigestService().verifyPassword(
            password,
            getRegistrationService().getUserWithLoginName(email).getPassword()));
    assertFalse(saveUser.getPassword().equalsIgnoreCase(password));
  }

}
