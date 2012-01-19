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
package org.topazproject.otm.stores;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlClient;
import org.topazproject.mulgara.itql.ItqlClientFactory;
import org.topazproject.mulgara.itql.DefaultItqlClientFactory;
import org.topazproject.otm.AbstractConnection;
import org.topazproject.otm.Blob;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.Connection;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.Filter;
import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.RdfUtil;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.SearchStore;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.criterion.itql.ComparisonCriterionBuilder;
import org.topazproject.otm.filter.AbstractFilterImpl;
import org.topazproject.otm.mapping.AbstractMapper;
import org.topazproject.otm.mapping.PropertyBinder;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.mapping.java.AbstractFieldBinder;
import org.topazproject.otm.metadata.Definition;
import org.topazproject.otm.metadata.PropertyDefinition;
import org.topazproject.otm.metadata.RdfDefinition;
import org.topazproject.otm.metadata.SearchableDefinition;
import org.topazproject.otm.query.GenericQuery;
import org.topazproject.otm.query.QueryException;
import org.topazproject.otm.query.QueryInfo;
import org.topazproject.otm.query.Results;
import org.topazproject.util.functional.Pair;

/**
 * An iTQL based store.
 *
 * @author Ronald Tschalär
 */
public class ItqlStore extends AbstractTripleStore implements SearchStore {
  private static final Log log = LogFactory.getLog(ItqlStore.class);
  private        final List<ItqlClient>  conCache = new ArrayList<ItqlClient>();
  private        final ItqlClientFactory itqlFactory;
  private        final URI               serverUri;

  // temporary hack for problems with mulgara's blank-nodes (must be unique for whole tx)
  private              int               bnCtr = 0;

  /**
   * Create a new itql-store instance.
   * The server URI will take one of two forms:
   * <ul>
   *   <li>rmi://hostname/server</li>
   *   <li>local:///server</li>
   * </ul>
   * The RMI scheme is used to access an external TQL store.
   * The <code>hostname</code> refers to the name of the computer that the
   * store is running on. The <code>server</code> is the name of the server
   * that TQL store is providing. By default, Mulgara servers are run with a
   * name of "server1". So to access a server on the local machine, the URI
   * might be: <code>rmi://localhost/server1</code>
   *
   * The <code>LOCAL</code> scheme is used to access an embedded Mulgara
   * database in the current JVM. The host name is <code>null</code> which
   * leads to the root of the path coming immediately after the "//"
   * characters. The server name is defined here in order to create the
   * database, so it can be anything.
   *
   * Graphs created on this store should use the server URI with an added
   * fragment. For instance:
   * <ul>
   *   <li>rmi://localhost/server1#data</li>
   *   <li>local:///myserver#data</li>
   * </ul>
   *
   * @param server  the uri of the iTQL server.
   * @throws OtmException OTM Exception
   */
  public ItqlStore(URI server) throws OtmException {
    this(server, new DefaultItqlClientFactory());
  }

  /**
   * Create a new itql-store instance.
   *
   * @param server  the uri of the iTQL server.
   * @param icf     the itql-client-factory to use
   * @throws OtmException OTM Exception
   */
  public ItqlStore(URI server, ItqlClientFactory icf) throws OtmException {
    serverUri   = server;
    itqlFactory = icf;

    //XXX: configure these
    ComparisonCriterionBuilder cc = new ComparisonCriterionBuilder();
    critBuilders.put("gt", cc);
    critBuilders.put("ge", cc);
    critBuilders.put("lt", cc);
    critBuilders.put("le", cc);
  }

  public Connection openConnection(Session sess, boolean readOnly) throws OtmException {
    return new ItqlStoreConnection(sess, readOnly);
  }

  public <T> void insert(ClassMetadata cm, Collection<RdfMapper> fields, String id, T o,
                         Connection con) throws OtmException {
    insertInternal(cm, fields, id, o, con);
  }

