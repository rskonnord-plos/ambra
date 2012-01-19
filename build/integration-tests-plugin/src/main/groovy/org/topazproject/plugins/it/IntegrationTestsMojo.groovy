/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.plugins.it;

import org.codehaus.mojo.groovy.GroovyMojo;

/**
 * Dummy to trigger running of the integration tests via a forked lifecycle.
 *
 * @goal run-it
 * @phase integration-test
 * @execute phase="test" lifecycle="integration-tests"
 */
public class IntegrationTestsMojo extends GroovyMojo {
  public void execute() {
  }
}
