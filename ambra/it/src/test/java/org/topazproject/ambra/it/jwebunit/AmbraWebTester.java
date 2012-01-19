/* $HeadURL::                                                                                      $
 * $Id$
 *
 * Copyright (c) 2007-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.ambra.it.jwebunit;

import net.sourceforge.jwebunit.junit.WebTester;

import org.topazproject.ambra.it.AmbraDAO;

/**
 * An extension for WebTester for keeping some plosone specific states.
 *
 * @author Pradeep Krishnan
 */
public class AmbraWebTester extends WebTester {
  private boolean loggedIn = false;
  private boolean admin    = false;
  private boolean initialized = false;

  private AmbraDAO dao;

  /**
   * Gets the login state.
   *
   * @return the login state of this tester
   */
  public boolean isLoggedIn() {
    return loggedIn;
  }

  /**
   * Set the login state of this tester
   *
   * @param loggedIn the state to set
   */
  public void setLoggedIn(boolean loggedIn) {
    this.loggedIn = loggedIn;
  }

  /**
   * Gets the admin state of this tester.
   *
   * @return admin if the logged in user role is set to admin
   */
  public boolean isAdmin() {
    return admin;
  }

  /**
   * Set admin.
   *
   * @param admin the value to set.
   */
  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  public void beginAt(String aRelativeURL) {
    super.beginAt(aRelativeURL);
    // need to reset our states since this is a new browser session
    admin = false;
    loggedIn = false;
    initialized = true;
  }

  public void gotoPage(String aRelativeURL) {
    if (!initialized)
      beginAt(aRelativeURL);
    else
      super.gotoPage(aRelativeURL);
  }

  public AmbraDAO getDao() {
    return dao;
  }

  public void setDao(AmbraDAO dao) {
    this.dao = dao;
  }
}
