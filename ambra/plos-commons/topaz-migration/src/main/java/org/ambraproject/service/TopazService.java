/*
 * $HeadURL:
 * $Id:
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.service;

import org.topazproject.ambra.models.support.fedora.AnnotationFedoraBlobFactory;
import org.topazproject.ambra.models.support.fedora.RepresentationFedoraBlobFactory;
import org.topazproject.fedora.otm.FedoraBlobStore;
import org.topazproject.mulgara.itql.DefaultItqlClientFactory;
import org.topazproject.mulgara.itql.EmbeddedClient;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.util.TransactionHelper;

import java.net.URI;
import java.net.URL;
import java.util.Properties;

/**
 * @author Joe Osowski
 */
public class TopazService {
  private static String PROP_FILE = "topaz.properties";

  private String fedoraHost = null;
  private String fedoraUsername = null;
  private String fedoraPassword = null;
  private String mulgaraHost = null;
  private int limit = -1;
  private boolean readOnly = false;
  private int timeout = -1;

  private SessionFactory sessionFactory;

  public TopazService() throws Exception
  {
    System.getProperties().setProperty("mulgara.rmi.pagetimeout","1200000");

    sessionFactory = new SessionFactoryImpl();

    loadConfig();

    sessionFactory.setTripleStore(new ItqlStore(URI.create(mulgaraHost),
                           new DefaultItqlClientFactory()));

    FedoraBlobStore blobStore = new FedoraBlobStore(
        fedoraHost,
        fedoraUsername,
        fedoraPassword);

    blobStore.addBlobFactory(new AnnotationFedoraBlobFactory("Ambra","info:fedora/"));
    blobStore.addBlobFactory(new RepresentationFedoraBlobFactory("doi","info:doi"));

    sessionFactory.setBlobStore(blobStore);
    sessionFactory.preloadFromClasspath();
    sessionFactory.validate();

    TransactionHelper.doInTx(sessionFactory, new TransactionHelper.Action<Void>() {
      public Void run(Transaction tx) {
        for (GraphConfig graph : tx.getSession().getSessionFactory().listGraphs())
          tx.getSession().createGraph(graph.getId());
        return null;
      }
    });

    sessionFactory.addAlias("id","info:doi/10.1371/");
    sessionFactory.addAlias("annoteaBodyId","info:fedora/");

    //session = sessionFactory.openSession();
  }

  private void loadConfig() throws Exception
  {
    URL url = getClass().getClassLoader().getResource(PROP_FILE);
    Properties properties = new Properties();

    properties.load(url.openStream());

    fedoraHost = properties.getProperty("fedora.host");
    fedoraUsername = properties.getProperty("fedora.username");
    fedoraPassword = properties.getProperty("fedora.password");
    mulgaraHost = properties.getProperty("mulgara.host");
    limit = Integer.valueOf(properties.getProperty("recordlimit","-1"));
    timeout = Integer.valueOf(properties.getProperty("timemout","-1"));
    readOnly = Boolean.valueOf(properties.getProperty("readonly","false"));
  }

  public Session openSession()
  {
    return sessionFactory.openSession();
  }

  public Transaction beginTransaction(Session session) {
    return session.beginTransaction(readOnly, timeout);
  }

  public void close() {
    EmbeddedClient.releaseResources();
  }

  /**
   * Get the configured value to force a limit on the number of records to migrate
   * @return
   */
  public int getRecordLimit()
  {
    return limit;
  }
}