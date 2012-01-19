/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.stores;

import java.net.MalformedURLException;
import java.net.URI;
import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.AnswerSet;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Connection;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.Filter;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.criterion.itql.ComparisonCriterionBuilder;
import org.topazproject.otm.filter.AbstractFilterImpl;
import org.topazproject.otm.mapping.Mapper;
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
  private static final Map<Object, List<ItqlHelper>> conCache = new HashMap();
  private        final URI serverUri;

  /** 
   * Create a new itql-store instance. 
   * 
   * @param server  the uri of the iTQL server.
   */
  public ItqlStore(URI server) {
    serverUri = server;

    //XXX: configure these
    ComparisonCriterionBuilder cc = new ComparisonCriterionBuilder("local:///topazproject#str");
    critBuilders.put("gt", cc);
    critBuilders.put("ge", cc);
    critBuilders.put("lt", cc);
    critBuilders.put("le", cc);
  }

  public Connection openConnection() {
    return new ItqlStoreConnection(serverUri);
  }

  public void closeConnection(Connection con) {
    ((ItqlStoreConnection) con).close();
  }

  public void insert(ClassMetadata cm, String id, Object o, Transaction txn) throws OtmException {
    Map<String, List<Mapper>> mappersByModel = groupMappersByModel(cm);
    StringBuilder insert = new StringBuilder(500);

    // for every model create an insert statement
    for (String m : mappersByModel.keySet()) {
      insert.append("insert ");
      int startLen = insert.length();

      if (m.equals(cm.getModel())) {
        for (String type : cm.getTypes())
          addStmt(insert, id, Rdf.rdf + "type", type, null, true);
      }

      buildInsert(insert, mappersByModel.get(m), id, o, txn.getSession());

      if (insert.length() > startLen)
        insert.append("into <").append(getModelUri(m, txn)).append(">;");
      else
        insert.setLength(insert.length() - 7);
    }

    if (log.isDebugEnabled())
      log.debug("insert: " + insert);

    try {
      ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection();
      isc.getItqlHelper().doUpdate(insert.toString(), null);
    } catch (RemoteException re) {
      throw new OtmException("error performing update", re);
    }
  }

  private static Map<String, List<Mapper>> groupMappersByModel(ClassMetadata cm) {
    Map<String, List<Mapper>> mappersByModel = new HashMap<String, List<Mapper>>();

    if (cm.getTypes().size() > 0)
      mappersByModel.put(cm.getModel(), new ArrayList<Mapper>());

    for (Mapper p : cm.getFields()) {
      String m = (p.getModel() != null) ? p.getModel() : cm.getModel();
      List<Mapper> pList = mappersByModel.get(m);
      if (pList == null)
        mappersByModel.put(m, pList = new ArrayList<Mapper>());
      pList.add(p);
    }

    return mappersByModel;
  }


  private void buildInsert(StringBuilder buf, List<Mapper> pList, String id, Object o, Session sess)
      throws OtmException {
    int i = 0;
    for (Mapper p : pList) {
      if (!p.isEntityOwned())
        continue;
      if (p.getMapperType() == Mapper.MapperType.PREDICATE_MAP) {
        Map<String, List<String>> pMap = (Map<String, List<String>>) p.getRawValue(o, true);
        for (String k : pMap.keySet())
          for (String v : pMap.get(k))
            addStmt(buf, id, k, v, null, false); // xxx: uri or literal?
      } else if (p.getSerializer() != null)
        addStmts(buf, id, p.getUri(), (List<String>) p.get(o) , p.getDataType(), p.typeIsUri(), 
                 p.getMapperType(), "$s" + i++ + "i", p.hasInverseUri());
      else
        addStmts(buf, id, p.getUri(), sess.getIds(p.get(o)) , null,true, p.getMapperType(), 
                 "$s" + i++ + "i", p.hasInverseUri());
    }
  }

  private static void addStmts(StringBuilder buf, String subj, String pred, List<String> objs, 
      String dt, boolean objIsUri, Mapper.MapperType mt, String prefix, boolean inverse) {
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
      String rdfType = (Mapper.MapperType.RDFBAG == mt) ? "<rdf:Bag> " :
                       ((Mapper.MapperType.RDFSEQ == mt) ? "<rdf:Seq> " : "<rdf:Alt> ");
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
      buf.append("> '").append(ItqlHelper.escapeLiteral(obj));
      if (dt != null)
        buf.append("'^^<").append(dt).append("> ");
      else
        buf.append("' ");
    }
  }

  public void delete(ClassMetadata cm, String id, Transaction txn) throws OtmException {
    Map<String, List<Mapper>> mappersByModel = groupMappersByModel(cm);
    StringBuilder delete = new StringBuilder(500);

    // for every model create a delete statement
    for (String m : mappersByModel.keySet()) {
      delete.append("delete ");
      buildDeleteSelect(delete, getModelUri(m, txn), mappersByModel.get(m),
                        m.equals(cm.getModel()) && cm.getTypes().size() > 0, id);
      delete.append("from <").append(getModelUri(m, txn)).append(">;");
    }

    if (log.isDebugEnabled())
      log.debug("delete: " + delete);

    try {
      ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection();
      isc.getItqlHelper().doUpdate(delete.toString(), null);
    } catch (RemoteException re) {
      throw new OtmException("error performing update: " + delete.toString(), re);
    }
  }

  private void buildDeleteSelect(StringBuilder qry, String model, List<Mapper> mappers,
                                 boolean delTypes, String id)
      throws OtmException {
    qry.append("select $s $p $o from <").append(model).append("> where ");

    // build forward statements
    boolean found = delTypes;
    boolean predicateMap = false;
    for (Mapper p : mappers) {
      if (!p.hasInverseUri() && p.isEntityOwned()) {
        if (p.getMapperType() == Mapper.MapperType.PREDICATE_MAP) {
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
      for (Mapper p : mappers) {
        if (!p.hasInverseUri() && p.isEntityOwned())
          qry.append("$p <mulgara:is> <").append(p.getUri()).append("> or ");
      }
      qry.setLength(qry.length() - 4);
      qry.append(") ) ");
    }

    // build reverse statements
    boolean needDisj = found | predicateMap;
    found = false;
    for (Mapper p : mappers) {
      if (p.hasInverseUri() && p.isEntityOwned()) {
        found = true;
        break;
      }
    }

    if (found) {
      if (needDisj)
        qry.append("or ");
      qry.append("($s $p $o and $o <mulgara:is> <").append(id).append("> and (");
      for (Mapper p : mappers) {
        if (p.hasInverseUri() && p.isEntityOwned())
          qry.append("$p <mulgara:is> <").append(p.getUri()).append("> or ");
      }
      qry.setLength(qry.length() - 4);
      qry.append(") ) ");
    }

    // build rdf:List, rdf:Bag, rdf:Seq or rdf:Alt statements
    found = false;
    for (Mapper p : mappers) {
      if ((p.getMapperType() == Mapper.MapperType.RDFLIST) ||
          (p.getMapperType() == Mapper.MapperType.RDFBAG) || 
          (p.getMapperType() == Mapper.MapperType.RDFSEQ) ||
          (p.getMapperType() == Mapper.MapperType.RDFALT)) {
        found = true;
        break;
      }
    }

    if (found) {
      qry.append("or ($s $p $o and <").append(id).append("> $x $c ")
        .append("and (trans($c <rdf:rest> $s) or $c <rdf:rest> $s or <").append(id).append("> $x $s)")
        .append(" and ($s <rdf:type> <rdf:List> or $s <rdf:type> <rdf:Bag> ")
        .append("or $s <rdf:type> <rdf:Seq> or $s <rdf:type> <rdf:Alt>) )");
    }
  }

  public Object get(ClassMetadata cm, String id, Object instance, Transaction txn,
                    List<Filter> filters, boolean filterObj) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection();
    SessionFactory      sf  = txn.getSession().getSessionFactory();

    // TODO: eager fetching

    // do query
    String get = buildGetSelect(cm, id, txn, filters, filterObj);
    if (log.isDebugEnabled())
      log.debug("get: " + get);

    String a;
    try {
      a = isc.getItqlHelper().doQuery(get, null);
    } catch (RemoteException re) {
      throw new OtmException("error performing query: " + get, re);
    }

    // parse
    Map<String, List<String>> fvalues = new HashMap<String, List<String>>();
    Map<String, List<String>> rvalues = new HashMap<String, List<String>>();
    Map<String, Set<String>> types = new HashMap<String, Set<String>>();
    try {
      AnswerSet ans = new AnswerSet(a);

      // check if we got something useful
      ans.beforeFirst();
      while (ans.next()) {
        if (!ans.isQueryResult())
          throw new OtmException("query failed: " + ans.getMessage());

        AnswerSet.QueryAnswerSet qa = ans.getQueryResults();

        // collect the results, grouping by predicate
        qa.beforeFirst();
        while (qa.next()) {
          String s = qa.getString("s");
          String p = qa.getString("p");
          String o = qa.getString("o");
          boolean inverse = (!id.equals(s) && id.equals(o));

          if (!inverse) {
            List<String> v = fvalues.get(p);
            if (v == null)
              fvalues.put(p, v = new ArrayList<String>());
            v.add(o);
          } else {
            List<String> v = rvalues.get(p);
            if (v == null)
              rvalues.put(p, v = new ArrayList<String>());
            v.add(s);
          }

          updateTypeMap(qa.getSubQueryResults(3), inverse ? s : o, types);
        }
      }
    } catch (AnswerException ae) {
      throw new OtmException("Error parsing answer", ae);
    }

    if (fvalues.size() == 0 && rvalues.size() == 0)
      return null;

    // figure out class to instantiate
    Class clazz = cm.getSourceClass();
    if (fvalues.get(Rdf.rdf + "type") == null) {
      if (cm.getType() != null)
        return null;
    } else {
      clazz = sf.mostSpecificSubClass(clazz, fvalues.get(Rdf.rdf + "type"));
      if (clazz == null)
        return null;
      cm    = sf.getClassMetadata(clazz);

      fvalues.get(Rdf.rdf + "type").removeAll(cm.getTypes());
    }

    if (cm.getIdField() == null)
      throw new OtmException("No id field in class '" + clazz 
        + "' and therefore cannot be instantiated.");

    // pre-process for special constructs (rdf:List, rdf:bag, rdf:Seq, rdf:Alt)
    String modelUri = getModelUri(cm.getModel(), txn);
    for (String p : fvalues.keySet()) {
      Mapper m = cm.getMapperByUri(p, false, null);
      if (m == null)
        continue;
      String mUri = (m.getModel() != null) ? getModelUri(m.getModel(), txn) : modelUri;
      if (Mapper.MapperType.RDFLIST == m.getMapperType())
        fvalues.put(p, getRdfList(id, p, mUri, txn, types, m, sf, filters));
      else if (Mapper.MapperType.RDFBAG == m.getMapperType() ||
          Mapper.MapperType.RDFSEQ == m.getMapperType() || 
          Mapper.MapperType.RDFALT == m.getMapperType())
        fvalues.put(p, getRdfBag(id, p, mUri, txn, types, m, sf, filters));
    }

    return instantiate(txn.getSession(), instance, cm, id, fvalues, rvalues, types);
  }

  private String buildGetSelect(ClassMetadata cm, String id, Transaction txn, List<Filter> filters,
                                boolean filterObj) throws OtmException {
    SessionFactory     sf     = txn.getSession().getSessionFactory();
    Set<ClassMetadata> fCls   = listFieldClasses(cm, sf);
    String             models = getModelsExpr(Collections.singleton(cm), txn);
    String             tmdls  = getModelsExpr(fCls.size() > 0 ? fCls : Collections.singleton(cm),
                                              txn);
    List<Mapper>       assoc  = listAssociations(cm, sf);

    StringBuilder qry = new StringBuilder(500);

    qry.append("select $s $p $o subquery (select $t from ").append(tmdls).append(" where ");
    qry.append("$o <rdf:type> $t) ");
    qry.append("from ").append(models).append(" where ");
    qry.append("$s $p $o and $s <mulgara:is> <").append(id).append(">");
    if (filterObj)
      applyObjectFilters(qry, cm.getSourceClass(), "$s", filters, sf);
    applyFieldFilters(qry, assoc, true, id, filters, sf);

    qry.append("; select $s $p $o subquery (select $t from ").append(models).append(" where ");
    qry.append("$s <rdf:type> $t) ");
    qry.append("from ").append(models).append(" where ");
    qry.append("$s $p $o and $o <mulgara:is> <").append(id).append(">");
    if (filterObj)
      applyObjectFilters(qry, cm.getSourceClass(), "$o", filters, sf);
    applyFieldFilters(qry, assoc, false, id, filters, sf);
    qry.append(";");

    return qry.toString();
  }

  private void applyObjectFilters(StringBuilder qry, Class cls, String var, List<Filter> filters,
                                  SessionFactory sf) throws OtmException {
    // avoid work if possible
    if (filters == null || filters.size() == 0)
      return;

    // find applicable filters
    filters = new ArrayList<Filter>(filters);
    for (Iterator<Filter> iter = filters.iterator(); iter.hasNext(); ) {
      Filter f = iter.next();
      ClassMetadata fcm = sf.getClassMetadata(f.getFilterDefinition().getFilteredClass());
      if (!fcm.getSourceClass().isAssignableFrom(cls))
        iter.remove();
    }

    if (filters.size() == 0)
      return;

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
  }

  private void applyFieldFilters(StringBuilder qry, List<Mapper> assoc, boolean fwd, String id,
                                 List<Filter> filters, SessionFactory sf)
        throws OtmException {
    // avoid work if possible
    if (filters == null || filters.size() == 0 || assoc.size() == 0)
      return;

    // build predicate->filter map
    Map<String, Set<Filter>> applicFilters = new HashMap<String, Set<Filter>>();
    for (Mapper m : assoc) {
      Class mc = m.getComponentType();

      for (Filter f : filters) {
        ClassMetadata cm = sf.getClassMetadata(f.getFilterDefinition().getFilteredClass());
        if (cm == null)
          continue;       // bug???

        Class fc = cm.getSourceClass();
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

  private static String getModelsExpr(Set<ClassMetadata> cmList, Transaction txn)
      throws OtmException {
    Set<String> mList = new HashSet<String>();
    for (ClassMetadata cm : cmList) {
      mList.add(getModelUri(cm.getModel(), txn));
      for (Mapper p : cm.getFields()) {
        if (p.getModel() != null)
          mList.add(getModelUri(p.getModel(), txn));
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

    for (Mapper p : cm.getFields()) {
      ClassMetadata c = sf.getClassMetadata(p.getComponentType());
      if (c != null)
        clss.add(c);
    }

    return clss;
  }

  private static List<Mapper> listAssociations(ClassMetadata cm, SessionFactory sf) {
    List<Mapper> mappers = new ArrayList<Mapper>();

    for (Mapper p : cm.getFields()) {
      if (p.getSerializer() == null && p.getMapperType() == Mapper.MapperType.PREDICATE)
        mappers.add(p);
    }

    return mappers;
  }

  private List<String> getRdfList(String sub, String pred, String modelUri, Transaction txn, 
                                  Map<String, Set<String>> types, Mapper m, SessionFactory sf,
                                  List<Filter> filters)
        throws OtmException {
    StringBuilder qry = new StringBuilder(500);
    qry.append("select $o $s $n subquery (select $t from <").append(modelUri)
       .append("> where $o <rdf:type> $t) from <").append(modelUri).append("> where ")
       .append("(trans($c <rdf:rest> $s) or $c <rdf:rest> $s or <")
       .append(sub).append("> <").append(pred).append("> $s) and <")
       .append(sub).append("> <").append(pred).append("> $c and $s <rdf:first> $o")
       .append(" and $s <rdf:rest> $n");
    if (m.getSerializer() == null)
      applyObjectFilters(qry, m.getComponentType(), "$o", filters, sf);
    qry.append(";");

    return execCollectionsQry(qry.toString(), txn, types);
  }

  private List<String> getRdfBag(String sub, String pred, String modelUri, Transaction txn,
                                 Map<String, Set<String>> types, Mapper m, SessionFactory sf,
                                 List<Filter> filters)
        throws OtmException {
    StringBuilder qry = new StringBuilder(500);
    qry.append("select $o $p subquery (select $t from <").append(modelUri)
       .append("> where $o <rdf:type> $t) from <")
       .append(modelUri).append("> where ")
       .append("($s $p $o minus $s <rdf:type> $o) and <")
       .append(sub).append("> <").append(pred).append("> $s");
    if (m.getSerializer() == null)
      applyObjectFilters(qry, m.getComponentType(), "$o", filters, sf);
    qry.append(";");

    return execCollectionsQry(qry.toString(), txn, types);
  }

  private List<String> execCollectionsQry(String qry, Transaction txn,
      Map<String, Set<String>> types) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection();
    SessionFactory      sf  = txn.getSession().getSessionFactory();

    log.debug("rdf:List/rdf:Bag query : " + qry);
    String a;
    try {
      a = isc.getItqlHelper().doQuery(qry, null);
    } catch (RemoteException re) {
      throw new OtmException("error performing rdf:List/rdf:Bag query", re);
    }

    List<String> res = new ArrayList();

    try {
      AnswerSet ans = new AnswerSet(a);

      // check if we got something useful
      ans.beforeFirst();
      if (!ans.next())
        return res;
      if (!ans.isQueryResult())
        throw new OtmException("query failed: " + ans.getMessage());

      AnswerSet.QueryAnswerSet qa = ans.getQueryResults();

      // collect the results,
      qa.beforeFirst();
      boolean isRdfList = qa.indexOf("n") != -1;
      if (isRdfList) {
        Map<String, String> fwd  = new HashMap<String, String>();
        Map<String, String> rev  = new HashMap<String, String>();
        Map<String, String> objs = new HashMap<String, String>();
        while (qa.next()) {
          String assoc = qa.getString("o");
          String node  = qa.isBlankNode(1) ? qa.getBlankNode(1) : qa.getString(1);
          String next  = qa.isBlankNode(2) ? qa.getBlankNode(2) : qa.getString(2);
          objs.put(node, assoc);
          fwd.put(node, next);
          rev.put(next, node);
          updateTypeMap(qa.getSubQueryResults(3), assoc, types);
        }
        log.debug("forward list: " + fwd);
        log.debug("reverse list: " + rev);
        log.debug("object  list: " + objs);

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

    } catch (AnswerException ae) {
      throw new OtmException("Error parsing answer", ae);
    }
    return res;
  }

  private void updateTypeMap(AnswerSet.QueryAnswerSet sqa, String assoc, 
      Map<String, Set<String>> types) throws AnswerException {
    sqa.beforeFirst();
    while (sqa.next()) {
      Set<String> v = types.get(assoc);
      if (v == null)
        types.put(assoc, v = new HashSet<String>());
      v.add(sqa.getString("t"));
    }
  }

  public List list(Criteria criteria, Transaction txn) throws OtmException {
    ItqlCriteria ic = new ItqlCriteria(criteria);
    String qry = ic.buildUserQuery();
    log.debug("list: " + qry);
    String a;
    try {
      ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection();
      a = isc.getItqlHelper().doQuery(qry, null);
    } catch (RemoteException re) {
      throw new OtmException("error performing query: " + qry, re);
    }
    return ic.createResults(a);
  }

  public Results doQuery(GenericQueryImpl query, Collection<Filter> filters, Transaction txn)
      throws OtmException {
    ItqlQuery iq = new ItqlQuery(query, filters, txn.getSession());
    QueryInfo qi = iq.parseItqlQuery();

    ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection();
    String a;
    try {
      a = isc.getItqlHelper().doQuery(qi.getQuery(), null);
    } catch (RemoteException re) {
      throw new QueryException("error performing query '" + query + "'", re);
    }

    return new ItqlResults(a, qi, iq.getWarnings().toArray(new String[0]), txn.getSession());
  }

  public Results doNativeQuery(String query, Transaction txn) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection();
    String a;
    try {
      a = isc.getItqlHelper().doQuery(query, null);
    } catch (RemoteException re) {
      throw new QueryException("error performing query '" + query + "'", re);
    }

    return new ItqlNativeResults(a);
  }

  private static String getModelUri(String modelId, Transaction txn) throws OtmException {
    ModelConfig mc = txn.getSession().getSessionFactory().getModel(modelId);
    if (mc == null) // Happens if using a Class but the model was not added
      throw new OtmException("Unable to find model '" + modelId + "'");
    return mc.getUri().toString();
  }

  private static ItqlHelper getItqlHelper(URI serverUri) throws OtmException {
    synchronized (conCache) {
      List<ItqlHelper> list = conCache.get(serverUri);
      if (list != null && list.size() > 0)
        return list.remove(list.size() - 1);
      try {
        return new ItqlHelper(serverUri);
      } catch (ServiceException se) {
        throw new OtmException("Error talking to '" + serverUri + "'", se);
      } catch (RemoteException re) {
        throw new OtmException("Error talking to '" + serverUri + "'", re);
      } catch (MalformedURLException mue) {
        throw new OtmException("Invalid server URI '" + serverUri + "'", mue);
      }
    }
  }

  private static void returnItqlHelper(URI serverUri, ItqlHelper itql) {
    synchronized (conCache) {
      List<ItqlHelper> list = conCache.get(serverUri);
      if (list == null)
        conCache.put(serverUri, list = new ArrayList<ItqlHelper>());
      list.add(itql);
    }
  }

  public void createModel(ModelConfig conf) throws OtmException {
    ItqlHelper itql = ItqlStore.getItqlHelper(serverUri);
    try {
      String type = (conf.getType() == null) ? "mulgara:Model" : conf.getType().toString();
      itql.doUpdate("create <" + conf.getUri() + "> <" + type + ">;", null);
      returnItqlHelper(serverUri, itql);
    } catch (Exception e) {
      throw new OtmException("Failed to create model <" + conf.getUri() + ">", e);
    }finally {
      try {
        //closeConnection(con);
      } catch (Throwable t) {
        log.warn("Close connection failed", t);
      }
    }
  }

  public void dropModel(ModelConfig conf) throws OtmException {
    ItqlHelper itql = ItqlStore.getItqlHelper(serverUri);
    try {
      itql.doUpdate("drop <" + conf.getUri() + ">;", null);
      returnItqlHelper(serverUri, itql);
    } catch (Exception e) {
      throw new OtmException("Failed to drop model <" + conf.getUri() + ">", e);
    } finally {
      try {
      } catch (Throwable t) {
        log.warn("Close connection failed", t);
      }
    }
  }

  public static class ItqlStoreConnection implements Connection {
    private final URI  serverUri;
    private ItqlHelper itql;

    public ItqlStoreConnection(URI serverUri) {
      this.serverUri = serverUri;
    }

    public ItqlHelper getItqlHelper() {
      return itql;
    }

    public void beginTransaction() throws OtmException {
      if (itql != null) {
        log.warn("Starting a transaction while one is already active - rolling back old one",
                 new Throwable());
        abort(false);
      }

      itql = ItqlStore.getItqlHelper(serverUri);
      try {
        itql.beginTxn("");
      } catch (RemoteException re) {
        throw new OtmException("error starting transaction", re);
      }
    }

    public void commit() throws OtmException {
      if (itql == null) {
        log.warn("Called commit but no transaction is active", new Throwable());
        return;
      }

      try {
        itql.commitTxn("");
      } catch (RemoteException re) {
        throw new OtmException("error committing transaction", re);
      }
      ItqlStore.returnItqlHelper(serverUri, itql);
      itql = null;
    }

    public void rollback() throws OtmException {
      if (itql == null) {
        log.warn("Called rollback but no transaction is active", new Throwable());
        return;
      }

      abort(true);
    }

    private void abort(boolean throwEx) throws OtmException {
      try {
        itql.rollbackTxn("");
        ItqlStore.returnItqlHelper(serverUri, itql);
        itql = null;
      } catch (RemoteException re) {
        if (throwEx)
          throw new OtmException("error rolling back transaction", re);
        else
          log.warn("Error during rollback", re);
      } finally {
        if (itql != null) {
          try {
            itql.close();
          } catch (RemoteException re) {
            log.error("Failed to close connection after rollback failure", re);
          } finally {
            itql = null;
          }
        }
      }
    }

    public void close() {
      if (itql != null) {
        log.warn("Closing connection with an active transaction - rolling back current one",
                 new Throwable());
        try {
          abort(false);
        } catch (OtmException oe) {
          throw new Error(oe);  // can't happen
        }
      }
    }
  }
}