  public <T> void index(ClassMetadata cm, Collection<SearchableDefinition> fields, String id, T o,
                        Connection con) throws OtmException {
    Collection<Mapper> mappers = new ArrayList<Mapper>();

    for (SearchableDefinition sd : fields) {
      final Map<EntityMode, PropertyBinder> propertyBinders = new HashMap<EntityMode, PropertyBinder>();
      final SearchableDefinition def = sd;

      Mapper m = cm.getMapperByName(sd.getLocalName());
      if (m instanceof RdfMapper) {
        propertyBinders.putAll(m.getBinders());
      } else {
        Session sess = ((ItqlStoreConnection) con).getSession();
        propertyBinders.put(sess.getEntityMode(), getBlobBinder(cm, sess, id));
      }

      m = new AbstractMapper(propertyBinders) {
        public PropertyDefinition getDefinition() {
          return def;
        }
      };
      mappers.add(m);
    }

    insertInternal(cm, mappers, id, o, con);
  }

  private static PropertyBinder getBlobBinder(final ClassMetadata cm, final Session sess,
                                              final String id) {
    final PropertyBinder origBinder = cm.getBlobField().getBinder(sess);
    if (origBinder.getSerializer() != null)
      return origBinder;

    // FIXME: this has too many assumptions...
    return new AbstractFieldBinder(null, null, null) {
      public List get(Object o) throws OtmException {

        Blob blob = sess.getSessionFactory().getBlobStore()
                                            .getBlob(cm, id, o, sess.getBlobStoreCon());
        byte[] b = blob.exists() ? blob.readAll() : null;

        if ((b == null) || (b.length == 0))
          return Collections.emptyList();

        try {
          return Collections.singletonList(new String(b, "UTF-8"));
        } catch (UnsupportedEncodingException uee) {
          throw new OtmException("Unexpected error", uee);
        }
      }

      public void set(Object o, List l) {
      }
    };
  }

