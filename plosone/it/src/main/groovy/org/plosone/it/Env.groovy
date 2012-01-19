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

/**
 * An integration test environment for PlosOne. The environment runs
 * its own copy of mulgara and fedora. The DummySSO Filter is enabled
 * instead of using CAS. The mulgara and fedora stores can be pre-populated
 * with canned data. Same with lucene and ingestion-queue etc. In addition
 * tests can reset the data used to the original state any time by calling 
 * the restore() function.
 * <p>
 * Multiple environments can be installed - however only one environment can 
 * be running at a time. So running tests in parallel is not an option.
 * <p>
 * The commands here launches mvn to run the ant-tasks-plugin for tasks like 
 * fedora-install, mulgara-install etc. Make sure this plugin is installed
 * in the local maven repository and the 'mvn' executable is in your PATH.
 * 
 * @author Pradeep Krishnan
 */
public class Env {
  private final AntBuilder ant = new AntBuilder();
  private final String install;
  private final String data;
  private static Env active = null;
  private final Mulgara mulgara;
  private final Fedora  fedora;
  private final PubApp  pubApp;

  public static boolean stopOnExit = true;
  private static boolean stopOnStart = true;
  private static String ext;

  static {
    ext = (System.properties.'os.name'.toLowerCase().indexOf('windows') > -1) ? '.bat' : '';
    stopOnExit = Env.boolPropValue('stopOnExit', true);
    stopOnStart = Env.boolPropValue('stopOnStart', true);
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

    mulgara = new Mulgara(Env.path(install, '/mulgara-service'))
    mulgara.sysProperties.'log4j.configuration'       = Env.pathUrl(install, '/mulgaraLog4j.xml')
    mulgara.sysProperties.'topaz.mulgara.databaseDir' = Env.path(install, '/data/mulgara')

    fedora = new Fedora(install)

    pubApp = new PubApp(Env.path(install, '/plosone-webapp'))
    pubApp.sysProperties.'log4j.configuration'           = Env.pathUrl(install, '/plosoneLog4j.xml')
    pubApp.sysProperties.'org.plos.configuration.overrides' = 'defaults-dev.xml'
    pubApp.sysProperties.'pub.spring.ingest.source'      = Env.path(install, '/data/ingestion-queue')
    pubApp.sysProperties.'pub.spring.ingest.destination' = Env.path(install, '/data/ingested')
    pubApp.sysProperties.'topaz.search.indexpath'        = Env.path(install, '/data/lucene')
    pubApp.sysProperties.'topaz.search.defaultfields'    = 'description,title,body,creator'
    pubApp.sysProperties.'org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH' = 'true'
  }

  /**
   * Stop all services and clean the install directory.
   */
  public void clean() {
    ant.echo 'Cleaning ...'
    stop();
    ant.delete(dir:install)
    ant.echo 'Finished cleaning'
  }

  /**
   * Stop all services.
   */
  public void stop() {
    if ((active == null) && (stopOnStart == false)) {
      ant.echo 'skipping stop() of services.'
      return
    }
    Env env = (active == null) ? this : active

    ant.echo 'Stopping all services ...'

    try { pubApp.stop() } catch (Throwable t) { }
    try { fedora.stop() } catch (Throwable t) { }
    try { mulgara.stop() } catch (Throwable t) { }

    active = null
    stopOnStart = false
    ant.echo 'Stopped all services'
  }

  /**
   * Install all services. A marker file is placed after the install
   * so that subsequent calls to install can bypass the actual install.
   */
  public void install() {
    File f = new File(Env.path(install, '/installed'));
    if (f.exists())
      return;

    ant.echo 'Installing ...'

    clean()  // clean any previous partial installs

    fedora.install()
    mulgara.install()
    pubApp.install()

    load()  // load data

    // install the log4j files
    resource('/plosoneLog4j.xml')
    resource('/mulgaraLog4j.xml')

    // finally create the marker file
    ant.touch(file: Env.path(install, '/installed'))
    ant.echo 'Finished installation'
  }

  /**
   * Restore the data to the same state as a fresh install.
   */
  public void restore() {
    ant.echo 'Restoring data ...'
    stop()
    ant.delete(dir: Env.path(install, "/data"))
    load()
    ant.echo 'Finished restoring data'
  }

 /**
  * Start all services. If another environment is currently running, that is stopped first.
  */
  public void start() {
    ant.echo 'Starting services ...'
    if (active != null) {
      if (active.install.equals(install))
         return
      active.stop()
    }
    active = this
    mulgara.start()
    fedora.start()
    mulgara.waitFor()
    ant.echo 'Mulgara started'
    pubApp.start()
    fedora.waitFor()
    ant.echo 'Fedora started'
    pubApp.waitFor()
    ant.echo 'Plosone started'
    ant.echo 'All services are up and running'
  }

  /**
   * Locate a pom.xml file to pass to mvn.  Only for the tasks that require a pom.
   * Looks up the pom in the current working directory or its parents. If a
   * pom is not found there, then the install directory and its parents are looked up.
   *
   * Note that the expectation here is to find a pom that is the head/pom.xml or
   * a child of the head/pom.xml where head represents the head of the plosone 
   * project source tree.
   */
  private String pom () {
    String p = Env.pom(new File(System.properties.'user.dir'))
    if (p == null)
      p = Env.pom(new File(install))
    if (p == null)
      throw new FileNotFoundException('a pom.xml was not found')
    return p
  }

