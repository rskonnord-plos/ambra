/* $HeadURL::                                                                                      $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plosone.it.jwebunit;

import net.sourceforge.jwebunit.junit.WebTester;

import org.plosone.it.PlosOneDAO;

/**
 * An extension for WebTester for keeping some plosone specific states.
 *
 * @author Pradeep Krishnan
 */
public class PlosOneWebTester extends WebTester {
  private boolean loggedIn = false;
  private boolean admin    = false;
  private boolean initialized = false;

  private PlosOneDAO dao;

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

  public PlosOneDAO getDao() {
    return dao;
  }

  public void setDao(PlosOneDAO dao) {
    this.dao = dao;
  }
}
