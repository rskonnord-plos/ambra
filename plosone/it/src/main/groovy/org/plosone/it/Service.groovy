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
 * A base class for all services managed by the test environment.
 *
 * @author Pradeep Krishnan
 */
public abstract class Service {

  protected String installDir
  private Map properties = [:]

  /**
   * Create a new Service object
   * 
   * @param installDir the installation root
   */
  public Service(String installDir) {
    this.installDir = installDir
  }

  /**
   * Install this service.
   */
  public abstract void install()

  /**
   * Start this service.
   */
  public abstract void start()

  /**
   * Stop this service.
   */
  public abstract void stop()

  /**
   * Wait for this service to start-up.
   */
  public abstract void waitFor()

  /**
   * Rebuild the database for this service. Called after 
   * canned-data has been restored from the original source.
   */
  public abstract void rebuild()

  /**
   * A simple echo to console.
   *
   * @param msg the message to echo
   */
  protected void echo(String msg) {
    String name = getClass().getName()
    name = name.substring(name.lastIndexOf('.') + 1)
    println '[' + name + '] ' + msg
  }

  /**
   * Wait for the service to start up. Tries for over 2 minutes before giving up.
   *
   * @param uri the url of the service to ping
   *
   * @return the response contents from the service ping
   */
  protected String waitFor(String uri) {
    Throwable saved = null;
    for (i in 1..120) {
      try {return uri.toURL().text} catch (Throwable e)  {saved = e}
      echo i + ' Waiting for ' + uri + ' ...'
      sleep 10000
    }
    echo 'Failed to start the service at ' + uri
    throw saved
  }

  /**
   * Gets the system properties map for the launched service.
   */
  public Map getSysProperties() {
    return properties
  }

}
