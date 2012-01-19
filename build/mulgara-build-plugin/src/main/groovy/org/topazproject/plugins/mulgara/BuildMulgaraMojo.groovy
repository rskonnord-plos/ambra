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

package org.topazproject.plugins.dependencies;

import org.apache.maven.project.MavenProject;
import org.codehaus.groovy.maven.mojo.GroovyMojo;

/**
 * Build our mulgara jar. This first adds any build classes (including resources), then adds
 * the contents of the mulgara jar, and finally adds the contents of all other dependencies.
 * Duplicate entries are skipped.
 *
 * @goal build
 * @phase package
 * @requiresDependencyResolution compile
 */
public class BuildMulgaraMojo extends GroovyMojo {
  /**
   * The maven project.
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * Where the resulting jar goes.
   *
   * @parameter expression="${project.build.directory}"
   */
  private String outputDirectory;

  /**
   * The name of resulting jar.
   *
   * @parameter expression="${project.build.finalName}"
   */
  private String finalName;

  public void execute() {
    def jar       = new File(outputDirectory, "${finalName}.${project.packaging}")
    def timestamp = new File(outputDirectory, "${finalName}.${project.packaging}.ts")
    def resources = project.build.outputDirectory
    def overrides = ant.fileset(dir:resources).getDirectoryScanner(ant.project).includedFiles

    log.info("Building jar: ${jar}")

    // check if we need to do anything
    if (timestamp.exists()) {
      boolean uptodate = true

      for (art in project.getArtifacts()) {
        if (art.file.lastModified() > timestamp.lastModified()) {
          uptodate = false
          break
        }
      }

      for (file in overrides) {
        if (new File(resources, file).lastModified() > timestamp.lastModified()) {
          uptodate = false
          break
        }
      }

      if (uptodate) {
        log.debug("jar up-to-date - skipping")
        return
      }
    }

    // rebuild
    log.debug("rebuilding jar")
    def mulgaraJar = project.getArtifacts().find
                          { art -> art.groupId == "org.mulgara" && art.artifactId == "mulgara" }

    ant.zip(destfile:jar, defaultexcludes:false) {
      fileset(dir:resources) {
        overrides.each { include(name:it) }
      }
      zipfileset(src:mulgaraJar.file) {
        overrides.each { exclude(name:it) }
      }
      project.getArtifacts().each { art ->
        if (art != mulgaraJar)
          zipfileset(src:art.file, excludes:"META-INF/MANIFEST.MF")
      }
    }

    ant.touch(file:timestamp)
  }
}
