/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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

package org.topazproject.ambra.migration;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.ambra.cache.OtmInterceptor;
import org.topazproject.ambra.models.Ambra;
import org.topazproject.ambra.models.AnnotationBlob;
import org.topazproject.ambra.models.Annotea;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.Category;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.models.Reply;
import org.topazproject.ambra.models.ReplyBlob;
import org.topazproject.ambra.models.TextRepresentation;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Connection;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SearchStore;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Graph;
import org.topazproject.otm.annotations.Graphs;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.metadata.SearchableDefinition;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.util.TransactionHelper;

/**
 * Index/re-index all Searchable fields into Lucene Graph.
 *
 * @author Pradeep Krishnan
 */
public class SearchMigrator implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(SearchMigrator.class);
  private SessionFactory sf;
  private OtmInterceptor oi;
  private int rdfThrottle = 3000;
  private int blobThrottle = 100;
  private int txnTimeout = 1800;
  private boolean finalize = true;
  private boolean reIndex = false;
  private boolean background = true;
  private Set<String> errorSet = new HashSet<String>();

  @Required
  public void setOtmSessionFactory(SessionFactory sf) {
    this.sf = sf;
  }

  @Required
  public void setOtmInterceptor(OtmInterceptor oi) {
    this.oi = oi;
  }

  /**
   * The number of OTM entity instances to index/re-index per txn.
   *
   * @param rdfThrottle the throttling to apply
   */
  public void setRdfThrottle(int rdfThrottle) {
    this.rdfThrottle = rdfThrottle;
  }

  /**
   * The number of blobs to index/re-index per txn.
   *
   * @param blobThrottle the throttling to apply
   */
  public void setBlobThrottle(int blobThrottle) {
    this.blobThrottle = blobThrottle;
  }

  /**
   * The txn time out value to set. Defaults to 10min.
   *
   * @param txnTimeout the timeout value to set
   */
  public void setTxnTimeout(int txnTimeout) {
    this.txnTimeout = txnTimeout;
  }

  /**
   * Discard all the previous indexing information and
   * start all over again.
   *
   * @param val the re-index value
   */
  public void setReIndex(boolean val) {
    reIndex = val;
  }

  /**
   * Finalize when the migrations are all done and place a
   * marker statement. Turn it on in production environment
   * to mark when doing the final run of the migrations.
   *
   * @param val the finalize value
   */
  public void setFinalize(boolean val) {
    finalize = val;
  }

  /**
   * Whether to run this migration in back-ground allowing web-traffic to proceed.
   *
   * @param val the background flag value
   */
  public void setBackground(boolean val) {
    background = val;
  }

  public void init() {
    if (!background)
      migrate();
    else {
      Thread t = new Thread(this, "Search-Migrator");
      t.setPriority(Thread.NORM_PRIORITY - 2);
      t.setDaemon(true);
      t.start();
    }
  }

  public void run() {
    try {
      migrate();
    } catch (RuntimeException e) {
      log.error("Uncaught exception. Exiting ...", e);
    } catch (Error e) {
      log.error("Uncaught error. Exiting ...", e);
    }
  }

  public long migrate() {
    log.info("Search Migrations starting ...");
    sf.preload(MigrationMarker.class);
    sf.preload(SearchMarker.class);
    sf.validate();

    int count = TransactionHelper.doInTx(sf, false, txnTimeout,
                                         new TransactionHelper.Action<Integer>() {
      public Integer run(Transaction tx) {
        return setup(tx.getSession());
      }
    });

    if (count > 0) {
      log.info("Nothing to do. Marker says search indexes are all migrated.");
      return 0;
    }

    long total = 0;
    for (ClassMetadata cm : getSearchableClasses()) {
      int throttle = (getSd(cm, cm.getBlobField()) != null) ? blobThrottle : rdfThrottle;
      long subtotal = 0;
      errorSet.clear();
      int err;
      do {
        err = errorSet.size();
        count = migrate(cm, throttle + err, finalize);
        if (count < 0) {
          log.info("Indexed all instances of " + cm);
          subtotal = count;
        } else {
          log.info("Indexed " + count + " instances of " + cm);
          total += count;
          subtotal += count;
        }
      } while ((count > 0) || (err < errorSet.size()));

      if (subtotal == 0)
        log.info("Nothing to do for " + cm);
      else
        log.info("Finished migrating " + cm);
    }

    finishUp();

    log.warn("Search Migration completed.");

    return total;
  }

  private int setup(Session s) {
    MigrationMarker mm = s.get(MigrationMarker.class, MigrationMarker.ID);

    if (reIndex) {
      log.warn("Re-Indexing ...");

      if (mm != null)
        mm.setSearchMigrated(0);

      try {
        log.info("Dropping 'sm' ...");
        s.dropGraph("sm");
      } catch (OtmException e) {
        if (log.isDebugEnabled())
          log.debug("drop-graph of 'sm' failed", e);
      }

      try {
        log.info("Dropping 'sm-real' ...");
        s.dropGraph("sm-real");
      } catch (OtmException e) {
        if (log.isDebugEnabled())
          log.debug("drop-graph of 'sm-real' failed", e);
      }
    }

    if ((finalize == false) && (mm != null))
      mm.setSearchMigrated(0);

    if ((mm == null) || (mm.getSearchMigrated() == 0)) {
      s.createGraph("sm");
      return 0;
    }

    return 1;
  }

  private void finishUp() {
    if (!finalize)
      return;

    TransactionHelper.doInTx(sf, false, txnTimeout, new TransactionHelper.Action<Void>() {
      public Void run(Transaction tx) {
        Session s = tx.getSession();
        MigrationMarker mm = new MigrationMarker();
        mm.setSearchMigrated(1);
        s.saveOrUpdate(mm);
        return null;
      }
    });
  }

  private int migrate(final ClassMetadata cm, final int throttle, final boolean throwError) {
    Session session = sf.openSession(oi);

    try {
      return TransactionHelper.doInTx(session, false, txnTimeout,
                                      new TransactionHelper.Action<Integer>() {
        public Integer run(Transaction tx) {
          return migrate(tx.getSession(), cm, throttle, throwError);
        }
      });
    } finally {
      try {
        session.close();
      } catch (Exception e) {
        log.warn("Failed to close session", e);
      }
    }
  }

  private int migrate(Session session, ClassMetadata cm, int throttle, boolean throwError) {
    SearchStore ss = (SearchStore)sf.getTripleStore();
    Connection con = session.getTripleStoreCon();
    Set<SearchableDefinition> fields = getSearchableFields(cm);

    String is = this.getInsertSelect(cm, throttle);
    if (is != null) {
      log.info("Performing insert select of " + cm + " using : " + is);
      session.doNativeUpdate(is);
      return -1; // indicates 'countless' instances migrated
    }

    String qs = getQuery(cm, throttle);
    log.info("Looking for un-indexed " + cm + " using : " + qs);
    Results r = session.doNativeQuery(qs);
    int count = 0;
    log.info("Creating indexes for " + cm);

    while (r.next()) {
      String id = r.getString(0);
      if (errorSet.contains(id))
        continue;
      try {
        Object o = session.get(cm.getName(), id);
        if (isInstance(o, cm)) {
          log.info("Creating search indexes for " + cm + " with id: " + id);
          ss.index(cm, fields, id, o, con);
          count++;
        }
        session.saveOrUpdate(new SearchMarker(id));
        session.flush();
      } catch (RuntimeException e) {
        if (throwError)
          throw e;
        else {
          log.error("Failed to create search indexes for " + cm + " with id: " + id, e);
          errorSet.add(id);
        }
      } finally {
        session.clear();
      }
    }
    return count;
  }

  private boolean isInstance(Object o, ClassMetadata cm) {
    // Nasty hack
    return !(o instanceof TextRepresentation)
           || ((TextRepresentation)o).getContentType().startsWith("text/");
  }

  private String getQuery(ClassMetadata cm, int throttle) {
    StringBuilder qs = new StringBuilder();
    URI sm = sf.getGraph("sm").getUri();
    String st = sf.getClassMetadata(SearchMarker.class).getTypes().iterator().next();

    qs.append("select $s from <").append(sm).append("> where (");
    buildWhere(qs, cm);
    qs.append(") minus $s <rdf:type> <").append(st).append("> limit ").append(throttle).append(";");

    return qs.toString();
  }

  private String getInsertSelect(ClassMetadata cm, int throttle) {
    if (getSd(cm, cm.getBlobField()) != null)
      return null;

    URI lucene = sf.getGraph("lucene").getUri();
    Map<URI, Set<RdfMapper>> gms = new HashMap<URI, Set<RdfMapper>>();
    for (RdfMapper m : cm.getRdfMappers()) {
      SearchableDefinition sd = getSd(cm, m);
      if (sd == null)
        continue;
      if (sd.getPreProcessor() != null)
        return null;

      String g = m.getGraph();
      if (g == null)
        g = cm.getGraph();
      URI gu = sf.getGraph(g).getUri();
      Set<RdfMapper> ms = gms.get(gu);
      if (ms == null)
        gms.put(gu, ms = new HashSet<RdfMapper>());
      ms.add(m);
    }

    StringBuilder q = new StringBuilder();
    for (Map.Entry<URI, Set<RdfMapper>> e : gms.entrySet()) {
      q.append("insert select $s $pidx $oidx from <")
       .append(e.getKey())
       .append("> where $s $pidx $oidx  and (");
      for (RdfMapper m : e.getValue())
        q.append("$pidx <mulgara:is> <").append(m.getUri()).append("> or ");

      q.setLength(q.length() - 4);
      q.append(") and (");
      buildWhere(q, cm);
      q.append(") into <").append(lucene).append(">;");
    }

    return q.toString();
  }

  private void buildWhere(StringBuilder qs, ClassMetadata cm) {
    String s = "$s";
    URI g;

    if (cm.getGraph() != null)
      g = sf.getGraph(cm.getGraph()).getUri();
    else {
      if (cm == sf.getClassMetadata(AnnotationBlob.class))
        cm = sf.getClassMetadata(ArticleAnnotation.class);
      else if (cm == sf.getClassMetadata(ReplyBlob.class))
        cm = sf.getClassMetadata(Reply.class);
      else
        throw new OtmException("Need special query building for " + cm);

      g = sf.getGraph(cm.getGraph()).getUri();
      s = "$a";

      qs.append("$a <").append(((RdfMapper)cm.getMapperByName("body")).getUri())
        .append("> $s in <").append(g).append("> and ");

      if (cm == sf.getClassMetadata(ArticleAnnotation.class))
        qs.append("($a <rdf:type> <").append(Comment.RDF_TYPE)
          .append("> or $a <rdf:type> <")
          .append(Annotea.W3C_TYPE_NS).append("Change>) and ");
    }

    if (cm == sf.getClassMetadata(Category.class)) {
      cm = sf.getClassMetadata(Article.class);
      s = "$a";
      qs.append("$a <").append(((RdfMapper)cm.getMapperByName("categories")).getUri())
        .append("> $s in <").append(g).append("> and ");
    } else if (cm == sf.getClassMetadata(TextRepresentation.class)) {
      cm = sf.getClassMetadata(Article.class);
      s = "$a";
      qs.append("$a <").append(((RdfMapper)cm.getMapperByName("representations")).getUri())
        .append("> $s in <").append(g).append("> and ");
    }

    if (cm.getTypes().isEmpty())
      throw new OtmException("Need special query building for " + cm);

    for (String t : cm.getTypes())
      qs.append(s).append(" <rdf:type> <").append(t).append("> in <").append(g).append("> and ");

    qs.setLength(qs.length() - 5);
  }

  private SearchableDefinition getSd(ClassMetadata cm, Mapper m) {
    if (m == null)
      return null;

    // return SearchableDefinition.findForProp(sf, m.getDefinition().getName());
    return SearchableDefinition.findForProp(sf, cm.getName() + ":" + m.getName());
  }

  private Set<SearchableDefinition> getSearchableFields(ClassMetadata cm) {
    Set<SearchableDefinition> defs = new HashSet<SearchableDefinition>();

    defs.add(getSd(cm, cm.getBlobField()));

    for (RdfMapper m : cm.getRdfMappers())
      defs.add(getSd(cm, m));

    defs.remove(null);

    return defs;
  }

  private Set<ClassMetadata> getSearchableClasses() {
    Set<ClassMetadata> scm = new HashSet<ClassMetadata>();
    for (ClassMetadata cm : sf.listClassMetadata()) {
      if (cm.isView())
        continue;

      boolean searchable = false;
      if (getSd(cm, cm.getBlobField()) != null)
        searchable = true;

      if (!searchable) {
        if (cm.getGraph() == null)
          continue;
        for (RdfMapper m : cm.getRdfMappers()) {
          if (getSd(cm, m) != null) {
            searchable = true;
            break;
          }
        }
      }

      if (!searchable)
        continue;

      Iterator<ClassMetadata> iter = scm.iterator();
      boolean addThis = true;
      while(iter.hasNext()) {
        ClassMetadata c = iter.next();
        if (c.isAssignableFrom(cm, EntityMode.POJO))
          addThis = false;
        else if (cm.isAssignableFrom(c, EntityMode.POJO))
          iter.remove();
      }
      if (addThis)
        scm.add(cm);
    }
    return scm;
  }

  @Entity(name = "MigrationMarker", graph="ri")
  public static class MigrationMarker {
    public static final String ID = "migrator:migrations";

    private String id = ID;
    private int searchMigrated = 0;

    @Id
    public void setId(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }

    @Predicate(uri="method:searchMigrated")
    public void setSearchMigrated(int searchMigrated) {
      this.searchMigrated = searchMigrated;
    }

    public int getSearchMigrated() {
      return searchMigrated;
    }
  }

  @Graphs({
    @Graph(id = "sm", uri = Ambra.GRAPH_PREFIX + "filter:graph=sm",
           type = Ambra.TYPE_PREFIX + "filter"),
    @Graph(id = "sm-real", uri = Ambra.GRAPH_PREFIX + "sm")
  })
  @Entity(name="SearchMarker", graph = "sm", types = {"topaz:searchMarker"})
  public static class SearchMarker {
    private String id;

    public SearchMarker() {
    }

    public SearchMarker(String id) {
      setId(id);
    }

    @Id
    public void setId(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }
  }
}
