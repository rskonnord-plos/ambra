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

import org.codehaus.cargo.container.installer.Installer
import org.codehaus.cargo.container.installer.ZipURLInstaller
import org.codehaus.cargo.container.deployable.Deployable
import org.codehaus.cargo.container.deployer.Deployer
import org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration
import org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration
import org.codehaus.cargo.container.tomcat.Tomcat5xStandaloneLocalConfiguration
import org.codehaus.cargo.container.tomcat.TomcatCopyingInstalledLocalDeployer
import org.codehaus.cargo.container.tomcat.Tomcat5xInstalledLocalContainer
import org.codehaus.cargo.container.tomcat.TomcatWAR
import org.codehaus.cargo.util.log.SimpleLogger

/**
 * Tomcat5x Service. 
 *
 * @author Pradeep Krishnan
 */
public abstract class Tomcat5x extends Service {
  public static final String TOMCAT_URL =
           Env.dependencyURL("tomcat", "apache-tomcat", "5.5.23", "zip")
   // "http://www.apache.org/dist/tomcat/tomcat-5/v5.5.23/bin/apache-tomcat-5.5.23.zip"

  private final Installer installer
  private final AbstractLocalConfiguration config
  private final def extraClasspath = []
  private final def confDir, logDir

  /**
   * Create a new Tomcat5x object.
   *
   * @param installDir root directory for this service
   * @param port the http port 
   * @param shutdownPort the port to send shutdown command to
   * @param jvmargs jvm args for tomcat
   */
  public Tomcat5x(String installDir, String port, String shutdownPort, String jvmargs) {
    this(TOMCAT_URL, installDir, port, shutdownPort, jvmargs)
  }

  /**
   * Create a new Tomcat5x object.
   *
   * @param tomcatUrl the url to download the tomcat service zip file from
   * @param installDir root directory for this service
   * @param port the http port 
   * @param shutdownPort the port to send shutdown command to
   * @param jvmargs jvm args for tomcat
   */
  public Tomcat5x(String tomcatUrl, String installDir, String port, 
                  String shutdownPort, String jvmargs) {
    super(installDir)

    confDir = Env.path(installDir, '/tomcat/config')
    logDir  = Env.path(installDir, '/tomcat/log')

    config = new Tomcat5xStandaloneLocalConfiguration(confDir)
    config.setProperty("cargo.servlet.port", port);
    config.setProperty("cargo.logging", "high");
    config.setProperty("cargo.jvmargs", jvmargs);
    config.setProperty("cargo.rmi.port", shutdownPort);

    this.installer = new ZipURLInstaller(new URL(tomcatUrl), 
                                         Env.path(installDir, '/tomcat/install'));
  }

  /*
   * inherited javadoc
   */
  public void install() {
    echo 'Installing Tomcat into ' + installDir + " ..."
    installer.install();
  }

  /**
   * Start tomcat with the given map of deployables.
   *
   * @param deployables a map containing war-context -to- war-file-path mappings
   */
  public void start(Map deployables) {
    echo 'Starting Tomcat ...'

    def ant     = new AntBuilder()
    ant.delete(dir:confDir)
    ant.mkdir(dir:confDir)
    ant.mkdir(dir:logDir)

    Tomcat5xInstalledLocalContainer container = new Tomcat5xInstalledLocalContainer(config)
    container.home = installer.home
    container.systemProperties = sysProperties
    container.extraClasspath = extraClasspath
    container.output = Env.path(logDir, '/output.log')
    //container.logger = new FileLogger(Env.path(installDir, '/tomcat/log/cargo.log')
    container.logger = new SimpleLogger()
    container.start();
    container.waitForCompletion(true)

    Deployer deployer = new TomcatCopyingInstalledLocalDeployer(container);

    for (String context : deployables.keySet()) {
      TomcatWAR war = new TomcatWAR(deployables[context])
      war.setContext(context)
      deployer.deploy(war)
    }
  }

  /*
   * inherited javadoc
   */
  public void stop() {
    echo 'Stopping Tomcat ...'
    Tomcat5xInstalledLocalContainer container = new Tomcat5xInstalledLocalContainer(config)
    container.home = installer.home
    container.stop()
  }

  /**
   * Sets the additional class path jars to be included in the container startuo.
   *
   * @param extraClasspath extra class path to set on the container
   */
  public void setExtraClasspath(def extraClasspath) {
    this.extraClasspath = extraClasspath
  }
}
