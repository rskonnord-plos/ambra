/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.owl;

import java.util.jar.JarFile
import org.apache.commons.lang.text.StrMatcher
import org.apache.commons.lang.text.StrTokenizer

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.SessionFactory
import org.topazproject.otm.ModelConfig
import org.topazproject.otm.stores.ItqlStore
import org.topazproject.otm.owl.OwlHelper
import org.topazproject.otm.OtmException
import org.topazproject.otm.ClassMetadata

/**
 * Utility class that uses otm-s OwlHelper class to generate owl metadata into
 * <local:///topazproject#metadata> of classes found in supplied classpath.
 *
 * @author Eric Brown
 */
class Metadata {
  private static final Log log = LogFactory.getLog(Metadata.class);
  static GroovyClassLoader gcl = new GroovyClassLoader()
  static SessionFactory factory = new SessionFactory()
  static String MODEL_PREFIX = "local:///topazproject#"

  static {
    // Setup otm
    factory.setTripleStore(
      new ItqlStore(URI.create("http://localhost:9091/mulgara-service/services/ItqlBeanService")))
  }

  /**
   * Given a list of directories and/or jar files, generate metadata.
   */
  public static void main(String[] args) {
    addClasses(fixArgs(args))
  }

  /**
   * Extract otm annotated meatadata from classes found in directories and/or jar files.
   *
   * @param classPaths a collection of jar filenames and/or directories
   */
  static void addClasses(classPaths) {
    // Update gcl classpath first
    classPaths.each() { fname ->
      def file = expandFilename(fname)
      if (file.isDirectory() || fname =~ /\.jar$/) {
        gcl.addClasspath(file.getAbsolutePath())
      } else
        println "$fname not a jar file or directory"
    }

    // Process classes
    classPaths.each() { fname ->
      def file = expandFilename(fname)
      if (fname =~ /\.jar$/)
        addJar(file)
      else if (file.isDirectory())
        addDirectory(file)
    }

    // Do the deed
    factory.addModel(new ModelConfig("metadata", URI.create(MODEL_PREFIX + "metadata"), null))
    OwlHelper.addFactory((SessionFactory)factory, factory.getModel("metadata"))
  }

  static void addJar(File file) {
    def jarfile = new JarFile(file)
    jarfile.entries().each() {
      def name = it.toString()
      if (name =~ /\.class$/) {
        def clazz = getClass(name)
        if (clazz)
          processClass(clazz)
      }
    }
  }

  /** Iterate over all the files in a directory and find any otm classes. */
  static void addDirectory(File dir) {
    dir.eachFileRecurse() { fname ->
      if (fname =~ /\.class$/) {
        def name = fname.getAbsolutePath() - dir.getAbsolutePath()
        name = name.replaceFirst(/^\//, "")
        def clazz = getClass(name)
        if (clazz)
          processClass(clazz)
      }
    }
  }

  /** See if a class is otm annotated and add it to our factory. */
  static void processClass(Class clazz) {
    try {
      factory.preload(clazz)
    } catch (OtmException o) {
      log.debug("Unable to load '$clazz'", o)
    }

    try {
      ClassMetadata cm = factory.getClassMetadata(clazz);
      String model = (cm != null) ? cm.getModel() : null
      if (model != null) {
        factory.addModel(new ModelConfig(model, URI.create(MODEL_PREFIX + model), null))
        println "Loaded ${clazz.getName()} into ${MODEL_PREFIX}${model}"
      }
    } catch (Throwable t) {
      println "error processing '${clazz.getName()}' " + t
      t.printStackTrace()
    }
  }

  /** Convert a filename to a classname and load it via our GroovyClassLoader */
  static Class getClass(String name) {
    def cName = name.replaceAll(/\//, ".") - ".class"
    try {
      return gcl.loadClass(cName)
    } catch (NoClassDefFoundError ncdfe) {
      // Probably this is a subclass of a library that is unloaded
      println "$ncdfe (loading $cName)"
      return null
    }
  }

  /** Deal with ~ at the beginning of a file or directory */
  static File expandFilename(String name) {
    return new File(name.replaceFirst(/^~/, System.getProperty("user.home")))
  }

  /** Fix an arg string if called from maven */
  static String[] fixArgs(String[] args) {
    // Fix crap with maven sometimes passing args of [ null ] (an array of one null)
    if (args[0] == null) args = [ ]
    if (args != null && args.length == 1 && args[0] != null)
      args = new StrTokenizer(args[0], StrMatcher.trimMatcher(), StrMatcher.quoteMatcher())
                      .getTokenArray()
    return args
  }
}
