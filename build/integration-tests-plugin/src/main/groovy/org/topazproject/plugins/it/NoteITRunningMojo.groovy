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

import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.groovy.GroovyMojo;

/**
 * Set a property at the beginning of a integration-test run to indicate this is an
 * integration-test run instead of a normal test run.
 *
 * @goal note-it-running
 */
public class NoteITRunningMojo extends GroovyMojo {
  /**
   * The maven project.
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * The name of the property to set in project.
   *
   * @parameter default-value="running-integration-tests"
   */
  private String itRunningProp;

  public void execute() {
    project.properties[itRunningProp] = 'true'
  }
}
