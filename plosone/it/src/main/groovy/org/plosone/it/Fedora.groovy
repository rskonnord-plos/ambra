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

import org.apache.tools.ant.taskdefs.Antlib;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.APIMStubFactory;

/**
 * Fedora service management.
 *
 * @author Pradeep Krishnan
 */
public class Fedora extends Service {
  private final AntBuilder ant = new AntBuilder()
  private final String ext = (System.properties.'os.name'.toLowerCase().indexOf('windows') > -1) ? '.bat' : ''
  private final String fedoraHome
  private final String opts

  /**
   * Create a Fedora service instance.
   *
   * @param installDir root directory for this service
   */
  public Fedora(String installDir) {
    super(installDir)
    fedoraHome = Env.path(installDir, "fedora-2.1.1")
    opts  = ' -DECQS_INSTALL_DIR=' + installDir +
            ' -DFEDORA_INSTALL_DIR=' + installDir
    String url = '/net/sf/antcontrib/antlib.xml'
    url = this.getClass().getResource(url)
    Antlib.createAntlib(ant.antProject, url.toURL(), null).execute()
  }

  /*
   * inherited javadoc
   */
  public void install() {
    for (task in ['ecqs-install', 'fedora-install'])
      antTask(task)
  }

  /*
   * inherited javadoc
   */
  public void start() {
    echo "Starting mckoi ..."
    ant.forget {
       exec(dir: fedoraHome, 
            executable:Env.path(fedoraHome, "/server/bin/mckoi-start") + ext,
            failonerror:true) {
         env(key:"FEDORA_HOME", file:fedoraHome)
       }
    }
    ant.sleep(seconds:"10")
    echo "Starting fedora ..."
    ant.delete(file: Env.path(fedoraHome, '/server/status'))
    ant.forget {
       exec(dir: fedoraHome,
            executable:Env.path(fedoraHome, "/server/bin/fedora-start") + ext,
            failonerror:true) {
         arg(line: "mckoi")
         env(key:"FEDORA_HOME", file:fedoraHome)
       }
    }
  }

  /*
   * inherited javadoc
   */
  public void stop() {
    echo "Stopping fedora ..."
    antTask 'fedora-stop'
  }

  /*
   * inherited javadoc
   */
  public void waitFor() {
    waitForFedora()
  }

  private String waitForFedora() {
    String uri    = "http://localhost:9090/fedora/services/management"
    String uname  = "fedoraAdmin"
    String passwd = "fedoraAdmin"
    FedoraAPIM apim = APIMStubFactory.create(uri, uname, passwd)
    Throwable saved = null;
    for (i in 1..120) {
      try {
        return apim.getNextPID(new org.apache.axis.types.NonNegativeInteger("1"), "test")[0]
      } catch (Throwable e)  {
        saved = e
      }
      echo i + ' Waiting for ' + uri + ' ...'
      sleep 1000
    }
    echo 'Failed to start the service at ' + uri
    throw saved
  }

  /*
   * inherited javadoc
   */
  public void rebuild() {
    echo "Rebuilding fedora data ..."
    echo "Starting mckoi ..."
    ant.forget {
       exec(dir: fedoraHome, 
            executable:Env.path(fedoraHome, "/server/bin/mckoi-start") + ext,
            failonerror:true) {
         env(key:"FEDORA_HOME", file:fedoraHome)
       }
    }
    ant.sleep(seconds:"10")
    echo "Replacing fedora objects and datastreams ..."
    ant.delete(dir: Env.path(fedoraHome, "/data/datastreams"))
    ant.delete(dir: Env.path(fedoraHome, "/data/objects"))
    ant.copy(todir:Env.path(fedoraHome, "/data")) {
      fileset(dir:Env.path(installDir, "/data/fedora"))
    }
    echo "Invoking fedora-rebuild ..."
    String cmd = Env.path(fedoraHome, "/server/bin/fedora-rebuild") + ext
    echo "Command: " + cmd
    String input = Env.resource(installDir, '/fedora-rebuild-input')
    echo "Input: " + input
    ant.exec(dir: fedoraHome, executable:cmd, failonerror:true,input:input) {
      arg(line:"mckoi")
      env(key:"FEDORA_HOME", file:fedoraHome)
    }
    echo "Stopping mckoi ..."
    ant.exec(dir: fedoraHome,
            executable:Env.path(fedoraHome, "/server/bin/mckoi-stop") + ext,
            failonerror:true) {
      arg(line:"fedoraAdmin fedoraAdmin")
      env(key:"FEDORA_HOME", file:fedoraHome)
    }
    ant.sleep(seconds:"5")
    echo "Fedora rebuild completed."
  }

  /**
   * Execute an ant-tasks plugin task. 
   */
  private void antTask(task) {
    echo 'Executing ant-tasks:' + task + ' ...'
    ant.exec(executable: 'mvn' + ext, failonerror:true) {
      arg(line: 'ant-tasks:' + task + opts)
    }
    echo 'Finished execution of ant-tasks:' + task
  }
}
