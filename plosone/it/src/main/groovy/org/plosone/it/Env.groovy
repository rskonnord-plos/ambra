/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/libs/article-util/src/main/groovy/#$
 * $Id: Delete.groovy 2686 2007-05-15 08:22:38Z ebrown $
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plosone.it;
import org.apache.tools.ant.taskdefs.Antlib;

public class Env {
  private AntBuilder ant = new AntBuilder();
  private String install;
  private String data;
  private String opts;
  private static Env active = null;
  private String mvnExec = null;

  public static boolean stopOnExit = true;

  static {
    def shutdownHook = new Thread( {
        if ((active != null) && stopOnExit)
          active.stop();
    }, "Shutdown services")
    Runtime.runtime.addShutdownHook(shutdownHook)
  }


 /**
  *  Construct a test environment.
  *
  *  @param install the installation directory
  *  @param data the mvn dependency spec for data (eg. 'org.plosone:plosone-it-data:0.7')
  */
  public Env(String install, String data) {
    File f = new File(install);
    this.install = install = f.absoluteFile.canonicalPath;
    this.data = data;
    opts  = ' -DECQS_INSTALL_DIR=' + install + 
            ' -DFEDORA_INSTALL_DIR=' + install + 
            ' -DDEST_DIR=' + install

    String url = '/net/sf/antcontrib/antlib.xml'
    url = this.getClass().getResource(url)
    Antlib.createAntlib(ant.antProject, url.toURL(), null).execute()
  }

  /**
   * Stop all services and clean the install directory
   */
  public void clean() {
    stop();
    ant.delete(dir:install)
  }

  /**
   * Stop all services
   */ 
  public void stop() {
    for (task in ['plosone-stop', 'mulgara-stop', 'fedora-stop'])
      try { antTask(task) } catch (Throwable t) { } 
    active = null
  }

  /**
   * Install all services. A marker file is placed after the install
   * so that subsequent calls to install can bypass the actual install.
   * 
   */
  public void install() {
    File f = new File(path(install, '/installed'));
    if (f.exists())
      return;

    clean()

    for (task in ['ecqs-install', 'fedora-install', 'mulgara-install', 'plosone-install'])
      antTask(task)
   
    load()

    ant.touch(file: path(install, '/installed'))
  }

  /**
   * Restore the data to the same state as a fresh install.
   *
   */
  public void restore() {
    if (active != null) 
      active.stop()
    else
       stop()
    ant.delete(dir: path(install, "/data"))
    load()
  }

 /**
  * Start all services. If another environment is currently running, that is stopped first.
  */
  public void start() {
    if (active != null) {
      if (active.install.equals(install))
         return
      active.stop()
    }  
    active = this
    mulgara()
    fedora()
    publishingApp()
    waitFor('http://localhost:8080/plosone-webapp/')
    ant.echo 'Publishing app started'
  }
  
  private void antTask(task) {
    ant.exec(executable: mavenExecutable) {
      arg(line: 'ant-tasks:' + task + opts)
    }
  }

  private void mulgara() {
    ant.echo 'Starting mulgara'
    ant.forget {
      ant.exec(executable: mavenExecutable) {
        arg(line: '-f ' + pom() + ' ant-tasks:mulgara-start -DDEST_DIR=' + install
         + ' -Dtopaz.mulgara.databaseDir=' + path(install, '/data/mulgara') 
         + ' -Dlog4j.configuration='+ mulgaraLog4j())
      }
      ant.echo 'Mulgara stopped'
    }
  }

  private void fedora() {
    ant.delete(file: path(install, '/fedora-2.1.1/server/status'))
    ant.exec(executable: mavenExecutable) {
      arg(line: 'ant-tasks:fedora-start -DSPAWN=true' + opts)
    }
  }

