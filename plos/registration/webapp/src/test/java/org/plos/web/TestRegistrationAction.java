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

import com.opensymphony.xwork2.Action;
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

    assertEquals(Action.INPUT, createUser(email, password));
  }

}
