/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm;

import org.topazproject.mulgara.itql.DefaultItqlClientFactory;
import org.topazproject.otm.metadata.RdfBuilder;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.stores.SimpleBlobStore;
import org.topazproject.otm.impl.SessionFactoryImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Common superclass for integration tests.
 */
public abstract class AbstractTest extends GroovyTestCase {
  private static final Log log = LogFactory.getLog(AbstractTest.class);

  def rdf;
  def store;
  def blobStore;

  protected def models = [['ri', 'otmtest1', null]];

  void setUp() {
    store = new ItqlStore("local:///topazproject".toURI(),
                          new DefaultItqlClientFactory(dbDir: "target/mulgara-db"))
    blobStore = new SimpleBlobStore("target/blob-store");
    rdf = new RdfBuilder(
        sessFactory:new SessionFactoryImpl(tripleStore:store, blobStore:blobStore), defModel:'ri', defUriPrefix:'topaz:')

    for (c in models) {
      def m = new ModelConfig(c[0], "local:///topazproject#${c[1]}".toURI(), c[2])
      rdf.sessFactory.addModel(m)
      try { store.dropModel(m); } catch (Throwable t) { }
      store.createModel(m)
    }
  }

  protected def doInTx(Closure c) {
    Session s = rdf.sessFactory.openSession()
    s.beginTransaction()
    try {
      def r = c(s)
      s.transaction.commit()
      return r
    } catch (OtmException e) {
      try {
        s.transaction.rollback()
      } catch (OtmException oe) {
        log.warn("rollback failed", oe);
      }
      log.error("error: ${e}", e)
      throw e
    } finally {
      try {
        s.close();
      } catch (OtmException oe) {
        log.warn("close failed", oe);
      }
    }
  }
}
