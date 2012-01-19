/* $HeadURL::                                                                            $
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
 */
package org.plos.bootstrap.migration;

import java.net.URI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;
import org.plos.configuration.WebappItqlClientFactory;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.RdfUtil;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.util.TransactionHelper;

/**
 * A listener class for doing migrations on startup.
 *
 * @author Pradeep Krishnan
 */
public class Migrator implements ServletContextListener {
  private static Log    log = LogFactory.getLog(Migrator.class);
  private static String RI;

  /**
   * Shutdown things.
   *
   * @param event the destryed event
   */
  public void contextDestroyed(ServletContextEvent event) {
  }

  /**
   * Initialize things.
   *
   * @param event init event
   *
   * @throws Error to abort
   */
  public void contextInitialized(ServletContextEvent event) {
    try {
      migrate();
    } catch (Exception e) {
      throw new Error("A data-migration operation failed. Aborting ...", e);
    }
  }

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
      RI                    = stripFilterResolver(conf.getString("ambra.models.ri"));

      log.info("Checking and performing data-migrations ...");
      SessionFactory factory = new SessionFactoryImpl();
      factory.setTripleStore(new ItqlStore(service, WebappItqlClientFactory.getInstance()));

      sess = factory.openSession();
      // Adding new statements (these shouldn't affect old app)
      tx = sess.beginTransaction(false, 15*60);
      int count = addObjInfoType(sess);
      tx.commit();
      tx = null;
      // Now do the main stuff (switch over to new)
      tx = sess.beginTransaction(false, 60*60);
      count += migrateArticleParts(sess) +
        migrateReps(sess);
      tx.commit();
      tx = null;
      // Now do the cleanup of stuff that new app doesn't care about
      tx = sess.beginTransaction(false, 15*60);
      count += removePIDs(sess) +
         removeObsoleteStates(sess);
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

  private String stripFilterResolver(String model) {
    int pos = model.indexOf("filter:model=");
    if (pos > 0) {
      model = model.substring(0, pos) + model.substring(pos + 13);
      log.warn("By-passing filter resolver. Updates will be to  <" + model + ">");
    }
    return model;
  }

  /**
   * Migrate the Article parts from a topaz-defined linked list to an RdfSeq. See rev 5594.
   *
   * @param sess the otm session to use
   * @return the number of migrations performed
   * @throws OtmException on an error
   */
  public int migrateArticleParts(Session sess) throws OtmException {
    log.info("Checking if Article-parts need data-migration ...");

    int artCnt = 0;
    int objCnt = 0;

    Results r =
      sess.doNativeQuery("select $a $f subquery " +
                         "  (select $s <topaz:nextObject> $o from <" + RI + "> " +
                         "   where $s <topaz:nextObject> $o and $a <dcterms:hasPart> $s) " +
                         "from <" + RI + "> where $a <rdf:type> <topaz:Article> and " +
                         "$a <topaz:nextObject> $f;");

    ArrayList<String> arts = new ArrayList<String>(7000);
    ArrayList<String> firsts = new ArrayList<String>(7000);
    ArrayList<Map<String,String>> linkss = new ArrayList<Map<String,String>>(7000);
    while (r.next()) {
      artCnt++;

      arts.add(r.getString("a"));
      firsts.add(r.getString("f"));
      Results sub   = r.getSubQueryResults(2);

      Map<String, String> links = new HashMap<String, String>();
      while (sub.next())
        links.put(sub.getString("s"), sub.getString("o"));

      objCnt += links.size();

      linkss.add(links);
    }

    if (artCnt == 0) {
      log.info("Did not find any article that required data-migration for its parts.");
      return 0;
    }

    log.info("Deleting topaz:nextObject statements for " + objCnt + " objects ...");
    StringBuilder del = new StringBuilder(5000);
    for (int i = 0; i < artCnt; i++) {

      String art = arts.get(i);
      String first = firsts.get(i);
      Map<String, String> links = linkss.get(i);

      if (del.length() == 0)
        del.append("delete ");

      int idx = 1;
      for (String p = first, prev = art; p != null; prev = p, p = links.get(p)) {
        del.append("<").append(art).append("> <dcterms:hasPart> <").append(p).append("> ");
        del.append("<").append(prev).append("> <topaz:nextObject> <").append(p).append("> ");
      }

      if (del.length() > 3000) {
        del.append("from <").append(RI).append(">;");
        sess.doNativeUpdate(del.toString());
        del.setLength(0);
      }
    }

    if (del.length() > 0) {
      del.append("from <").append(RI).append(">;");
      sess.doNativeUpdate(del.toString());
      del.setLength(0);
    }

    log.info("Inserting dcterms:hasPart as an rdf:Seq for " + artCnt + " articles ...");
    StringBuilder ins = del; // re-use
    for (int i = 0; i < artCnt; i++) {

      String art = arts.get(i);
      String first = firsts.get(i);
      Map<String, String> links = linkss.get(i);

      if (ins.length() == 0)
        ins.append("insert ");

      String seq = " $seq" + i;
      ins.append("<").append(art).append("> <dcterms:hasPart>" + seq + seq + " <rdf:type> <rdf:Seq> ");

      int idx = 1;
      for (String p = first, prev = art; p != null; prev = p, p = links.get(p)) {
        ins.append(seq).append(" <rdf:_").append(idx++).append("> <").append(p).append("> ");
      }

      if (ins.length() > 3000) {
        ins.append("into <").append(RI).append(">;");
        sess.doNativeUpdate(ins.toString());
        ins.setLength(0);
      }
    }

    if (ins.length() > 0) {
      ins.append("into <").append(RI).append(">;");
      sess.doNativeUpdate(ins.toString());
      ins.setLength(0);
    }

    if (artCnt > 0)
      log.warn("Finished data-migration of Article parts. " + artCnt + " Articles and " +
               objCnt + " ObjectInfo objects migrated.");

    return artCnt + objCnt;
  }