  private <T, M extends Mapper> void insertInternal(ClassMetadata cm, Collection<M> fields,
                                                    String id, T o, Connection con)
      throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) con;

    Map<String, List<M>> mappersByGraph = groupMappersByGraph(cm, fields);

    // for every graph create an insert statement
    for (String g : mappersByGraph.keySet()) {
      StringBuilder insert = isc.getInserts().get(g);
      if (insert == null)
        isc.getInserts().put(g, insert = new StringBuilder(500));

      if (g.equals(cm.getGraph())) {
        for (String type : cm.getAllTypes())
          addStmt(insert, id, Rdf.rdf + "type", type, null, true);
      }

      buildInsert(insert, mappersByGraph.get(g), id, o, isc.getSession());
    }
  }

  public void flush(Connection con) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) con;
    StringBuilder insert = new StringBuilder(500);

    for (String g : isc.getInserts().keySet()) {
      StringBuilder stmts = isc.getInserts().get(g);
      if (stmts.length() == 0)
        continue;

      insert.append("insert ").append(stmts).
             append("into <").append(getGraphUri(g, isc)).append(">;");
    }

    isc.getInserts().clear();

    if (log.isDebugEnabled())
      log.debug("flush: " + insert);

    if (insert.length() == 0)
      return;

    try {
      isc.getItqlClient().doUpdate(insert.toString());
    } catch (IOException ioe) {
      throw new OtmException("error performing flush", ioe);
    }
  }

  private static <T> Map<String, List<T>> groupMappersByGraph(ClassMetadata cm,
                                                              Collection<T> fields) {
    Map<String, List<T>> mappersByGraph = new HashMap<String, List<T>>();

    if (cm.getAllTypes().size() > 0)
      mappersByGraph.put(cm.getGraph(), new ArrayList<T>());

    for (T p : fields) {
      String g =
          getGraph((p instanceof Definition) ? (Definition) p : ((Mapper) p).getDefinition(), cm);
      List<T> pList = mappersByGraph.get(g);
      if (pList == null)
        mappersByGraph.put(g, pList = new ArrayList<T>());
      pList.add(p);
    }

    return mappersByGraph;
  }

  private static String getGraph(Definition def, ClassMetadata cm) {
    if (def instanceof RdfDefinition) {
      RdfDefinition rd = (RdfDefinition) def;
      return (rd.getGraph() != null) ? rd.getGraph() : cm.getGraph();
    }

    if (def instanceof SearchableDefinition) {
      return ((SearchableDefinition) def).getIndex();
    }

    throw new Error("Unexpected definition type: " + def.getClass() + ": " + def);
  }

  @SuppressWarnings("unchecked")
  private void buildInsert(StringBuilder buf, List<? extends Mapper> pList, String id, Object o,
                           Session sess)
      throws OtmException {
    for (Mapper p : pList) {
      Definition def = p.getDefinition();
      PropertyBinder     b   = p.getBinder(sess);

      if (def instanceof SearchableDefinition) {
        SearchableDefinition sd = (SearchableDefinition) def;
        List<String> objs = (List<String>) b.get(o);

        if (sd.getPreProcessor() != null) {
          List<String> procd = new ArrayList<String>(objs.size());
          for (String obj : objs)
            procd.add(sd.getPreProcessor().process(o, sd, obj));
          objs = procd;
        }

        addStmts(buf, id, sd.getUri(), objs, null, false, CollectionType.PREDICATE,
                 "$s" + bnCtr++ + "i", false);
        continue;
      }

      RdfDefinition rd = (RdfDefinition) def;
      if (!rd.isEntityOwned())
        continue;

      if (rd.isPredicateMap()) {
        Map<String, List<String>> pMap = (Map<String, List<String>>) b.getRawValue(o, true);
        for (String k : pMap.keySet())
          for (String v : pMap.get(k))
            addStmt(buf, id, k, v, null, false); // xxx: uri or literal?
      } else if (!rd.isAssociation())
        addStmts(buf, id, rd.getUri(), (List<String>) b.get(o) , rd.getDataType(),
                 rd.typeIsUri(), rd.getColType(), "$s" + bnCtr++ + "i", rd.hasInverseUri());
      else
        addStmts(buf, id, rd.getUri(), sess.getIds(b.get(o)) , null,true, rd.getColType(),
                 "$s" + bnCtr++ + "i", rd.hasInverseUri());
    }

    if (bnCtr > 1000000000)
      bnCtr = 0;        // avoid negative numbers
  }

  private static void addStmts(StringBuilder buf, String subj, String pred, List<String> objs,
                               String dt, boolean objIsUri, CollectionType mt, String prefix,
                               boolean inverse) {
    int i = 0;
    switch (mt) {
      case PREDICATE:
        for (String obj : objs) {
          if (!inverse)
            addStmt(buf, subj, pred, obj, dt, objIsUri);
          else
            addStmt(buf, obj, pred, subj, dt, true);
        }
        break;

      case RDFLIST:
        if (objs.size() > 0)
          buf.append("<").append(subj).append("> <").append(pred).append("> ")
             .append(prefix).append("0 ");
        for (String obj : objs) {
          addStmt(buf, prefix+i, "rdf:type", "rdf:List", null, true);
          addStmt(buf, prefix+i, "rdf:first", obj, dt, objIsUri);
          if (i > 0)
            buf.append(prefix).append(i-1).append(" <rdf:rest> ")
                .append(prefix).append(i).append(" ");
          i++;
        }
        if (i > 0)
          addStmt(buf, prefix+(i-1), "rdf:rest", "rdf:nil", null, true);
        break;

      case RDFBAG:
      case RDFSEQ:
      case RDFALT:
        String rdfType = (CollectionType.RDFBAG == mt) ? "<rdf:Bag> " :
                         ((CollectionType.RDFSEQ == mt) ? "<rdf:Seq> " : "<rdf:Alt> ");
        if (objs.size() > 0) {
          buf.append("<").append(subj).append("> <").append(pred).append("> ")
             .append(prefix).append(" ");
          buf.append(prefix).append(" <rdf:type> ").append(rdfType);
        }
        for (String obj : objs)
          addStmt(buf, prefix, "rdf:_" + ++i, obj, dt, objIsUri);
        break;
    }
  }

  private static void addStmt(StringBuilder buf, String subj, String pred, String obj, String dt,
                              boolean objIsUri) {
    if (!subj.startsWith("$"))
      buf.append("<").append(subj).append("> <").append(pred);
    else
      buf.append(subj).append(" <").append(pred);

    if (objIsUri) {
      buf.append("> <").append(obj).append("> ");
    } else {
      buf.append("> '").append(RdfUtil.escapeLiteral(obj));
      if (dt != null)
        buf.append("'^^<").append(dt).append("> ");
      else
        buf.append("' ");
    }
  }

  public <T> void delete(ClassMetadata cm, Collection<RdfMapper> fields, String id, T o,
                         Connection con) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) con;

    final boolean partialDelete = (cm.getRdfMappers().size() != fields.size());
    Map<String, List<RdfMapper>> mappersByGraph = groupMappersByGraph(cm, fields);
    StringBuilder delete = new StringBuilder(500);

    // for every graph create a delete statement
    for (String g : mappersByGraph.keySet()) {
      int len = delete.length();
      delete.append("delete ");

      if (buildDeleteSelect(delete, getGraphUri(g, isc), mappersByGraph.get(g),
                      !partialDelete && g.equals(cm.getGraph()) && cm.getAllTypes().size() > 0, id))
        delete.append("from <").append(getGraphUri(g, isc)).append(">;");
      else
        delete.setLength(len);
    }

    doDelete(isc, delete.toString());
  }

  public <T> void remove(ClassMetadata cm, Collection<SearchableDefinition> fields, String id, T o,
                         Connection con) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) con;

    Map<String, List<SearchableDefinition>> defsByGraph = groupMappersByGraph(cm, fields);
    StringBuilder delete = new StringBuilder(500);

    // for every index and graph create a delete statement
    for (String index : defsByGraph.keySet()) {
      Map<String, List<RdfMapper>> rdfMappersByGraph =
          groupMappersByGraph(cm, findRdfMappersForSearch(cm, defsByGraph.get(index)));

      for (String g : rdfMappersByGraph.keySet()) {
        int len = delete.length();
        delete.append("delete ");

        if (buildDeleteSelect(delete, getGraphUri(g, isc), rdfMappersByGraph.get(g), false, id))
          delete.append("from <").append(getGraphUri(index, isc)).append(">;");
        else
          delete.setLength(len);
      }
    }

    doDelete(isc, delete.toString());
  }

  private static Collection<RdfMapper> findRdfMappersForSearch(ClassMetadata cm,
                                                               List<SearchableDefinition> defs) {
    Collection<RdfMapper> mappers = new ArrayList<RdfMapper>();

    for (SearchableDefinition def : defs) {
      Mapper m = cm.getMapperByName(def.getLocalName());
      if (m instanceof RdfMapper)
        mappers.add((RdfMapper) m);
    }

    return mappers;
  }

  public <T> void remove(ClassMetadata cm, SearchableDefinition field, String id, T o,
                         Connection con) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) con;

    String g = getGraphUri(field.getIndex(), isc);
    String c = "<" + id + "> <" + field.getUri() + "> $o";

    String tql = "delete select " + c + " from <" + g + "> where " + c + " from <" + g + ">;";
    doDelete(isc, tql);
  }

  private void doDelete(ItqlStoreConnection isc, String delete) throws OtmException {
    if (log.isDebugEnabled())
      log.debug("delete: " + delete);

    if (delete.length() == 0)
      return;

    try {
      isc.getItqlClient().doUpdate(delete);
    } catch (IOException ioe) {
      throw new OtmException("error performing update: " + delete, ioe);
    }
  }

  private boolean buildDeleteSelect(StringBuilder qry, String graph, List<RdfMapper> rdfMappers,
                                    boolean delTypes, String id)
      throws OtmException {
    qry.append("select $s $p $o from <").append(graph).append("> where ");
    int len = qry.length();

    // build forward statements
    boolean found = delTypes;
    boolean predicateMap = false;
    for (RdfMapper p : rdfMappers) {
      if (!p.hasInverseUri() && p.isEntityOwned()) {
        if (p.isPredicateMap()) {
          predicateMap = true;
          found = false;
          break;
        }
        found = true;
      }
    }

    if (predicateMap)
      qry.append("($s $p $o and $s <mulgara:is> <").append(id).append(">) ");

    if (found) {
      qry.append("($s $p $o and $s <mulgara:is> <").append(id).append("> and (");
      if (delTypes)
        qry.append("$p <mulgara:is> <rdf:type> or ");
      for (RdfMapper p : rdfMappers) {
        if (!p.hasInverseUri() && p.isEntityOwned())
          qry.append("$p <mulgara:is> <").append(p.getUri()).append("> or ");
      }
      qry.setLength(qry.length() - 4);
      qry.append(") ) ");
    }

    // build reverse statements
    boolean needDisj = found | predicateMap;
    found = false;
    for (RdfMapper p : rdfMappers) {
      if (p.hasInverseUri() && p.isEntityOwned()) {
        found = true;
        break;
      }
    }

    if (found) {
      if (needDisj)
        qry.append("or ");
      qry.append("($s $p $o and $o <mulgara:is> <").append(id).append("> and (");
      for (RdfMapper p : rdfMappers) {
        if (p.hasInverseUri() && p.isEntityOwned())
          qry.append("$p <mulgara:is> <").append(p.getUri()).append("> or ");
      }
      qry.setLength(qry.length() - 4);
      qry.append(") ) ");
    }

    // build rdf:List, rdf:Bag, rdf:Seq or rdf:Alt statements
    Collection<RdfMapper> col = null;
    for (RdfMapper p : rdfMappers) {
      if (p.getColType() == CollectionType.RDFLIST) {
        if (col == null)
          col = new ArrayList<RdfMapper>();
        col.add(p);
      }
    }

    if (col != null) {
      qry.append("or ($s $p $o and <").append(id).append("> $x $c ")
        .append("and (trans($c <rdf:rest> $s) or $c <rdf:rest> $s or <").append(id).append("> $x $s)")
        .append(" and $s <rdf:type> <rdf:List> and (");
      for (RdfMapper p : col)
        qry.append("$x <mulgara:is> <").append(p.getUri()).append("> or ");
      qry.setLength(qry.length() - 4);
      qry.append(") ) ");
    }

    col = null;
    for (RdfMapper p : rdfMappers) {
      if ((p.getColType() == CollectionType.RDFBAG) ||
          (p.getColType() == CollectionType.RDFSEQ) ||
          (p.getColType() == CollectionType.RDFALT)) {
        if (col == null)
          col = new ArrayList<RdfMapper>();
        col.add(p);
      }
    }

    if (col != null) {
      qry.append("or ($s $p $o and <").append(id).append("> $x $s ")
        .append(" and ($s <rdf:type> <rdf:Bag> ")
        .append("or $s <rdf:type> <rdf:Seq> or $s <rdf:type> <rdf:Alt>) and (");
      for (RdfMapper p : col)
        qry.append("$x <mulgara:is> <").append(p.getUri()).append("> or ");
      qry.setLength(qry.length() - 4);
      qry.append(") ) ");
    }

    return (qry.length() > len);
  }

  public TripleStore.Result get(ClassMetadata cm, final String id, Connection con,
                   final List<Filter> filters, boolean filterObj) throws OtmException {
    final ItqlStoreConnection isc = (ItqlStoreConnection) con;
    // do query
    String get = buildGetSelect(cm, id, isc, filters, filterObj);
    if (log.isDebugEnabled())
      log.debug("get: " + get);

    List<Answer> ans;
    try {
      ans = isc.getItqlClient().doQuery(get);
    } catch (IOException ioe) {
      throw new OtmException("error performing query: " + get, ioe);
    } catch (AnswerException ae) {
      throw new OtmException("error performing query: " + get, ae);
    }

    // parse
    final Map<String, List<String>> fvalues = new HashMap<String, List<String>>();
    final Map<String, List<String>> rvalues = new HashMap<String, List<String>>();

    try {
      Map<String, List<String>> values = fvalues;
      for (Answer qa : ans) {
        if (qa.getVariables() == null)
          throw new OtmException("query failed: " + qa.getMessage());

        if (values == null)
          throw new OtmException("query failed: expecting 2 answers. got " + ans.size());

        // collect the results, grouping by predicate
        qa.beforeFirst();
        while (qa.next()) {
          String p = qa.getString(0);
          String o = qa.getString(1);

          List<String> v = values.get(p);
          if (v == null)
            values.put(p, v = new ArrayList<String>());
          v.add(o);
        }
        qa.close();
        // prepare to read inverse query results (if not already done)
        values = (values == rvalues) ? null : rvalues;
      }
    } catch (AnswerException ae) {
      throw new OtmException("Error parsing answer", ae);
    }

    return new TripleStore.Result() {
      public Map<String, List<String>> getFValues() {
        return fvalues;
      }

      public Map<String, List<String>> getRValues() {
        return rvalues;
      }
    };
  }

  private String buildGetSelect(ClassMetadata cm, String id, ItqlStoreConnection isc,
                                List<Filter> filters, boolean filterObj) throws OtmException {
    String     graphs = getGraphsExpr(Collections.singleton(cm), isc);
    StringBuilder qry = new StringBuilder(500);

    qry.append("select $p $o from ").append(graphs).append(" where ");
    qry.append("<").append(id).append("> $p $o");
    if (filterObj) {
      String subjConstr = " and $s <mulgara:is> <" + id + ">";
      if (applyObjectFilters(qry, cm, "$s", subjConstr, filters, isc.getSession()))
        qry.append(subjConstr);
    }

    qry.append("; select $p $s from ").append(graphs).append(" where ");
    qry.append("$s $p <").append(id).append(">");
    if (filterObj) {
      String subjConstr = " and $o <mulgara:is> <" + id + ">";
      if (applyObjectFilters(qry, cm, "$o", subjConstr, filters, isc.getSession()))
        qry.append(subjConstr);
    }
    qry.append(";");

    return qry.toString();
  }

  private boolean applyObjectFilters(StringBuilder qry, ClassMetadata cm, String var, String constr,
                                     List<Filter> filters, Session session) throws OtmException {
    // avoid work if possible
    if (filters == null || filters.size() == 0)
      return false;

    // find applicable filters
    SessionFactory             sf     = session.getSessionFactory();
    List<Pair<Filter,Boolean>> finfos = new ArrayList<Pair<Filter,Boolean>>();

    for (Filter f : filters) {
      ClassMetadata fcm = sf.getClassMetadata(f.getFilterDefinition().getFilteredClass());
      if (fcm.isAssignableFrom(cm, session.getEntityMode()))
        finfos.add(new Pair<Filter,Boolean>(f, true));
      else if (cm.isAssignableFrom(fcm, session.getEntityMode()))
        finfos.add(new Pair<Filter,Boolean>(f, false));
    }

    if (finfos.size() == 0)
      return false;

    // apply filters
    qry.append(" and (");

    int idx = 0;
    for (Pair<Filter,Boolean> i : finfos) {
      qry.append("(");
      ItqlCriteria.buildFilter((AbstractFilterImpl) i.first(), i.second(), qry, var, constr,
                               "$gof" + idx++, session);
      qry.append(") or ");
    }

    qry.setLength(qry.length() - 4);
    qry.append(")");

    return true;
  }

  private static String getGraphsExpr(Set<? extends ClassMetadata> cmList,
                                      ItqlStoreConnection isc)
      throws OtmException {
    Set<String> gList = new HashSet<String>();
    for (ClassMetadata cm : cmList) {
      gList.add(getGraphUri(cm.getGraph(), isc));
      for (RdfMapper p : cm.getRdfMappers()) {
        if (p.getGraph() != null)
          gList.add(getGraphUri(p.getGraph(), isc));
      }
    }

    StringBuilder mexpr = new StringBuilder(100);
    for (String g : gList)
      mexpr.append("<").append(g).append("> or ");
    mexpr.setLength(mexpr.length() - 4);

    return mexpr.toString();
  }

  public List<String> getRdfList(String sub, String pred, String graphUri, Connection con)
        throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) con;
    StringBuilder qry = new StringBuilder(500);
    qry.append("select $o $s $n from <").append(graphUri).append("> where ")
       .append("(trans($c <rdf:rest> $s) or $c <rdf:rest> $s or <")
       .append(sub).append("> <").append(pred).append("> $s) and <")
       .append(sub).append("> <").append(pred).append("> $c and $s <rdf:first> $o")
       .append(" and $s <rdf:rest> $n;");

    return execCollectionsQry(qry.toString(), isc);
  }

  public List<String> getRdfBag(String sub, String pred, String graphUri, Connection con)
        throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) con;

    /* Optimization note: using
     *   ($s $p $o and <foo> <bar> $s) minus ($s <rdf:type> $o and <foo> <bar> $s)
     * is (more than 30 times) faster than the more succinct
     *   ($s $p $o minus $s <rdf:type> $o) and <foo> <bar> $s
     */
    StringBuilder qry = new StringBuilder(500);
    qry.append("select $o $p from <")
       .append(graphUri).append("> where ")
       .append("(($s $p $o and <").append(sub).append("> <").append(pred).append("> $s) minus ")
       .append("($s <rdf:type> $o and <").append(sub).append("> <").append(pred).append("> $s));");

    return execCollectionsQry(qry.toString(), isc);
  }

  private List<String> execCollectionsQry(String qry, ItqlStoreConnection isc) throws OtmException {
    if (log.isDebugEnabled())
      log.debug("rdf:List/rdf:Bag query : " + qry);

    List<Answer> ans;
    try {
      ans = isc.getItqlClient().doQuery(qry);
    } catch (IOException ioe) {
      throw new OtmException("error performing rdf:List/rdf:Bag query", ioe);
    } catch (AnswerException ae) {
      throw new OtmException("error performing rdf:List/rdf:Bag query", ae);
    }

    List<String> res = new ArrayList<String>();

    try {
      // check if we got something useful
      Answer qa = ans.get(0);
      if (qa.getVariables() == null)
        throw new OtmException("query failed: " + qa.getMessage());

      // collect the results,
      qa.beforeFirst();
      boolean isRdfList = qa.indexOf("n") != -1;
      if (isRdfList) {
        Map<String, String> fwd  = new HashMap<String, String>();
        Map<String, String> rev  = new HashMap<String, String>();
        Map<String, String> objs = new HashMap<String, String>();
        while (qa.next()) {
          String assoc = qa.getString("o");
          String node  = qa.getString(1);
          String next  = qa.getString(2);
          objs.put(node, assoc);
          fwd.put(node, next);
          rev.put(next, node);
        }

        if (objs.size() == 0)
          return res;

        String first = rev.keySet().iterator().next();
        while (rev.containsKey(first))
          first = rev.get(first);
        for (String n = first; fwd.get(n) != null; n = fwd.get(n))
          res.add(objs.get(n));
      } else {
        while (qa.next()) {
          String p = qa.getString("p");
          if (!p.startsWith(Rdf.rdf + "_"))
            continue; // an un-recognized predicate
          int i = Integer.decode(p.substring(Rdf.rdf.length() + 1)) - 1;
          while (i >= res.size())
            res.add(null);
          String assoc = qa.getString("o");
          res.set(i, assoc);
        }
      }
      qa.close();
    } catch (AnswerException ae) {
      throw new OtmException("Error parsing answer", ae);
    }
    return res;
  }

  public List list(Criteria criteria, Connection con) throws OtmException {
    ItqlCriteria ic = new ItqlCriteria(criteria);
    String qry = ic.buildUserQuery();
    log.debug("list: " + qry);
    List<Answer> ans;
    try {
      ItqlStoreConnection isc = (ItqlStoreConnection) con;
      ans = isc.getItqlClient().doQuery(qry);
    } catch (IOException ioe) {
      throw new OtmException("error performing query: " + qry, ioe);
    } catch (AnswerException ae) {
      throw new OtmException("error performing query: " + qry, ae);
    }
    return ic.createResults(ans);
  }

  public Results doQuery(GenericQuery query, Collection<Filter> filters, Connection con)
      throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) con;

    ItqlQuery iq = new ItqlQuery(query, filters, isc.getSession());
    QueryInfo qi = iq.parseItqlQuery();

    List<Answer> ans;
    try {
      ans = isc.getItqlClient().doQuery(qi.getQuery());
    } catch (IOException ioe) {
      throw new QueryException("error performing query '" + query + "'", ioe);
    } catch (AnswerException ae) {
      throw new QueryException("error performing query '" + query + "'", ae);
    }

    return new ItqlOQLResults(ans.get(0), qi, iq.getWarnings().toArray(new String[0]),
                              isc.getSession());
  }

  public Results doNativeQuery(String query, Connection con) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) con;
    List<Answer> ans;
    try {
      ans = isc.getItqlClient().doQuery(query);
    } catch (IOException ioe) {
      throw new QueryException("error performing query '" + query + "'", ioe);
    } catch (AnswerException ae) {
      throw new QueryException("error performing query '" + query + "'", ae);
    }

    if (ans.get(0).getMessage() != null)
      throw new QueryException("error performing query '" + query + "' - message was: " +
                               ans.get(0).getMessage() +
                               "\nDid you attempt to use doNativeQuery for a non-query command?" +
                               " If so, please use doNativeUpdate instead");

    return new ItqlNativeResults(ans.get(0), isc.getSession());
  }

  public void doNativeUpdate(String command, Connection con) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) con;
    try {
      isc.getItqlClient().doUpdate(command);
    } catch (IOException ioe) {
      throw new QueryException("error performing command '" + command + "'", ioe);
    }
  }

  private static String getGraphUri(String graphId, ItqlStoreConnection isc) throws OtmException {
    GraphConfig mc = isc.getSession().getSessionFactory().getGraph(graphId);
    if (mc == null) // Happens if using a Class but the graph was not added
      throw new OtmException("Unable to find graph '" + graphId + "'");
    return mc.getUri().toString();
  }

  private ItqlClient getItqlClient(Map<String, String> aliases, boolean useCache)
      throws OtmException {
    ItqlClient res = null;

    while (useCache && (res = removeTopEntry()) != null) {
      try {
        res.ping();

        if (log.isDebugEnabled())
          log.debug("Got itql-client from connection-cache: " + res);
        break;
      } catch (IOException ioe) {
        res.close();
        if (log.isDebugEnabled())
          log.debug("Discarding failed itql-client: " + res, ioe);
      }
    }

    if (res == null) {
      try {
        res = itqlFactory.createClient(serverUri);
        if (log.isDebugEnabled())
          log.debug("Created new itql-client: " + res);
      } catch (Exception e) {
        throw new OtmException("Error talking to '" + serverUri + "'", e);
      }
    }

    if (aliases != null)
      res.setAliases(aliases);

    return res;
  }

  private ItqlClient removeTopEntry() {
    synchronized (conCache) {
      return conCache.isEmpty() ? null : conCache.remove(conCache.size() - 1);
    }
  }

  private void returnItqlClient(ItqlClient itql) {
    if (log.isDebugEnabled())
      log.debug("Returning itql-client to connection-cache: " + itql);

    synchronized (conCache) {
      conCache.add(itql);
    }
  }

  /**
   * Creates a new graph in the TQL store.
   *
   * @param conf the configuration
   * @param con a TQL connection
   * @throws OtmException on an error
   */
  public void createGraph(GraphConfig conf, Connection con) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection)con;
    String type = (conf.getType() == null) ? "mulgara:Model" : conf.getType().toString();
    try {
      isc.getItqlClient().doUpdate("create <" + conf.getUri() + "> <" + type + ">;");
    } catch (Exception e) {
      throw new OtmException("Failed to create graph <" + conf.getUri() + ">", e);
    }
  }

  /**
   * Drops a graph in the persistence store, deleting all triples.
   *
   * @param conf the configuration
   * @param con a TQL connection
   *
   * @throws OtmException on an error
   */
  public void dropGraph(GraphConfig conf, Connection con) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection)con;
    try {
      isc.getItqlClient().doUpdate("drop <" + conf.getUri() + ">;");
    } catch (Exception e) {
      throw new OtmException("Failed to remove graph <" + conf.getUri() + ">", e);
    }
  }

  private class ItqlStoreConnection extends AbstractConnection {
    private ItqlClient   itql;
    private Map<String, StringBuilder> inserts = new HashMap<String, StringBuilder>();

    public ItqlStoreConnection(Session sess, boolean readOnly) throws OtmException {
      super(sess);
      itql = ItqlStore.this.getItqlClient(sess.getSessionFactory().listAliases(), true);
      itql.clearLastError();

      try {
        enlistResource(readOnly ? itql.getReadOnlyXAResource() : itql.getXAResource());
      } catch (IOException ioe) {
        throw new OtmException("Error getting xa-resource", ioe);
      }
    }

    public ItqlClient getItqlClient() throws OtmException {
      return itql;
    }

    public Map<String, StringBuilder> getInserts() {
      return inserts;
    }

    public void close() {
      Exception e = itql.getLastError();
      if (!(e instanceof IOException || e != null && e.getCause() instanceof IOException))
        returnItqlClient(itql);
      else
        itql.close();
      itql = null;
    }
  }
}
