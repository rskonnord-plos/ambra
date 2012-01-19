/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
package org.topazproject.ambra.migration;

import java.net.URI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.configuration.WebappItqlClientFactory;
import org.topazproject.ambra.models.Ambra;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.stores.ItqlStore;

/**
 * Does migrations on startup.
 *
 * @author Pradeep Krishnan
 */
public class BootstrapMigrator {
  private static Logger    log = LoggerFactory.getLogger(BootstrapMigrator.class);
  private static final String RI  = Ambra.GRAPH_PREFIX + "ri";

  /**
   * Apply all migrations.
   *
   * @throws Exception on an error
   */
  public void migrate() throws Exception {
    Session sess            = null;
    Transaction tx          = null;

    try {
      Configuration conf    = ConfigurationStore.getInstance().getConfiguration();
      URI           service = new URI(conf.getString("ambra.topaz.tripleStore.mulgara.itql.uri"));

      log.info("Checking and performing data-migrations ...");
      SessionFactory factory = new SessionFactoryImpl();
      factory.setTripleStore(new ItqlStore(service, WebappItqlClientFactory.getInstance()));

      sess = factory.openSession();
      tx = sess.beginTransaction(false, 60*60);
      List<String> graphs = getGraphs(sess, service);
      if (log.isDebugEnabled())
        log.debug("Found the following graphs: " + graphs);

      int count = dropObsoleteGraphs(sess, graphs);
      count += addXsdIntToTopazState(sess, graphs);
      tx.commit();
      tx = null;
      if (count == 0)
        log.info("Nothing to do. Everything was already migrated.");
      else
        log.warn("Committed " + count + " data-migrations.");
    } finally {
      try {
        if (tx != null)
          tx.rollback();
      } catch (Throwable t) {
        log.warn("Error in rollback", t);
      }
      try {
        if (sess != null)
          sess.close();
      } catch (Throwable t) {
        log.warn("Error closing session", t);
      }
    }
  }

  public List<String> getGraphs(Session session, URI mulgara) throws OtmException{
    List<String> graphs = new ArrayList<String>();
    Results r = session.doNativeQuery("select $s from <" + mulgara + "#> where $s $p $o;");
    while (r.next())
      graphs.add(r.getString(0));

    return graphs;
  }

  /**
   * Drop obsolete graphs. Ignore the exceptions as graphs might not exist.
   *
   * @param session the Topaz session
   * @param graphs the list of graphs in mulgara
   * @return the number of migrations performed
   * @throws OtmException on an error
   */
  public int dropObsoleteGraphs(Session session, List<String> graphs) throws OtmException {
    String og = Ambra.GRAPH_PREFIX + "str";
    if (!graphs.contains(og)) {
      log.info("Skipped dropObsoleteGraphs since <" + og + "> is not in the list of graphs");
      return 0;
    }

    session.doNativeUpdate("drop <" + og + "> ;");
    return 1;
  }

  /**
   * Add xsd:int to topaz:state.
   *
   * @param sess the otm session to use
   * @param graphs the list of graphs in mulgara
   * @return the number of migrations performed
   * @throws OtmException on an error
   */
  public int addXsdIntToTopazState(Session sess, List<String> graphs) throws OtmException {
    if (!graphs.contains(RI)) {
      log.info("Skipped addXsdIntToTopazState since <" + RI + "> is not in the list of graphs");
      return 0;
    }
    String marker = "<migrator:migrations> <method:addXsdIntToTopazState> ";
    log.info("Adding xsd:int to topaz:state fields ...");

    // FIXME: Remove the marker. Blocked on  http://mulgara.org/trac/ticket/153
    Results r = sess.doNativeQuery("select $o from <" + RI + "> where " + marker + "$o;");

    if (r.next() && "1".equals(r.getString(0))) {
     log.info("Marker statement says this migration is already done.");
     // log.info("Did not find any <topaz:state> statements without an <xsd:int> data-type.");
      return 0;
    }

    r = sess.doNativeQuery("select $s $o from <" + RI + "> where $s <topaz:state> $o;");
    Map<String, String> map = new HashMap<String, String>();  // not that many; so this is fine.
    while(r.next()) {
      Results.Literal v = r.getLiteral(1);
      if (v.getDatatype() == null)
        map.put(r.getString(0), v.getValue());
    }

    StringBuilder b = new StringBuilder(2500);
    b.append("delete ");
    for (String k : map.keySet()) {
      b.append("<" + k + "> <topaz:state> '" + map.get(k) + "' ");
      if (b.length() > 2000) {
        b.append(" from <" + RI + ">;");
        if (log.isDebugEnabled())
          log.debug(b.toString());
        sess.doNativeUpdate(b.toString());
        b.setLength(0);
        b.append("delete ");
      }
    }

    if (b.length() > 7) {
      b.append(" from <" + RI + ">;");
      if (log.isDebugEnabled())
        log.debug(b.toString());
      sess.doNativeUpdate(b.toString());
    }

    b.setLength(0);
    b.append("insert ");
    for (String k : map.keySet()) {
      b.append("<" + k + "> <topaz:state> '" + map.get(k) + "'^^<xsd:int> ");
      if (b.length() > 2000) {
        b.append(" into <" + RI + ">;");
        if (log.isDebugEnabled())
          log.debug(b.toString());
        sess.doNativeUpdate(b.toString());
        b.setLength(0);
        b.append("insert ");
      }
    }

    b.append(marker).append("'1' into <" + RI + ">;");
    if (log.isDebugEnabled())
      log.debug(b.toString());
    sess.doNativeUpdate(b.toString());

    log.warn("Added ^^<xsd:int> to " + map.size() + " <topaz:state> literals.");

    return map.size();
  }
}