  /**
   * Remove any isPID statements. See rev 5594.
   *
   * @param sess the otm session to use
   * @return 0
   * @throws OtmException on an error
   */
  public int removePIDs(Session sess) throws OtmException {
    log.info("Removing all <topaz:isPID> statements");
    sess.doNativeUpdate("delete select $s <topaz:isPID> $o from <" + RI +
                        "> where $s <topaz:isPID> $o from <" + RI + ">;");

    return 0;
  }

  /**
   * Migrate the Article Representations.
   *
   * @param sess the otm session to use
   *
   * @return the number of migrations performed
   *
   * @throws OtmException on an error
   */
  public int migrateReps(Session sess) throws OtmException {
    final String C_T = "-contentType";
    final String O_S = "-objectSize";
    log.info("Checking if Representations need data-migration ...");

    Results                                 r       =
      sess.doNativeQuery("select $s subquery (select $p $o from <" + RI + "> where $s $p $o) "
                         + " from <" + RI + "> where "
                         + " $s <topaz:hasRepresentation> $rep "
                         + " minus $rep <rdf:type> <topaz:Representation>;");

    Map<String, Collection<Representation>> objs = new HashMap<String, Collection<Representation>>();

    int repCnt = 0;

    while (r.next()) {
      String                      id   = r.getString(0);
      Results                     sub  = r.getSubQueryResults(1);
      Map<String, Representation> reps = new HashMap<String, Representation>();

      while (sub.next()) {
        String p = sub.getString(0);

        if (!p.startsWith(Rdf.topaz))
          continue;

        if (p.endsWith(C_T)) {
          String rep = p.substring(Rdf.topaz.length(), p.length() - C_T.length());
          getRep(reps, rep).contentType = sub.getLiteral(1);
        } else if (p.endsWith(O_S)) {
          String rep = p.substring(Rdf.topaz.length(), p.length() - O_S.length());
          getRep(reps, rep).objectSize = sub.getLiteral(1);
        } else if (p.equals(Rdf.topaz + "hasRepresentation")) {
          getRep(reps, sub.getString(1));
        }
      }

      repCnt += reps.size();
      if (reps.size() > 0)
        objs.put(id, reps.values());
    }

    if (repCnt == 0) {
      log.info("Did not find any object that required data-migration for its Representations.");
      return 0;
    }

    log.info("Deleting " + repCnt + " old Representations ...");
    StringBuilder b = new StringBuilder(5000);
    for (String id : objs.keySet()) {
      buildDeleteReps(id, objs.get(id), b);
      if (b.length() > 3000) {
        b.append("from <" + RI + ">;");
        sess.doNativeUpdate(b.toString());
        b.setLength(0);
      }
    }

    if (b.length() > 0 ) {
      b.append("from <" + RI + ">;");
      sess.doNativeUpdate(b.toString());
      b.setLength(0);
    }

    log.info("Inserting " + repCnt + " new Representations ...");
    for (String id : objs.keySet()) {
      buildInsertReps(id, objs.get(id), b);
      if (b.length() > 3000) {
        b.append("into <" + RI + ">;");
        sess.doNativeUpdate(b.toString());
        b.setLength(0);
      }
    }

    if (b.length() > 0 ) {
      b.append("into <" + RI + ">;");
      sess.doNativeUpdate(b.toString());
      b.setLength(0);
    }


    log.warn("Finished data-migration of " + repCnt + " Representations. " + objs.size()
             + " ObjectInfo objects migrated.");

    return repCnt;
  }

