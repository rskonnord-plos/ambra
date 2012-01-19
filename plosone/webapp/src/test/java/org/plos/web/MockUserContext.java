/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.web;

import javax.servlet.http.HttpSession;

public class MockUserContext extends UserContext {
  public HttpSession getHttpSession() {
    return null;
  }
}
