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
import java.net.URI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.Connection;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.Filter;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.RdfUtil;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.criterion.itql.ComparisonCriterionBuilder;
import org.topazproject.otm.filter.AbstractFilterImpl;
import org.topazproject.otm.mapping.Binder;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.query.GenericQueryImpl;
import org.topazproject.otm.query.QueryException;
import org.topazproject.otm.query.QueryInfo;
import org.topazproject.otm.query.Results;

/** 
 * An iTQL based store.
 * 
 * @author Ronald Tschalär
 */
public class ItqlStore extends AbstractTripleStore {
  private static final Log log = LogFactory.getLog(ItqlStore.class);
  private        final List<ItqlClient>  conCache = new ArrayList<ItqlClient>();
  private        final ItqlClientFactory itqlFactory;
  private        final URI               serverUri;

  // temporary hack for problems with mulgara's blank-nodes (must be unique for whole tx)
  private              int               bnCtr = 0;

  /** 
   * Create a new itql-store instance. 
   * 
   * @param server  the uri of the iTQL server.
   */
  public ItqlStore(URI server) throws OtmException {
    this(server, new DefaultItqlClientFactory());
  }

  /** 
   * Create a new itql-store instance. 
   * 
   * @param server  the uri of the iTQL server.
   * @param icf     the itql-client-factory to use
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
    ItqlStoreConnection isc = (ItqlStoreConnection) con;

    Map<String, List<RdfMapper>> mappersByModel = groupMappersByModel(cm, fields);

    // for every model create an insert statement
    for (String m : mappersByModel.keySet()) {
      StringBuilder insert = isc.getInserts().get(m);
      if (insert == null)
        isc.getInserts().put(m, insert = new StringBuilder(500));

      if (m.equals(cm.getModel())) {
        for (String type : cm.getTypes())
          addStmt(insert, id, Rdf.rdf + "type", type, null, true);
      }

      buildInsert(insert, mappersByModel.get(m), id, o, isc.getSession());
    }
  }

  public void flush(Connection con) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) con;
    StringBuilder insert = new StringBuilder(500);

    for (String m : isc.getInserts().keySet()) {
      StringBuilder stmts = isc.getInserts().get(m);
      if (stmts.length() == 0)
        continue;

      insert.append("insert ").append(stmts).
             append("into <").append(getModelUri(m, isc)).append(">;");
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

  private static Map<String, List<RdfMapper>> groupMappersByModel(ClassMetadata cm, 
                                                               Collection<RdfMapper> fields) {
    Map<String, List<RdfMapper>> mappersByModel = new HashMap<String, List<RdfMapper>>();

    if (cm.getTypes().size() > 0)
      mappersByModel.put(cm.getModel(), new ArrayList<RdfMapper>());

    for (RdfMapper p : fields) {
      String m = (p.getModel() != null) ? p.getModel() : cm.getModel();
      List<RdfMapper> pList = mappersByModel.get(m);
      if (pList == null)
        mappersByModel.put(m, pList = new ArrayList<RdfMapper>());
      pList.add(p);
    }

    return mappersByModel;
  }


  private void buildInsert(StringBuilder buf, List<RdfMapper> pList, String id, Object o, Session sess)
      throws OtmException {
    for (RdfMapper p : pList) {
      if (!p.isEntityOwned())
        continue;
      Binder b = p.getBinder(sess);
      if (p.isPredicateMap()) {
        Map<String, List<String>> pMap = (Map<String, List<String>>) b.getRawValue(o, true);
        for (String k : pMap.keySet())
          for (String v : pMap.get(k))
            addStmt(buf, id, k, v, null, false); // xxx: uri or literal?
      } else if (!p.isAssociation())
        addStmts(buf, id, p.getUri(), (List<String>) b.get(o) , p.getDataType(), p.typeIsUri(), 
                 p.getColType(), "$s" + bnCtr++ + "i", p.hasInverseUri());
      else
        addStmts(buf, id, p.getUri(), sess.getIds(b.get(o)) , null,true, p.getColType(), 
                 "$s" + bnCtr++ + "i", p.hasInverseUri());
    }
    if (bnCtr > 1000000000)
      bnCtr = 0;        // avoid negative numbers
  }

  private static void addStmts(StringBuilder buf, String subj, String pred, List<String> objs, 
      String dt, boolean objIsUri, CollectionType mt, String prefix, boolean inverse) {
    int i = 0;
    switch (mt) {
    case PREDICATE:
      for (String obj : objs)
        if (!inverse)
          addStmt(buf, subj, pred, obj, dt, objIsUri);
        else
          addStmt(buf, obj, pred, subj, dt, true);
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
    Map<String, List<RdfMapper>> mappersByModel = groupMappersByModel(cm, fields);
    StringBuilder delete = new StringBuilder(500);

    // for every model create a delete statement
    for (String m : mappersByModel.keySet()) {
      int len = delete.length();
      delete.append("delete ");
      if (buildDeleteSelect(delete, getModelUri(m, isc), mappersByModel.get(m),
                        !partialDelete && m.equals(cm.getModel()) && cm.getTypes().size() > 0, id))
        delete.append("from <").append(getModelUri(m, isc)).append(">;");
      else
        delete.setLength(len);
    }

    if (log.isDebugEnabled())
      log.debug("delete: " + delete);

    if (delete.length() == 0)
      return;

    try {
      isc.getItqlClient().doUpdate(delete.toString());
    } catch (IOException ioe) {
      throw new OtmException("error performing update: " + delete.toString(), ioe);
    }
  }

  private boolean buildDeleteSelect(StringBuilder qry, String model, List<RdfMapper> rdfMappers,
                                 boolean delTypes, String id)
      throws OtmException {
    qry.append("select $s $p $o from <").append(model).append("> where ");
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

  public Object get(ClassMetadata cm, String id, Object instance, Connection con,
                   List<Filter> filters, boolean filterObj) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) con;
    SessionFactory      sf  = isc.getSession().getSessionFactory();

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
    Map<String, List<String>> fvalues = new HashMap<String, List<String>>();
    Map<String, List<String>> rvalues = new HashMap<String, List<String>>();
    Map<String, Set<String>> types = new HashMap<String, Set<String>>();


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

          updateTypeMap(qa.getSubQueryResults(2), o, types);
        }
        qa.close();
        // prepare to read inverse query results (if not already done)
        values = (values == rvalues) ? null : rvalues;
      }
    } catch (AnswerException ae) {
      throw new OtmException("Error parsing answer", ae);
    }

    if (fvalues.size() == 0 && rvalues.size() == 0) {
      if (log.isDebugEnabled())
        log.debug("No statements found about '" + id + "' for " + cm);
      return null;
    }

    // figure out sub-class to instantiate
    // XXX: should this be narrowed down based on other restrictions?
    List<String> t = fvalues.get(Rdf.rdf + "type");
    ClassMetadata sup = cm;
    cm = sf.getSubClassMetadata(cm, isc.getSession().getEntityMode(), t);
    if (cm == null) {
      HashSet<String> props = new HashSet<String>(fvalues.keySet());
      props.addAll(rvalues.keySet());
      log.warn("Properties " + props + " on '" + id 
          + "' does not satisfy the restrictions imposed by " + sup
          + ". No object will be instantiated.");
      return null;
    }
    if (t != null)
      t.removeAll(cm.getTypes());

    // pre-process for special constructs (rdf:List, rdf:bag, rdf:Seq, rdf:Alt)
    String modelUri = getModelUri(cm.getModel(), isc);
    Set<String> replaced = null;
    for (RdfMapper m : cm.getRdfMappers()) {
      if (m.hasInverseUri() || !fvalues.containsKey(m.getUri()) || 
          (m.getColType() == CollectionType.PREDICATE))
        continue;

      String p = m.getUri();
      String mUri = (m.getModel() != null) ? getModelUri(m.getModel(), isc) : modelUri;
      List<String> vals;
      if (m.getColType() == CollectionType.RDFLIST)
        vals = getRdfList(id, p, mUri, isc, types, m, sf, filters);
      else
        vals = getRdfBag(id, p, mUri, isc, types, m, sf, filters);

      if (replaced == null)
        replaced = new HashSet<String>();

      if (replaced.contains(p))
        fvalues.get(p).addAll(vals);
      else {
        fvalues.put(p, vals);
        replaced.add(p);
      }
    }

    if (log.isDebugEnabled())
      log.debug("Instantiating object with '" + id + "' for " + cm);

    return instantiate(isc.getSession(), instance, cm, id, fvalues, rvalues, types);
  }

  private String buildGetSelect(ClassMetadata cm, String id, ItqlStoreConnection isc,
                                List<Filter> filters, boolean filterObj) throws OtmException {
    SessionFactory        sf     = isc.getSession().getSessionFactory();
    Set<ClassMetadata> fCls   = listFieldClasses(cm, sf);
    String                models = getModelsExpr(Collections.singleton(cm), isc);
    String                tmdls  = fCls.size() > 0 ? getModelsExpr(fCls, isc) : models;
    List<RdfMapper>          assoc  = listAssociations(cm, sf);

    StringBuilder qry = new StringBuilder(500);

    qry.append("select $p $o subquery (select $t from ").append(tmdls).append(" where ");
    qry.append("$o <rdf:type> $t) ");
    qry.append("from ").append(models).append(" where ");
    qry.append("<").append(id).append("> $p $o");
    if (filterObj) {
      if (applyObjectFilters(qry, cm, "$s", filters, sf))
        qry.append(" and $s <mulgara:is> <").append(id).append(">");
    }
    applyFieldFilters(qry, assoc, true, id, filters, sf);

    qry.append("; select $p $s subquery (select $t from ").append(models).append(" where ");
    qry.append("$s <rdf:type> $t) ");
    qry.append("from ").append(models).append(" where ");
    qry.append("$s $p <").append(id).append(">");
    if (filterObj) {
      if (applyObjectFilters(qry, cm, "$o", filters, sf))
        qry.append(" and $o <mulgara:is> <").append(id).append(">");
    }
    applyFieldFilters(qry, assoc, false, id, filters, sf);
    qry.append(";");

    return qry.toString();
  }

  private boolean applyObjectFilters(StringBuilder qry, ClassMetadata cm, String var,
                                     List<Filter> filters, SessionFactory sf) throws OtmException {
    // avoid work if possible
    if (filters == null || filters.size() == 0)
      return false;

    // find applicable filters
    filters = new ArrayList<Filter>(filters);
    for (Iterator<Filter> iter = filters.iterator(); iter.hasNext(); ) {
      Filter f = iter.next();
      ClassMetadata fcm = sf.getClassMetadata(f.getFilterDefinition().getFilteredClass());
      if (!fcm.isAssignableFrom(cm))
        iter.remove();
    }

    if (filters.size() == 0)
      return false;

    // apply filters
    qry.append(" and (");

    int idx = 0;
    for (Filter f : filters) {
      qry.append("(");
      ItqlCriteria.buildFilter((AbstractFilterImpl) f, qry, var, "$gof" + idx++);
      qry.append(") or ");
    }

    qry.setLength(qry.length() - 4);
    qry.append(")");

    return true;
  }

  private void applyFieldFilters(StringBuilder qry, List<RdfMapper> assoc, boolean fwd, String id,
                                 List<Filter> filters, SessionFactory sf)
        throws OtmException {
    // avoid work if possible
    if (filters == null || filters.size() == 0 || assoc.size() == 0)
      return;

    // build predicate->filter map
    Map<String, Set<Filter>> applicFilters = new HashMap<String, Set<Filter>>();
    for (RdfMapper m : assoc) {
      ClassMetadata mc = sf.getClassMetadata(m.getAssociatedEntity());

      for (Filter f : filters) {
        ClassMetadata fc = sf.getClassMetadata(f.getFilterDefinition().getFilteredClass());
        if (fc == null)
          continue;       // bug???

        if (fc.isAssignableFrom(mc)) {
          Set<Filter> fset = applicFilters.get(m.getUri());
          if (fset == null)
            applicFilters.put(m.getUri(), fset = new HashSet<Filter>());
          fset.add(f);
        }
      }
    }

    if (applicFilters.size() == 0)
      return;

    // a little optimization: try to group filters
    Map<Set<Filter>, Set<String>> filtersToPred = new HashMap<Set<Filter>, Set<String>>();
    for (String p : applicFilters.keySet()) {
      Set<Filter> fset = applicFilters.get(p);
      Set<String> pset = filtersToPred.get(fset);
      if (pset == null)
        filtersToPred.put(fset, pset = new HashSet<String>());
      pset.add(p);
    }

    // apply filters
    StringBuilder predList = new StringBuilder(500);
    qry.append(" and (");

    int idx = 0;
    for (Set<Filter> fset : filtersToPred.keySet()) {
      qry.append("(");

      qry.append("(");
      Set<String> pset = filtersToPred.get(fset);
      for (String pred : pset) {
        String predExpr = "$p <mulgara:is> <" + pred + "> or ";
        qry.append(predExpr);
        predList.append(predExpr);
      }
      qry.setLength(qry.length() - 4);
      qry.append(") and ((");

      for (Filter f : fset) {
        ItqlCriteria.buildFilter((AbstractFilterImpl) f, qry, fwd ? "$o" : "$s", "$gff" + idx++);
        qry.append(") and (");
      }
      qry.setLength(qry.length() - 7);
      qry.append("))) or ");
    }

    predList.setLength(predList.length() - 4);
    if (fwd)
      qry.append("(<").append(id).append("> $p $any1 minus (");
    else
      qry.append("($any1 $p <").append(id).append("> minus (");
    qry.append(predList).append(")))");
  }

  private static String getModelsExpr(Set<? extends ClassMetadata> cmList,
                                      ItqlStoreConnection isc)
      throws OtmException {
    Set<String> mList = new HashSet<String>();
    for (ClassMetadata cm : cmList) {
      mList.add(getModelUri(cm.getModel(), isc));
      for (RdfMapper p : cm.getRdfMappers()) {
        if (p.getModel() != null)
          mList.add(getModelUri(p.getModel(), isc));
      }
    }

    StringBuilder mexpr = new StringBuilder(100);
    for (String m : mList)
      mexpr.append("<").append(m).append("> or ");
    mexpr.setLength(mexpr.length() - 4);

    return mexpr.toString();
  }

  private static Set<ClassMetadata> listFieldClasses(ClassMetadata cm, SessionFactory sf) {
    Set<ClassMetadata> clss = new HashSet<ClassMetadata>();

    for (RdfMapper p : cm.getRdfMappers()) {
      ClassMetadata c = sf.getClassMetadata(p.getAssociatedEntity());
      if ((c != null) && ((c.getTypes().size() + c.getRdfMappers().size()) > 0))
        clss.add(c);
    }

    return clss;
  }

  private static List<RdfMapper> listAssociations(ClassMetadata cm, SessionFactory sf) {
    List<RdfMapper> rdfMappers = new ArrayList<RdfMapper>();

    for (ClassMetadata c : allSubClasses(cm, sf)) {
      for (RdfMapper p : c.getRdfMappers()) {
        if (p.isAssociation())
          rdfMappers.add(p);
      }
    }

    return rdfMappers;
  }

  private static Set<ClassMetadata> allSubClasses(ClassMetadata top, SessionFactory sf) {
    Set<ClassMetadata> classes = new HashSet<ClassMetadata>();
    classes.add(top);

    for (ClassMetadata cm : sf.listClassMetadata()) {
      if (cm.isAssignableFrom(top))
        classes.add(cm);
    }

    return classes;
  }

  private List<String> getRdfList(String sub, String pred, String modelUri, ItqlStoreConnection isc,
                                  Map<String, Set<String>> types, RdfMapper m, SessionFactory sf,
                                  List<Filter> filters)
        throws OtmException {
    String tmodel = modelUri;
    if (m.isAssociation())
      tmodel = getModelUri(sf.getClassMetadata(m.getAssociatedEntity()).getModel(), isc);
    StringBuilder qry = new StringBuilder(500);
    qry.append("select $o $s $n subquery (select $t from <").append(tmodel)
       .append("> where $o <rdf:type> $t) from <").append(modelUri).append("> where ")
       .append("(trans($c <rdf:rest> $s) or $c <rdf:rest> $s or <")
       .append(sub).append("> <").append(pred).append("> $s) and <")
       .append(sub).append("> <").append(pred).append("> $c and $s <rdf:first> $o")
       .append(" and $s <rdf:rest> $n");
    if (m.isAssociation())
      applyObjectFilters(qry, sf.getClassMetadata(m.getAssociatedEntity()), "$o", filters, sf);
    qry.append(";");

    return execCollectionsQry(qry.toString(), isc, types);
  }

  private List<String> getRdfBag(String sub, String pred, String modelUri, ItqlStoreConnection isc,
                                 Map<String, Set<String>> types, RdfMapper m, SessionFactory sf,
                                 List<Filter> filters)
        throws OtmException {
    String tmodel = modelUri;
    if (m.isAssociation())
      tmodel = getModelUri(sf.getClassMetadata(m.getAssociatedEntity()).getModel(), isc);

    /* Optimization note: using
     *   ($s $p $o and <foo> <bar> $s) minus ($s <rdf:type> $o and <foo> <bar> $s)
     * is (more than 30 times) faster than the more succinct
     *   ($s $p $o minus $s <rdf:type> $o) and <foo> <bar> $s
     */
    StringBuilder qry = new StringBuilder(500);
    qry.append("select $o $p subquery (select $t from <").append(tmodel)
       .append("> where $o <rdf:type> $t) from <")
       .append(modelUri).append("> where ")
       .append("(($s $p $o and <").append(sub).append("> <").append(pred).append("> $s) minus ")
       .append("($s <rdf:type> $o and <").append(sub).append("> <").append(pred).append("> $s)) ");