  private void buildDeleteReps(String id, Collection<Representation> reps, StringBuilder b) {
    final String C_T = "-contentType";
    final String O_S = "-objectSize";

    if (b.length() == 0)
      b.append("delete ");

    for (Representation rep : reps) {
      b.append("<" + id + "> <topaz:hasRepresentation> '" + rep.name + "' ");
      if (rep.contentType != null) {
        b.append("<" + id + "> <topaz:" +  rep.name + C_T + "> '"
                   + RdfUtil.escapeLiteral(rep.contentType.getValue()) + "'");

        if (rep.contentType.getDatatype() != null)
          b.append("^^<" + rep.contentType.getDatatype() + ">");

        b.append(" ");
      }

      if (rep.objectSize != null) {
        b.append("<" + id + "> <topaz:" + rep.name + O_S + "> '"
                   + RdfUtil.escapeLiteral(rep.objectSize.getValue()) + "'");

        if (rep.objectSize.getDatatype() != null)
          b.append("^^<" + rep.objectSize.getDatatype() + ">");

        b.append(" ");
      }
    }
  }

  private void buildInsertReps(String id, Collection<Representation> reps, StringBuilder b) {
    if (b.length() == 0)
      b.append("insert ");

    for (Representation rep : reps) {
      String rid = id + "/" + rep.name;
      b.append("<" + id + "> <" + Rdf.topaz + "hasRepresentation> <" + rid + "> ");
      b.append("<" + rid + "> <rdf:type> <" + Rdf.topaz + "Representation> ");
      b.append("<" + rid + "> <" + Rdf.dc_terms + "identifier> '" + rep.name + "' ");

      if (rep.contentType != null) {
        b.append("<" + rid + "> <" + Rdf.topaz + "contentType> '"
                   + RdfUtil.escapeLiteral(rep.contentType.getValue()) + "'");

        if (rep.contentType.getDatatype() != null)
          b.append("^^<" + rep.contentType.getDatatype() + ">");

        b.append(" ");
      }

      if (rep.objectSize != null) {
        b.append("<" + rid + "> <" + Rdf.topaz + "objectSize> '"
                   + RdfUtil.escapeLiteral(rep.objectSize.getValue()) + "'");

        if (rep.objectSize.getDatatype() != null)
          b.append("^^<" + rep.objectSize.getDatatype() + ">");

        b.append(" ");
      }
    }
  }

  private Representation getRep(Map<String, Representation> reps, String rep) {
    Representation o = reps.get(rep);

    if (o == null) {
      o        = new Representation();
      o.name   = rep;
      reps.put(rep, o);
    }

    return o;
  }

  private static class Representation {
    public String          name;
    public Results.Literal contentType;
    public Results.Literal objectSize;
  }

  /**
   * Add the rdf:type for ObjectInfo's.
   *
   * @param sess the otm session to use
   * @return the number of migrations performed
   * @throws OtmException on an error
   */
  public int addObjInfoType(Session sess) throws OtmException {
    log.info("Adding rdf:type to ObjectInfo's ...");

    Results r = sess.doNativeQuery(
          "select count(select $s from <" + RI + "> where " +
          "             ($s <dcterms:isPartOf> $o or $s <rdf:type> <topaz:Article>) " +
          "             minus $s <rdf:type> <topaz:ObjectInfo>) " +
          "from <" + RI + "> where $dummy <mulgara:is> 'ignored';");
    r.next();
    int cnt = (int) Double.parseDouble(r.getString(0));

    if (cnt == 0) {
      log.info("Did not find any ObjectInfo that required an rdf:type to be added.");
    } else {
      sess.doNativeUpdate("insert select $s <rdf:type> <topaz:ObjectInfo> from <" + RI +
                          "> where $s <dcterms:isPartOf> $o " + 
                          " or $s <rdf:type> <topaz:Article> into <" + RI + ">;");
      log.warn("Added rdf:type to " + cnt + " ObjectInfo's.");
    }

    return cnt;
  }

  /**
   * Removes articleState's on ObjectInfo's and Category's.
   *
   * @param sess the otm session to use
   * @return the number of migrations performed
   * @throws OtmException on an error
   */
  public int removeObsoleteStates(Session sess) throws OtmException {
    log.info("Removing obsolete state fields ...");

    Results r = sess.doNativeQuery(
          "select count(select $s from <" + RI + "> where " +
          "             $s <topaz:articleState> $o minus $s <rdf:type> <topaz:Article>) " +
          "from <" + RI + "> where $dummy <mulgara:is> 'ignored';");
    r.next();
    int cnt = (int) Double.parseDouble(r.getString(0));

    if (cnt == 0) {
      log.info("Did not find any objects with leftover state fields.");
    } else {
      sess.doNativeUpdate("delete select $s <topaz:articleState> $o from <" + RI +
                          "> where $s <topaz:articleState> $o minus $s <rdf:type> <topaz:Article>" +
                          " from <" + RI + ">;");
      log.warn("Removed state fields from " + cnt + " objects.");
    }

    return cnt;
  }
}
