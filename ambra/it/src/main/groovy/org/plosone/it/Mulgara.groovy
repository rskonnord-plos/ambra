/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/libs/article-util/src/main/groovy/#$
 * $Id: Delete.groovy 2686 2007-05-15 08:22:38Z ebrown $
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

package org.plosone.it

/**
 * Mulgara service manager.
 *
 * @author Pradeep Krishnan
 */
public class Mulgara extends Service {

  private String mulgaraJar
  private int    httpPort
  private String jvmargs

  /**
   * Create a Mulgara service instance.
   *
   * @param installDir root directory for this service
   */
  public Mulgara(String installDir) {
    this(installDir, 
         Env.dependencyPath('org.topazproject', 'mulgara-service', Env.pomVersion(), "jar"))
  }

  /**
   * Create a Mulgara service instance.
   *
   * @param installDir root directory for this service
   * @param mulgaraJar path to mulgara jar file
   */
  public Mulgara(String installDir, String mulgaraJar) {
    this(installDir, mulgaraJar, -1, null);
  }

  /**
   * Create a Mulgara service instance.
   *
   * @param installDir root directory for this service
   * @param mulgaraJar path to mulgara jar file
   * @param httpPort   the http-port for mulgara
   * @param jvmargs jvm args for tomcat
   */
  public Mulgara(String installDir, String mulgaraJar, int httpPort, String jvmargs) {
    super(installDir);
    this.mulgaraJar = mulgaraJar
    this.httpPort   = httpPort > 0 ? httpPort : 34679
    this.jvmargs    = jvmargs ?: ""
  }

  public void install() {
    // nothing to do
  }

  private void runMulgara(boolean start, boolean wait) {
    def logConf = sysProperties.'log4j.configuration'
    def dbDir   = sysProperties.'ambra.topaz.tripleStore.mulgara.databaseDir'
    String cmd =
      "java ${jvmargs} -jar ${mulgaraJar} -s topazproject -a ${dbDir} -l ${logConf} -p ${httpPort}"
    if (!start)
      cmd += " -x"

    try {
      def proc = cmd.execute()
      new Thread({ proc.in.eachLine()  { line -> echo line } } as Runnable).start()
      new Thread({ proc.err.eachLine() { line -> echo line } } as Runnable).start()

      if (wait)
        proc.waitFor()
    } catch (Exception e) {
      echo e
    }
  }

  public void start() {
    echo 'Starting Mulgara ...'
    runMulgara(true, false)
  }

  public void stop() {
    echo 'Stopping Mulgara ...'
    runMulgara(false, true)
  }

  /*
   * inherited javadoc
   */
  public void waitFor() {
    waitFor("http://localhost:${httpPort}/")
    echo 'mulgara is up'
  }

  /*
   * inherited javadoc
   */
  public void rebuild() {
   // nothing to do
  }
}
