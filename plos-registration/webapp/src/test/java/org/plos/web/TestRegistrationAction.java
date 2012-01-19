/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.web;

import com.opensymphony.xwork.Action;
import org.plos.BasePlosoneRegistrationTestCase;

/**
 *
 */
public class TestRegistrationAction extends BasePlosoneRegistrationTestCase {
  public void testShouldCreateAUserAccount() throws Exception {
    final String email = "viru-creating-a-user-account@home.com";
    final String password = "virupasswd";

    assertEquals(Action.SUCCESS, createUser(email, password));
  }

  public void testShouldFailToCreateAnotherAccountWithSameEmail() throws Exception {
    final String email = "viru-creating-a-account-twice@home.com";
    final String password = "virupasswd";

    assertEquals(Action.SUCCESS, createUser(email, password));

    assertEquals(Action.ERROR, createUser(email, password));
  }

}
