/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
 *
 * Modified from code part of Fedora. It's license reads:
 * License and Copyright: The contents of this file will be subject to the
 * same open source license as the Fedora Repository System at www.fedora.info
 * It is expected to be released with Fedora version 2.2.
 * Copyright 2006 by The Technical University of Denmark.
 * All rights reserved.
 */
package org.topazproject.fedoragsearch.topazlucene;

import java.util.List;
import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import org.plos.configuration.ConfigurationStore;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexModifier;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 * Package private class to initialize and hold shared configuration data.
 *
 * Both OperationsImpl and Statement needs some of the same shared configuration data. It
 * is read and saved here.
 *
 * @author Eric Brown
 * @version $Id$
 */
class TopazConfig {
  private static final Log   log        = LogFactory.getLog(TopazConfig.class);

  static final Configuration CONF       = ConfigurationStore.getInstance().getConfiguration();
  static final String FEDORAOBJ_PATH    = CONF.getString("ambra.services.search.fedoraObjPath",   null);
  static final String INDEX_PATH;         // see static section below
  static final String INDEX_NAME        = CONF.getString("ambra.services.search.indexName",       "topaz");
  static final long   CACHE_EXPIRATION  = CONF.getLong(  "ambra.services.search.cacheExpiration", 600000L);
  static final String FOXML2LUCENE_XSLT =
    CONF.getString("ambra.services.search.foxmlToLucene", "topazFoxmlToLucene");
  static final List<String> defFieldsList = Arrays.asList(new String[] { "description", "title", "body" });
  static final List<String> DEFAULT_FIELDS =
    CONF.getList(  "ambra.services.search.defaultFields", defFieldsList);

  static final String FEDORA_BASE_URL   = CONF.getString("ambra.topaz.blobStore.fedora.base-url", null);

  static final String GFIND_XSLT =
    CONF.getString("ambra.services.search.xslt.gfind", "gfindObjectsToResultPage");
  static final String BROWSE_XSLT =
    CONF.getString("ambra.services.search.xslt.browse", "browseIndexTResultPage");
  static final String INDEXINFO_XSLT = CONF.getString("ambra.services.search.xslt.indexInfo", "copyXml");
  static final String UPDATE_XSLT    =
    CONF.getString("ambra.services.search.xslt.", "updateIndexToResultPage");

  static final String INDEXINFO_XML =
    CONF.getString("ambra.services.search.xml.indexinfo", "indexInfo.xml");
  static final String ANALYZER_NAME = CONF.getString("ambra.services.search.analyzerName",
      "org.apache.lucene.analysis.standard.StandardAnalyzer");

  private static Analyzer analyzer;

  // Log some errors if necessary
  static {
    if (FEDORAOBJ_PATH == null) // may still work fine as long as don't re-index all of fedora
      log.info("ambra.services.search.fedoraObjPath - location of fedora foxml files not configured");

    INDEX_PATH = getIndexPath(); // Get config if set or create temp location
    initializeIndex(); // If it doesn't exist, create it
    initializeAnalyzer();
  }

  /**
   * Try to configure INDEX_PATH from commons-config.
   * If this doesn't work, setup a temporary directory. Helpful for integration testing.
   *
   * @returns The path to use for the lucene index.
   */
  private static String getIndexPath() {
    String indexPath = CONF.getString("ambra.services.search.indexPath", null);
    if (indexPath == null) {
      log.error("ambra.services.search.indexPath - location of lucene index not configured");
      try {
        // Create a temporary directory to stash DB
        File dir = File.createTempFile("topazlucene", "_db");
        indexPath = dir.toString();
        if (!dir.delete()) // Delete the file as we want a directory
          log.error("Unable to delete temporary file " + dir);
      } catch (IOException ioe) {
        log.error("Unable to create temporary directory", ioe);
      }
    }
    log.info("topaz-lucene db in '" + indexPath + "'");
    return indexPath;
  }

  /**
   * Make sure that lucene index exists (trying to create it if it does not).
   */
  private static void initializeIndex() {
    try {
      // Create the directory if it doesn't exist
      File dir = new File(INDEX_PATH);
      if (!dir.exists()) {
        if (!dir.mkdirs())
          log.error("Unable to create directory for lucene: " + INDEX_PATH);
        else
          log.info("Created directories for lucene db: " + INDEX_PATH);
      } else if (!dir.isDirectory())
        log.error("Lucene directory is not a directory! -- " + INDEX_PATH);
      try {
        // Try to open the DB for reading. If the DB doesn't exist (no segments file), will fail
        TopazIndexSearcher is = new TopazIndexSearcher(INDEX_PATH);
        is.close();
      } catch (FileNotFoundException fnfe) {
        // Create the database
        IndexModifier im = new IndexModifier(INDEX_PATH, new StandardAnalyzer(), true);
        im.close();
        log.warn("Creating Lucene DB: " + INDEX_PATH + " (Error opening DB was " + fnfe + ")");
      }
    } catch (IOException ioe) {
      log.error("Error seeing or creating database", ioe);
    }
  }

  /**
   * Initialize the configured analyzer -- we only need one instance of our analyzer
   * for all operations.
   */
  private static void initializeAnalyzer() {
    try {
      analyzer = (Analyzer) Class.forName(ANALYZER_NAME).newInstance();
      log.debug("Using lucene analyzer: " + ANALYZER_NAME);
    } catch (ClassNotFoundException cnfe) {
      log.error("Unable to find lucene analyzer class: " + ANALYZER_NAME, cnfe);
    } catch (InstantiationException ie) {
      log.error("Unable to instantiate lucene analyzer: " + ANALYZER_NAME, ie);
    } catch (IllegalAccessException iae) {
      log.error("Access violation instantiating lucene analyzer: " + ANALYZER_NAME, iae);
    }
  }

  /**
   * Return the configured analyzer.
   *
   * @returns The configured analyzer.
   */
  public static Analyzer getAnalyzer() { return analyzer; }
}