  private void publishingApp() {
    ant.echo 'Starting publishing app'
    ant.forget {
      ant.exec(executable: mavenExecutable) {
        arg(line: '-f ' + pom() + ' ant-tasks:plosone-start -DDEST_DIR=' + install
         + ' -Dorg.plos.configuration.overrides=defaults-dev.xml -Dlog4j.configuration=' 
         + publishingAppLog4j() 
         + ' -Dpub.spring.ingest.source=' + path(install, '/data/ingestion-queue')
         + ' -Dpub.spring.ingest.destination=' + path(install, '/data/ingested')
         + ' -Dtopaz.search.indexpath=' + path(install, '/data/lucene')
         + ' -Dtopaz.search.defaultfields=description,title,body,creator'      
       )
      }
      ant.echo 'Publishing app Stopped'
    }
  }

  private String waitFor(String uri) {
    Throwable saved = null;
    for (i in 1..120) {
      try {return uri.toURL().getText()} catch (Throwable e)  {saved = e}
      ant.echo i + ' Waiting for ' + uri + ' ...'
      sleep 1000
    }
    throw saved
  }
  
  private String pom () {
    String p = pom(new File(System.properties.'user.dir'))
    if (p == null)
      p = pom(new File(install))
    if (p == null)
      throw new FileNotFoundException('a pom.xml was not found')
    return p
  }

  private String pom(File dir) {
    if (dir == null)
      return null;
    File f = new File(dir, 'pom.xml');
    if (f.exists())
      return f.absoluteFile.canonicalPath;
    return pom(dir.parentFile)
  }


  private void load() {
    if (data != null) {
      ant.exec(executable: mavenExecutable, failonerror:true) {
        arg(line: '-f ' + pom() + ' ant-tasks:tgz-explode -Dlocation=' + install 
          + ' -Dtype=tgz -Ddependencies=' + data)
      }
    }

    File d = new File(install);
    d = new File(install, 'data')
    if (!d.exists())
       d.mkdir()
    File m = new File(d, 'mulgara')
    if (!m.exists())
      m.mkdir()

    File f = new File(d, 'fedora')
    if (!f.exists())
      f.mkdir()
    
    File iq = new File(d, 'ingestion-queue')
    if (!iq.exists())
      iq.mkdir()

    File cq = new File(d, 'ingested')
    if (!cq.exists())
      cq.mkdir()

    File l = new File(d, 'lucene')
    if (!l.exists())
      l.mkdir()

    ant.exec(executable: mavenExecutable) {
      arg(line: 'ant-tasks:fedora-rebuild ' + opts
       + ' -DFEDORA_REBUILD_STDIN=' + rebuildInput()
       + ' -DFEDORA_REBUILD_FROM=' + path(install, '/data/fedora'))
    }
  }

  private String rebuildInput() {
     return resource('/fedora-rebuild-input')
  }
  
  private String publishingAppLog4j() {
    File f = new File(resource('/plosoneLog4j.xml'))
    return f.toURL().toString()
  }
  
  private String mulgaraLog4j() {
     File f = new File(resource('/mulgaraLog4j.xml'))
     return f.toURL().toString()
  }

  public String resource(String name) {
     String input = path(install, name)
     def out = new BufferedOutputStream(new FileOutputStream(input))
     out << getClass().getResourceAsStream(name)
     out.close()
     return input
  }

  public void script(String[] args) {
    String res = args[0]
    String[] sargs = new String[args.length - 1]

    for (x in 1 .. args.length - 1)
        sargs[x-1] = args[x]

    println 'executing script ' + res + ' with args ' + sargs
    GroovyShell shell = new GroovyShell();
    shell.run(new File(res), sargs)
  }

  private String getMavenExecutable() {
    if (mvnExec == null) {
      if (System.properties.'os.name'.toLowerCase().indexOf('windows') > -1) {
        mvnExec = 'mvn.bat'
      } else {
        mvnExec = 'mvn'
      }
    }
    return mvnExec
  }

  private String path(String dir, String file) {
    String sep = System.properties.'file.separator'
        
    if (!file.startsWith('/'))
      file = '/' + file

    file =  dir + file
    if (!sep.equals('/')) {
      int pos;
      while ((pos = file.indexOf('/')) > -1)
         file = file.substring(0, pos) + sep + file.substring(pos+1)     
    }
    return file
  }

}
