/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/libs/article-util/src/main/groovy/#$
 * $Id: Delete.groovy 2686 2007-05-15 08:22:38Z ebrown $
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plosone.it

/**
 * Mulgara service manager.
 *
 * @author Pradeep Krishnan
 */
public class Mulgara extends Tomcat5x {

  private String mulgaraWar

  /**
   * Create a Mulgara service instance.
   *
   * @param installDir root directory for this service
   */
  public Mulgara(String installDir) {
    this(installDir, 
      Env.dependencyPath('org.topazproject', 'mulgara-service', Env.pomVersion(), "war"))
  }

  /**
   * Create a Mulgara service instance.
   *
   * @param installDir root directory for this service
   * @param mulgaraWar path to mulgara war file
   */
  public Mulgara(String installDir, String mulgaraWar) {
    this(Tomcat5x.TOMCAT_URL, installDir, mulgaraWar)
  }

  /**
   * Create a Mulgara service instance.
   *
   * @param tomcatUrl the url to download the tomcat service zip file from
   * @param installDir root directory for this service
   * @param mulgaraWar path to mulgara war file
   */
  public Mulgara(String tomcatUrl, String installDir, String mulgaraWar) {
    this(tomcatUrl, installDir, mulgaraWar, '9091', '9291', '-server -Xmx200m')
  }

  /**
   * Create a Mulgara service instance.
   *
   * @param tomcatUrl the url to download the tomcat service zip file from
   * @param installDir root directory for this service
   * @param mulgaraWar path to mulgara war file
   * @param port the http port 
   * @param shutdownPort the port to send shutdown command to
   * @param jvmargs jvm args for tomcat
   */
  public Mulgara(String tomcatUrl, String installDir, String mulgaraWar, String port, 
                 String shutdownPort, String jvmargs) {
    super(tomcatUrl, installDir, port, shutdownPort, jvmargs)
    this.mulgaraWar = mulgaraWar
  }

  /*
   * inherited javadoc
   */
  public void start() {
    super.start(['/mulgara-service':mulgaraWar])
  }

  /*
   * inherited javadoc
   */
  public void waitFor() {
    waitFor('http://localhost:9091/mulgara-service/services/ItqlBeanService')
  }

  /*
   * inherited javadoc
   */
  public void rebuild() {
   // nothing to do
  }

}
