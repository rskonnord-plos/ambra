/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.ambraproject.testutils;

import org.ambraproject.service.search.SolrServerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Class added for unit tests to use an in-memory embedded solr server.
 * <p/>
 * Extends the Ambra SolrServerFactory so it can be passed to existing beans. Documents are added to the server
 * by passing a map to the addDocument method.
 *
 * @author Alex Kudlick Date: 5/13/11
 *         <p/>
 *         org.ambraproject.testutils
 */
public class EmbeddedSolrServerFactory extends SolrServerFactory {

  private static Logger log = LoggerFactory.getLogger(EmbeddedSolrServerFactory.class);

  private SolrServer server;

  private String solrHome;

  private String destSolrXmlFile;

  /**
   * Constructor.
   *
   * TODO: this constructor does a lot of IO, and can throw IOException, not ideal
   * in a constructor.  Refactor to eliminate this.
   */
  public EmbeddedSolrServerFactory() throws IOException {

    // Build a directory structure for the embedded solr server's solr home,
    // and copy configuration files there from the classpath.
    solrHome = System.getProperty("java.io.tmpdir") + File.separator + "EmbeddedSolrTest_"
        + System.currentTimeMillis();
    File conf = new File(solrHome + File.separator + "collection1" + File.separator + "conf");
    if (!conf.mkdirs()) {
      throw new RuntimeException("Could not create dir " + conf.getCanonicalPath());
    }
    destSolrXmlFile = solrHome + File.separator + "solr.xml";
    copyResource("solr/collection1/conf/test-solr.xml", destSolrXmlFile);
    copyResource("solr/collection1/conf/test-solr-config.xml",
        conf.getCanonicalPath() + File.separator + "solrconfig.xml");
    copyResource("solr/collection1/conf/test-solr-schema.xml",
        conf.getCanonicalPath() + File.separator + "schema.xml");
    copyResource("solr/collection1/conf/stopwords.txt",
        conf.getCanonicalPath() + File.separator + "stopwords.txt");
    copyResource("solr/collection1/conf/author_stopwords.txt",
        conf.getCanonicalPath() + File.separator + "author_stopwords.txt");

    System.setProperty("solr.solr.home", solrHome);
    CoreContainer coreContainer = new CoreContainer(solrHome, new File(destSolrXmlFile));
    server = new EmbeddedSolrServer(coreContainer, "collection1");
    log.info("EmbeddedSolrServer started with solr home " + solrHome);
  }

  /**
   * Copies a file from the classpath to the given destination.
   *
   * @param resource resource location relative to the classpath
   * @param destination destination file path
   * @throws IOException
   */
  private void copyResource(String resource, String destination) throws IOException {
    IOUtils.copy(getClass().getClassLoader().getResourceAsStream(resource),
        new FileOutputStream(new File(destination)));
  }

  public void tearDown() throws Exception {
    server = null;
    FileUtils.deleteQuietly(new File(solrHome));
  }

  /**
   * Add a document to the server stored here. Note that "id" is a required field
   *
   * @param document - a map from solr field names to values.  The value array should have more than one entry only for
   *                 fields that are multivalued (see the test schema.xml)
   * @throws Exception - from the server.add() method
   */
  public void addDocument(Map<String, String[]> document) throws Exception {
    SolrInputDocument inputDocument = new SolrInputDocument();
    for (String fieldName : document.keySet()) {
      for (String value : document.get(fieldName)) {
        inputDocument.addField(fieldName, value);
      }
    }
    server.add(inputDocument);
    server.commit();
  }

  /**
   * Add a document to the server stored here.  Note that "id" is a required field
   *
   * @param document each row should be a field, with the first entry the field name, and the rest of the entries the values for the field.
   *                 Only multi-valued fields should have more than one value
   * @throws Exception from there server.add() method
   */
  public void addDocument(String[][] document) throws Exception {
    SolrInputDocument inputDocument = new SolrInputDocument();
    for (String[] row : document) {
      String fieldName = row[0];
      for (String value : Arrays.copyOfRange(row, 1, row.length)) {
        inputDocument.addField(fieldName, value);
      }
    }
    server.add(inputDocument);
    server.commit();
  }

  /**
   * Delete all documents in solr
   */
  public void deleteAllDocuments() {
    try {
      UpdateResponse updateResponse = server.deleteByQuery("*:*");
    } catch (Exception e) {
      log.error("Failed to delete all documents in solr.", e);
    }
  }

  /**
   * Get Embedded Solr Server instance
   *
   * @return Solr server
   */
  @Override
  public SolrServer getServer() {
    return server;
  }
}