  /**
   * Look up a pom in this directory or recursively its parents.
   * 
   * @param dir the directory to look for pom.xml
   *
   * @return the full path of the pom.xml file found or null
   */
  private static String pom(File dir) {
    if (dir == null)
      return null;
    File f = new File(dir, 'pom.xml');
    if (f.exists())
      return f.absoluteFile.canonicalPath;
    return Env.pom(dir.parentFile)
  }

  /**
   * Load canned data into the environment. The canned data is expected to be
   * a mvn artifact and is expected to be a tar.gz of the expected data directory
   * lay out. The directory layout is as follows:
   * <code>
   *       data
   *       `-- README
   *       `-- lucene
   *       `-- ingestion-queue
   *       `-- ingested
   *       `-- fedora
   *       |   |-- objects
   *       |   |-- datastreams
   *       `-- mulgara (mulgara data dir)
   * </code>
   */
  private void load() {
    ant.echo 'Loading/Creating data for ...'

    if (data != null) {
      ant.exec(executable: 'mvn' + ext, failonerror:true) {
        arg(line: '-f ' + pom() + ' ant-tasks:untar -Dlocation=' + install 
          + ' -Dtype=tbz -Dcompression=bzip2 -Ddependencies=' + data)
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

    fedora.rebuild()

    ant.echo 'Finished loading data'
  }

  /**
   * Copy a resource from class-path and return the file-name.
   *
   * @param name the resource to find in class-path
   * 
   * @return the filename where the resource was copied into.
   */
  public String resource(String name) {
    return Env.resource(install, name)
  }

  /**
   * Copy a resource from class-path and return the file-name.
   *
   * @param tmpDir the directory to copy the resource to
   * @param name the resource to find in class-path
   * 
   * @return the filename where the resource was copied into.
   */
  public static String resource(String tmpDir, String name) {
     String input = Env.path(tmpDir, name)
     def out = new BufferedOutputStream(new FileOutputStream(input))
     def is = Env.class.getResourceAsStream(name)
     assert is != null, "resource '" + name + "' not found in classpath"
     out << is
     out.close()
     return input
  }

  /**
   * Execute a groovy shell script with arguments.
   *
   * @param args command line where first argument is the script-file to execute
   */
  public void script(String[] args) {
    String res = args[0]
    String[] sargs = new String[args.length - 1]

    for (x in 1 .. args.length - 1)
        sargs[x-1] = args[x]

    ant.echo 'Executing script ' + res + ' with args ' + sargs
    GroovyShell shell = new GroovyShell();
    shell.run(new File(res), sargs)
  }

  /**
   * Convert to a platform specific file path.
   *
   * @param dir a directory or root path
   * @param file a filename or a relative path
   *
   * @return platform specific file path
   */
  public static String path(String dir, String file) {
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

  /**
   * Convert a path to a file: URL.
   *
   * @param dir a directory or root path
   * @param file a filename or a relative path
   *
   * @return the file: URL for the path
   */
  private static String pathUrl(String dir, String file) {
    File f = new File(Env.path(dir, file))
    return f.toURL().toString()
  }

  /**
   * Gets the version of the pom in the current working directory (or its parent(s))
   *
   * @return the pom versio
   */
  public static String pomVersion() {
    String p = Env.pom(new File(System.properties.'user.dir'))
    if (p == null)
       throw new FileNotFoundException('No pom.xml file found in ' + System.properties.'user.dir')

    def doc = new XmlSlurper().parse(new File(p))
    String version = doc.version?.text()
    if ((version == null) || "".equals(version))
      version = doc.parent?.version?.text()
    if ((version == null) || "".equals(version))
       throw new Exception("Couldn't find a pom version in " + p)

    return version
  }

  /**
   * Gets the path to the dependency in the local maven repository.
   */
  public static String dependencyPath(def groupId, def artifactId, def version, def type) {
    def p = groupId.replaceAll('\\.', '/') + '/' +
                  artifactId + '/' +
                  version + '/' +
                  artifactId + '-' + version + '.' + type

    return Env.path(Env.mavenLocalRepository(), p)
  }

  /**
   * Gets the path to the dependency in the local maven repository as a URL.
   */
  public static String dependencyURL(def groupId, def artifactId, def version, def type) {
    def p = groupId.replaceAll('\\.', '/') + '/' +
                  artifactId + '/' +
                  version + '/' +
                  artifactId + '-' + version + '.' + type
    return Env.pathUrl(Env.mavenLocalRepository(), p)
  }

  /**
   * Gets the maven local repository.
   */
  public static String mavenLocalRepository() {
    File settingsFile = new File(System.properties.'user.home', ".m2/settings.xml");
    String localRepository = null;
    if (settingsFile.exists()) {
       def doc = new XmlSlurper().parse(settingsFile);
       localRepository = doc.localRepository?.text()
    }
    if ((localRepository == null) || "".equals(localRepository))
       localRepository = Env.path(System.properties.'user.home', '/.m2/repository')

    return localRepository
  }

  private static boolean boolPropValue(String name, boolean defaultVal) {
    String val = System.properties[name]
    if ((val == null) || "".equals(val))
      return defaultVal
    val = val.toLowerCase()

    if (val.equals("true") || val.equals("yes"))
      return true

    if (val.equals("false") || val.equals("no"))
      return false

    return defaultVal
  }

}
