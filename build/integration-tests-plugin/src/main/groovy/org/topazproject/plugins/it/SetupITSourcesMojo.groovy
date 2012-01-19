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

import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.groovy.GroovyMojo;

/**
 * Set up the integration test sources.
 *
 * @goal setup-it-locations
 */
public class SetupITSourcesMojo extends GroovyMojo {
  /**
   * The maven project.
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * Source directories containing the integration tests. The default is the single directory
   * ${basedir}/src/it/java .
   *
   * @parameter
   */
  private File[] sources;

  /**
   * Directories of resources for the integration tests. The default is the single directory
   * ${basedir}/src/it/resources .
   *
   * @parameter
   */
  private Resource[] resources;

  /**
   * Where the built test classes go.
   *
   * @parameter expression="${project.build.directory}/it-classes"
   */
  private String outputDirectory;

  public void execute() {
    log.debug "Original Source directories: ${project.getTestCompileSourceRoots()}"
    log.debug "Original Resource directories: ${project.getTestResources()}"
    log.debug "Original source directory: ${project.build.testSourceDirectory}"
    log.debug "Original output directory: ${project.build.testOutputDirectory}"

    // set up test sources directories
    if (!sources)
      sources = [ new File("${project.basedir}/src/it/java") ]

    project.getTestCompileSourceRoots().clear()
    for (src in sources)
      project.addTestCompileSourceRoot(src.getAbsolutePath())

    project.build.testSourceDirectory = sources[0].getAbsolutePath()

    // set up test resources directories
    if (!resources)
      resources = [ new Resource(directory: "${project.basedir}/src/it/resources") ]

    project.getTestResources().clear()
    for (rsrc in resources)
      project.addTestResource(rsrc)

    // set up test output directory
    project.build.testOutputDirectory = outputDirectory

    // log it
    log.info "ITest Source directories added: ${sources}"
    log.info "ITest Resource directories added: ${resources}"
    log.info "ITest output directory: ${outputDirectory}"

    log.debug "Resulting Source directories: ${project.getTestCompileSourceRoots()}"
    log.debug "Resulting Resource directories: ${project.getTestResources()}"
    log.debug "Resulting source directory: ${project.build.testSourceDirectory}"
    log.debug "Resulting output directory: ${project.build.testOutputDirectory}"
  }
}