    if (m.isAssociation())
      applyObjectFilters(qry, sf.getClassMetadata(m.getAssociatedEntity()), "$o", filters, sf);
    qry.append(";");

    return execCollectionsQry(qry.toString(), isc, types);
  }

  private List<String> execCollectionsQry(String qry, ItqlStoreConnection isc,
      Map<String, Set<String>> types) throws OtmException {
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

    List<String> res = new ArrayList();

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
          updateTypeMap(qa.getSubQueryResults(3), assoc, types);
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
          updateTypeMap(qa.getSubQueryResults(2), assoc, types);
        }
      }
      // XXX: Type map will contain rdf:Bag, rdf:List, rdf:Seq, rdf:Alt.
      // XXX: These are left over from the rdf:type look-ahead in the main query.
      // XXX: Ideally we should clean it up. But it does not affect the
      // XXX: mostSepcificSubclass selection.

      qa.close();
    } catch (AnswerException ae) {
      throw new OtmException("Error parsing answer", ae);
    }
    return res;
  }

  private void updateTypeMap(Answer qa, String assoc, Map<String, Set<String>> types)
      throws AnswerException {
    qa.beforeFirst();
    while (qa.next()) {
      Set<String> v = types.get(assoc);
      if (v == null)
        types.put(assoc, v = new HashSet<String>());
      v.add(qa.getString("t"));
    }
    qa.close();
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

  public Results doQuery(GenericQueryImpl query, Collection<Filter> filters, Connection con)
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

  private static String getModelUri(String modelId, ItqlStoreConnection isc) throws OtmException {
    ModelConfig mc = isc.getSession().getSessionFactory().getModel(modelId);
    if (mc == null) // Happens if using a Class but the model was not added
      throw new OtmException("Unable to find model '" + modelId + "'");
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

  // TODO: move this method into session so it can be part of a tx
  public void createModel(ModelConfig conf) throws OtmException {
    String type = (conf.getType() == null) ? "mulgara:Model" : conf.getType().toString();
    runSingleCmd("create <" + conf.getUri() + "> <" + type + ">;",
                 "Failed to create model <" + conf.getUri() + ">");
  }

  // TODO: move this method into session so it can be part of a tx
  public void dropModel(ModelConfig conf) throws OtmException {
    runSingleCmd("drop <" + conf.getUri() + ">;", "Failed to drop model <" + conf.getUri() + ">");
  }

  private void runSingleCmd(String itql, String errMsg) throws OtmException {
    /* can't use cached clients because mulgara disallows using a session with both internal
     * (begin/commit) and external (getXAResource) transactions. When we move the model operations
     * into session and hence make them part of transaction then we won't have this problem.
     */
    ItqlClient con = getItqlClient(null, false);
    try {
      con.doUpdate(itql);
    } catch (Exception e) {
      throw new OtmException(errMsg, e);
    } finally {
      con.close();
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
