/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.plugins.reports;

import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.groovy.GroovyMojo;

/**
 * Set up the additional directories for running the reports. This involves adding the groovy
 * sources and the integration-tests sources, and also setting up a different output directory
 * for the tests so as to not clobber the normal ones.
 *
 * @goal configure-directories
 * @phase pre-site
 * @author Ronald Tschalär
 */
public class ConfigDirectoriesMojo extends GroovyMojo {
  /**
   * The maven project.
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * Directories containing sources. The default is the directories ${basedir}/src/main/java,
   * ${basedir}/src/main/groovy, ${project.build.directory}/groovy-stubs/main .
   *
   * @parameter
   */
  private File[] sources;

  /**
   * Directories containing the test sources. The default is the directories
   * ${basedir}/src/test/java, ${basedir}/src/test/groovy, ${basedir}/src/it/java,
   * ${basedir}/src/it/groovy, ${project.build.directory}/groovy-stubs/test,
   * and ${project.build.directory}/groovy-stubs/it .
   *
   * @parameter
   */
  private File[] testSources;

  /**
   * Directories of resources for the test runs. The default is the directories
   * ${basedir}/src/test/resources and ${basedir}/src/it/resources .
   *
   * @parameter
   */
  private Resource[] testResources;

  /**
   * Where the built test classes go.
   *
   * @parameter expression="${project.build.directory}/reports-classes"
   */
  private String testOutputDirectory;

  public void execute() {
    log.debug "Original source directories: ${project.getCompileSourceRoots()}"
    log.debug "Original test-source directories: ${project.getTestCompileSourceRoots()}"
    log.debug "Original test-resource directories: ${project.getTestResources()}"
    log.debug "Original test-output directory: ${project.build.testOutputDirectory}"

    // set up sources directories
    if (!sources)
      sources = [ new File("${project.basedir}/src/main/java"),
                  new File("${project.basedir}/src/main/groovy"),
                  new File("${project.build.directory}/groovy-stubs/main") ]

    project.getCompileSourceRoots().clear()
    for (src in sources)
      project.addCompileSourceRoot(src.getAbsolutePath())

    project.build.sourceDirectory = sources[0].getAbsolutePath()

    // set up test sources directories
    if (!testSources)
      testSources = [ new File("${project.basedir}/src/test/java"),
                      new File("${project.basedir}/src/test/groovy"),
                      new File("${project.basedir}/src/it/java"),
                      new File("${project.basedir}/src/it/groovy"),
                      new File("${project.build.directory}/groovy-stubs/test"),
                      new File("${project.build.directory}/groovy-stubs/it") ]

    project.getTestCompileSourceRoots().clear()
    for (src in testSources)
      project.addTestCompileSourceRoot(src.getAbsolutePath())

    project.build.testSourceDirectory = testSources[0].getAbsolutePath()

    // set up test resources directories
    if (!testResources)
      testResources = [ new Resource(directory: "${project.basedir}/src/test/resources"),
                        new Resource(directory: "${project.basedir}/src/it/resources") ]

    project.getTestResources().clear()
    for (rsrc in testResources)
      project.addTestResource(rsrc)

    // set up test output directory
    project.build.testOutputDirectory = testOutputDirectory

    // log it
    log.info "Reporting source directories: ${project.getCompileSourceRoots()}"
    log.info "Reporting test-source directories: ${project.getTestCompileSourceRoots()}"
    log.info "Reporting test-resource directories: ${project.getTestResources()}"
    log.info "Reporting test-output directory: ${project.build.testOutputDirectory}"
  }
}
