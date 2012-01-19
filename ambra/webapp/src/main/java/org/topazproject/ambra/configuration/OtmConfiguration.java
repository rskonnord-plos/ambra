/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.BlobStore;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.fedora.otm.FedoraBlobStore;
import org.topazproject.fedora.otm.FedoraBlobFactory;

/**
 * Convenience class to manage configuration of OTM Session Factory
 *
 * @author Stephen Cheng
 */
public class OtmConfiguration {
  private SessionFactory   factory;
  private String[]         preloadClasses;
  private ModelConfig[]    models;
  private static final Log log = LogFactory.getLog(OtmConfiguration.class);

  /**
   * Creates a new OtmConfiguration object.
   *
   * @param tripleStoreUrl the URL for the store
   * @param blobStore the blob-store to use
   */
  public OtmConfiguration(String tripleStoreUrl, BlobStore blobStore) {
    factory = new SessionFactoryImpl();

    if (log.isDebugEnabled()) {
      log.debug("Creating new triplestore: " + tripleStoreUrl);
    }

    factory.setTripleStore(new ItqlStore(URI.create(tripleStoreUrl),
                           WebappItqlClientFactory.getInstance()));

    if (log.isDebugEnabled())
      log.debug("Added blobstore : " + blobStore.getClass());

    factory.setBlobStore(blobStore);
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
    this.preloadClasses = preloadClasses;

    for (String className : preloadClasses) {
      try {
        factory.preload(Class.forName(className));
      } catch (ClassNotFoundException ce) {
        log.info("Could not preload class: " + className, ce);
      }
    }
  }

  /**
   * Gets the session factory.
   *
   * @return Returns the factory.
   */
  public SessionFactory getFactory() {
    return factory;
  }

  /**
   * Get the list of configured models.
   *
   * @return Returns the models.
   */
  public ModelConfig[] getModels() {
    return models;
  }

  /**
   * Configures the factory with models that we use and makes sure they exist.
   *
   * @param models The models to set.
   */
  public void setModels(ModelConfig[] models) {
    this.models = models;

    for (ModelConfig model : models) {
      factory.addModel(model);
      factory.getTripleStore().createModel(model);
    }
  }

  /**
   * Get the list of configured aliases.
   *
   * @return the aliases
   */
  public Map<String, String> getAliases() {
    return factory.listAliases();
  }

  /**
   * Configures the factory with aliases that we use.
   *
   * @param aliases the aliases to set.
   */
  public void setAliases(Map<String, String> aliases) {
    for (Map.Entry<String, String> alias : aliases.entrySet())
      factory.addAlias(alias.getKey(), alias.getValue());
  }

  /**
   * Configures the blob factoris used by the fedora blob-store.
   *
   * @param fbfs array of fedora blob factories to add
   */
  public void setFedoraBlobFactories(FedoraBlobFactory[] fbfs) {
    if (factory.getBlobStore() instanceof FedoraBlobStore) {
      FedoraBlobStore fbs = (FedoraBlobStore)factory.getBlobStore();
      for (FedoraBlobFactory fbf : fbfs) {
        fbs.addBlobFactory(fbf);
        if (log.isDebugEnabled())
          log.debug("Added BlobFactory for " + fbf.getSupportedUriPrefixes());
      }
    }
  }
}
