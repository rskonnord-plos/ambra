/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
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

package org.topazproject.plugins.it;

import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.codehaus.groovy.maven.mojo.GroovyMojo;

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
