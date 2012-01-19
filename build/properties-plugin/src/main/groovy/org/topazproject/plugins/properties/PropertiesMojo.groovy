/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.plugins.properties;

import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.groovy.GroovyMojo;

/**
 * Compute and set some properties.
 *
 * @goal set-properties
 * @phase validate
 */
public class PropertiesMojo extends GroovyMojo {
  /**
   * The maven project.
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * The name of the base-version property.
   *
   * @parameter default-value="base-version"
   */
  private String baseVersion;

  public void execute() {
    project.properties[baseVersion] = project.version.replaceAll(/-.*/, '')
  }
}
