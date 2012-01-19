/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2009 by Topaz, Inc.
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
package org.topazproject.ambra.configuration;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.BlobStore;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.util.TransactionHelper;

import org.topazproject.fedora.otm.FedoraBlobStore;
import org.topazproject.fedora.otm.FedoraBlobFactory;

import org.topazproject.mulgara.itql.EmbeddedClient;

/**
 * Convenience class to manage configuration of OTM Session Factory
 *
 * @author Stephen Cheng
 */
public class OtmConfiguration {
  private static final Log log = LogFactory.getLog(OtmConfiguration.class);

  private final String        tripleStoreUrl;
  private final BlobStore     blobStore;
  private SessionFactory      factory = null;
  private String[]            preloadClasses = new String[0];
  private GraphConfig[]       graphs = new GraphConfig[0];
  private Map<String, String> aliases = new HashMap<String, String>();

  /**
   * Creates a new OtmConfiguration object.
   *
   * @param tripleStoreUrl the URL for the store
   * @param blobStore the blob-store to use
   */
  public OtmConfiguration(String tripleStoreUrl, BlobStore blobStore) {
    this.tripleStoreUrl = tripleStoreUrl;
    this.blobStore = blobStore;
  }

  /**
   * Gets the set of classes that we configured the factory with.
   *
   * @return Returns the preloadClasses.
   */
  public String[] getPreloadClasses() {
    return preloadClasses;
  }

  /**
   * Preloads the session factory with the classes that we use.
   *
   * @param preloadClasses The preloadClasses to set.
   */
  public void setPreloadClasses(String[] preloadClasses) {
    if ((factory != null) && !Arrays.equals(preloadClasses, this.preloadClasses)) {
      factory = null;
      log.warn("Removed old factory because new set of preloadClasses specified");
    }
    this.preloadClasses = preloadClasses;
  }

  /**
   * Gets the session factory.
   *
   * @return Returns the factory.
   */
  public SessionFactory getFactory() {
    if (factory == null)
      factory = createFactory();
    return factory;
  }

  /**
   * Get the list of configured graphs.
   *
   * @return Returns the graphs.
   */
  public GraphConfig[] getGraphs() {
    return graphs;
  }

  /**
   * Configures the factory with graphs that we use and makes sure they exist.
   *
   * @param graphs The graphs to set.
   */
  public void setGraphs(final GraphConfig[] graphs) {
    if ((factory != null) && !Arrays.equals(graphs, this.graphs)) {
      factory = null;
      log.warn("Removed old factory because new set of graph configs specified");
    }
    this.graphs = graphs;
  }

  /**
   * Get the list of configured aliases.
   *
   * @return the aliases
   */
  public Map<String, String> getAliases() {
    return aliases;
  }

  /**
   * Configures the factory with aliases that we use.
   *
   * @param aliases the aliases to set.
   */
  public void setAliases(Map<String, String> aliases) {
    if ((factory != null) && !aliases.equals(this.aliases)) {
      factory = null;
      log.warn("Removed old factory because new set of aliases specified");
    }
    this.aliases = aliases;
  }

  /**
   * Configures the blob factoris used by the fedora blob-store.
   *
   * @param fbfs array of fedora blob factories to add
   */
  public void setFedoraBlobFactories(FedoraBlobFactory[] fbfs) {
    if (blobStore instanceof FedoraBlobStore) {
      FedoraBlobStore fbs = (FedoraBlobStore)blobStore;
      for (FedoraBlobFactory fbf : fbfs) {
        fbs.addBlobFactory(fbf);
        if (log.isDebugEnabled())
          log.debug("Added BlobFactory for " + Arrays.asList(fbf.getSupportedUriPrefixes()));
      }
    }
  }

  private SessionFactory createFactory() {
    if (log.isDebugEnabled())
      log.debug("Creating new SessionFactory instance ...");

    SessionFactory factory = new SessionFactoryImpl();

    if (log.isDebugEnabled())
      log.debug("Adding aliases: " + aliases);

    for (Map.Entry<String, String> alias : aliases.entrySet())
      factory.addAlias(alias.getKey(), alias.getValue());

    if (log.isDebugEnabled())
      log.debug("Creating new triplestore: " + tripleStoreUrl);

    factory.setTripleStore(new ItqlStore(URI.create(tripleStoreUrl),
                           WebappItqlClientFactory.getInstance()));

    if (log.isDebugEnabled())
      log.debug("Setting blobstore : " + blobStore.getClass());

    factory.setBlobStore(blobStore);

    if (log.isDebugEnabled())
      log.debug("Pre-loading classes from class-path ...");

    factory.preloadFromClasspath();

    if (log.isDebugEnabled())
      log.debug("Pre-loading classes : " + Arrays.asList(preloadClasses));

    for (String className : preloadClasses) {
      try {
        factory.preload(Class.forName(className));
      } catch (ClassNotFoundException ce) {
        log.warn("Could not preload class: " + className, ce);
      }
    }

    if (log.isDebugEnabled())
      log.debug("Adding graph configs : " + Arrays.asList(graphs));

    for (GraphConfig graph : graphs)
      factory.addGraph(graph);

    if (log.isDebugEnabled())
      log.debug("Validating factory config...");

    factory.validate();

    if (log.isDebugEnabled())
      log.debug("Creating graphs : " + Arrays.asList(factory.listGraphs()));

    TransactionHelper.doInTx(factory, new TransactionHelper.Action<Void>() {
      public Void run(Transaction tx) {
        for (GraphConfig graph : tx.getSession().getSessionFactory().listGraphs())
          tx.getSession().createGraph(graph.getId());
        return null;
      }
    });

    log.info("Initialized OTM SessionFactory instance.");

    return factory;
  }

  /**
   * Close down everything.
   */
  public void close() {
    EmbeddedClient.releaseResources();
  }
}
