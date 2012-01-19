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
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.criterion.Conjunction;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.CriterionBuilder;
import org.topazproject.otm.criterion.Disjunction;
import org.topazproject.otm.criterion.Junction;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.PredicateCriterion;
import org.topazproject.otm.criterion.SubjectCriterion;
import org.topazproject.otm.criterion.itql.ComparisonCriterionBuilder;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.query.QueryException;
import org.topazproject.otm.query.QueryInfo;
import org.topazproject.otm.query.Results;

/** 
 * An iTQL based store.
 * 
 * @author Ronald Tschalär
 */
public class ItqlStore implements TripleStore {
  private static final Log log = LogFactory.getLog(ItqlStore.class);
  private static final Map<Object, List<ItqlHelper>> conCache = new HashMap();
  private        final URI serverUri;
  private Map<String, CriterionBuilder> critBuilders = new HashMap<String, CriterionBuilder>();

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
    critBuilders.put("lt", cc);
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

  public ResultObject get(ClassMetadata cm, String id, Transaction txn) throws OtmException {
    ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection();
    SessionFactory      sf  = txn.getSession().getSessionFactory();

    // TODO: eager fetching

    // do query
    StringBuilder get = new StringBuilder(500);
    buildGetSelect(get, cm, id, txn);
    get.append(";");

    log.debug("get: " + get);
    String a;
    try {
      a = isc.getItqlHelper().doQuery(get.toString(), null);
    } catch (RemoteException re) {
      throw new OtmException("error performing query: " + get.toString(), re);
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

          AnswerSet.QueryAnswerSet sqa = qa.getSubQueryResults(3);
          sqa.beforeFirst();
          while (sqa.next()) {
            String assoc = inverse ? s : o;
            Set<String> v = types.get(assoc);
            if (v == null)
              types.put(assoc, v = new HashSet<String>());
            v.add(sqa.getString("t"));
          }
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

    // pre-process for special constructs (rdf:List, rdf:bag, rdf:Seq, rdf:Alt)
    String modelUri = getModelUri(cm.getModel(), txn);
    for (String p : fvalues.keySet()) {
      Mapper m = cm.getMapperByUri(p, false, null);
      if (m == null)
        continue;
      String mUri = (m.getModel() != null) ? getModelUri(m.getModel(), txn) : modelUri;
      if (Mapper.MapperType.RDFLIST == m.getMapperType())
        fvalues.put(p, getRdfList(id, p, mUri, txn));
      else if (Mapper.MapperType.RDFBAG == m.getMapperType() ||
          Mapper.MapperType.RDFSEQ == m.getMapperType() || 
          Mapper.MapperType.RDFALT == m.getMapperType())
        fvalues.put(p, getRdfBag(id, p, mUri, txn));
    }

    // create result
    ResultObject ro;
    try {
      ro = new ResultObject(clazz.newInstance(), id);
    } catch (Exception e) {
      throw new OtmException("Failed to instantiate " + clazz, e);
    }

    cm.getIdField().set(ro.o, Collections.singletonList(id));

    // re-map values based on the rdf:type look ahead
    Map<Mapper, List<String>> mvalues = new HashMap();
    boolean inverse = false;
    for (Map<String, List<String>> values : new Map[] { fvalues, rvalues }) {
      for (String p : values.keySet()) {
        for (String o : values.get(p)) {
          Mapper m = cm.getMapperByUri(sf, p, inverse, types.get(o));
          if (m != null) {
            List<String> v = mvalues.get(m);
            if (v == null)
              mvalues.put(m, v = new ArrayList<String>());
            v.add(o);
          }
        }
      }
      inverse = true;
    }

    // now assign values to fields
    for (Mapper m : mvalues.keySet()) {
      if (m.getSerializer() != null)
        m.set(ro.o, mvalues.get(m));
      else
        ro.unresolvedAssocs.put(m, mvalues.get(m));
    }

    ro.types = types;

    boolean found = false;
    for (Mapper m : cm.getFields()) {
      if (m.getMapperType() == Mapper.MapperType.PREDICATE_MAP) {
        found = true;
        break;
      }
    }

    if (found) {
      Map<String, List<String>> map = new HashMap<String, List<String>>();
      map.putAll(fvalues);
      for (Mapper m : cm.getFields()) {
        if (m.getMapperType() == Mapper.MapperType.PREDICATE_MAP)
          continue;
        if (m.hasInverseUri())
          continue;
        map.remove(m.getUri());
      }
      for (Mapper m : cm.getFields()) {
        if (m.getMapperType() == Mapper.MapperType.PREDICATE_MAP)
          m.setRawValue(ro.o, map);
      }
    }

    // done
    return ro;
  }

  private void buildGetSelect(StringBuilder qry, ClassMetadata cm, String id, Transaction txn)
      throws OtmException {
    SessionFactory sf     = txn.getSession().getSessionFactory();
    String         models = getModelsExpr(new ClassMetadata[] { cm }, txn);
    String         tmdls  = getModelsExpr(listFieldClasses(cm, sf), txn);

    qry.append("select $s $p $o subquery (select $t from ").append(tmdls).append(" where ");
    qry.append("$o <rdf:type> $t) ");
    qry.append("from ").append(models).append(" where ");
    qry.append("$s $p $o and $s <mulgara:is> <").append(id).append(">;");

    qry.append("select $s $p $o subquery (select $t from ").append(models).append(" where ");
    qry.append("$s <rdf:type> $t) ");
    qry.append("from ").append(models).append(" where ");
    qry.append("$s $p $o and $o <mulgara:is> <").append(id).append(">");
  }

  private static String getModelsExpr(ClassMetadata[] cmList, Transaction txn) {
    Set<String> mList = new HashSet<String>();
    for (ClassMetadata cm : cmList) {
      mList.add(cm.getModel());
      for (Mapper p : cm.getFields()) {
        if (p.getModel() != null)
          mList.add(p.getModel());
      }
    }

    StringBuilder mexpr = new StringBuilder(100);
    for (String m : mList)
      mexpr.append("<").append(getModelUri(m, txn)).append("> or ");
    mexpr.setLength(mexpr.length() - 4);

    return mexpr.toString();
  }

  private static ClassMetadata[] listFieldClasses(ClassMetadata cm, SessionFactory sf) {
    Set<ClassMetadata> clss = new HashSet<ClassMetadata>();
    for (Mapper p : cm.getFields()) {
      ClassMetadata c = sf.getClassMetadata(p.getComponentType());
      if (c != null)
        clss.add(c);
    }

    if (clss.size() == 0)
      clss.add(cm);     // dummy to ensure we always have something, to simplify rest of code

    return clss.toArray(new ClassMetadata[clss.size()]);
  }

  private List<String> getRdfList(String sub, String pred, String modelUri, Transaction txn)
      throws OtmException {
    StringBuilder qry = new StringBuilder(500);
    qry.append("select $o from <").append(modelUri).append("> where ")
       .append("(trans($c <rdf:rest> $s) or $c <rdf:rest> $s or <")
       .append(sub).append("> <").append(pred).append("> $s) and <")
       .append(sub).append("> <").append(pred).append("> $c and $s <rdf:first> $o;");

    return execCollectionsQry(qry.toString(), txn);
  }

  private List<String> getRdfBag(String sub, String pred, String modelUri, Transaction txn)
      throws OtmException {
    StringBuilder qry = new StringBuilder(500);
    qry.append("select $o $p from <").append(modelUri).append("> where ")
       .append("($s $p $o minus $s <rdf:type> $o) and <")
       .append(sub).append("> <").append(pred).append("> $s;");

    return execCollectionsQry(qry.toString(), txn);
  }

  private List<String> execCollectionsQry(String qry, Transaction txn) throws OtmException {
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
      boolean sort = qa.indexOf("p") != -1;
      if (!sort) {
        while (qa.next())
          res.add(qa.getString("o"));
      } else {
        while (qa.next()) {
          String p = qa.getString("p");
          if (!p.startsWith(Rdf.rdf + "_"))
            continue; // an un-recognized predicate
          int i = Integer.decode(p.substring(Rdf.rdf.length() + 1)) - 1;
          while (i >= res.size())
            res.add(null);
          res.set(i, qa.getString("o"));
        }
      }

    } catch (AnswerException ae) {
      throw new OtmException("Error parsing answer", ae);
    }
    return res;
  }

  public List list(Criteria criteria, Transaction txn) throws OtmException {
    // do query
    String qry = buildUserQuery(criteria, txn);
    log.debug("list: " + qry);
    String a;
    try {
      ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection();
      a = isc.getItqlHelper().doQuery(qry, null);
    } catch (RemoteException re) {
      throw new OtmException("error performing query: " + qry, re);
    }

    // parse
    List results  = new ArrayList();
    ClassMetadata cm = criteria.getClassMetadata();

    try {
      AnswerSet ans = new AnswerSet(a);

      // check if we got something useful
      ans.beforeFirst();
      if (!ans.next())
        return null;
      if (!ans.isQueryResult())
        throw new OtmException("query failed: " + ans.getMessage());

      // go through the rows and build the results
      AnswerSet.QueryAnswerSet qa = ans.getQueryResults();

      qa.beforeFirst();
      while (qa.next()) {
        String s = qa.getString("s");
        Object o = txn.getSession().get(cm.getSourceClass(), s);
        if (o != null)
          results.add(o);
      }
    } catch (AnswerException ae) {
      throw new OtmException("Error parsing answer", ae);
    }

    return results;
  }

  private String buildUserQuery(Criteria criteria, Transaction txn) throws OtmException {
    StringBuilder qry = new StringBuilder(500);
    ClassMetadata cm = criteria.getClassMetadata();
    String model = getModelUri(cm.getModel(), txn);

    qry.append("select $s");
    int len = qry.length();
    buildProjections(criteria, qry, "$s");
    boolean hasOrderBy = len != qry.length();

    qry.append(" from <").append(model).append("> where ");

    len = qry.length();
    buildWhereClause(criteria, txn, qry, "$s");
    if (qry.length() == len)
      throw new OtmException("No criterion list to filter by and the class '" 
                             + cm + "' does not have an rdf:type.");

    qry.setLength(qry.length() - 4);

    if (hasOrderBy) {
      qry.append("order by ");
      List<String> orders = new ArrayList();
      buildOrderBy(criteria, orders, "$s");
      for (String o : orders)
        qry.append(o);
    }

    if (criteria.getMaxResults() > 0)
      qry.append(" limit " + criteria.getMaxResults());

    if (criteria.getFirstResult() >= 0)
      qry.append(" offset " + criteria.getFirstResult());

    qry.append(";");

    return qry.toString();
  }

  private void buildProjections(Criteria criteria, StringBuilder qry, String subject) {
    int i = 0;
    String prefix = " " + subject + "o";
    for (Order o : criteria.getOrderList())
      qry.append(prefix + i++);

    i = 0;
    for (Criteria cr : criteria.getChildren())
      buildProjections(cr, qry, subject + "c" + i++);
  }

  private void buildWhereClause(Criteria criteria, Transaction txn, StringBuilder qry,
                                String subject) throws OtmException {
    ClassMetadata cm = criteria.getClassMetadata();
    String model = getModelUri(cm.getModel(), txn);

    if (cm.getType() != null)
      qry.append(subject).append(" <rdf:type> <").append(cm.getType())
        .append("> in <").append(model).append("> and ");

    //XXX: there is problem with this auto generated where clause
    //XXX: it could potentially limit the result set returned by a trans() criteriom
    int i = 0;
    String object = subject + "o";
    for (Order o: criteria.getOrderList())
      buildPredicateWhere(cm, o.getName(), subject, object + i++, qry, model, txn);

    i = 0;
    String tmp = subject + "t";
    for (Criterion c : criteria.getCriterionList())
      qry.append(c.toItql(criteria, subject, tmp+i++)).append(" and ");

    i = 0;
    for (Criteria cr : criteria.getChildren()) {
      String child = subject + "c" + i++;
      buildPredicateWhere(cm, cr.getMapping().getName(), subject, child, qry, model, txn);
      buildWhereClause(cr, txn, qry, child);
    }
  }

  private void buildPredicateWhere(ClassMetadata cm, String name, String subject, String object, 
                        StringBuilder qry, String model, Transaction txn) throws OtmException {
      Mapper m = cm.getMapperByName(name);
      if (m == null)
        throw new OtmException("No field with the name '" + name + "' in " + cm);

      String mUri = (m.getModel() != null) ? getModelUri(m.getModel(), txn) : model;

     if (m.hasInverseUri()) {
        String tmp = object;
        object = subject;
        subject = tmp;
      }

      qry.append(subject).append(" <").append(m.getUri()).append("> ").append(object)
            .append(" in <").append(mUri).append("> and ");
  }

  private void buildOrderBy(Criteria criteria, List<String> orders, String subject) {
    int i = 0;
    String prefix = subject + "o";
    for (Order o : criteria.getOrderList()) {
      int pos = criteria.getOrderPosition(o);
      while (pos >= orders.size())
        orders.add("");
      orders.set(pos, prefix + i++ + (o.isAscending() ? " asc " : " desc "));
    }

    i = 0;
    for (Criteria cr : criteria.getChildren())
      buildOrderBy(cr, orders, subject + "c" + i++);
  }

  public Results doQuery(String query, Transaction txn) throws OtmException {
    ItqlQuery iq = new ItqlQuery();
    QueryInfo qi = iq.parseItqlQuery(txn.getSession(), query);

    ItqlStoreConnection isc = (ItqlStoreConnection) txn.getConnection();
    String a;
    try {
      a = isc.getItqlHelper().doQuery(qi.getQuery(), null);
    } catch (RemoteException re) {
      throw new QueryException("error performing query '" + query + "'", re);
    }

    return new ItqlResults(a, qi, iq.getWarnings().toArray(new String[0]), txn.getSession());
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

  public CriterionBuilder getCriterionBuilder(String func)
                                       throws OtmException {
    return critBuilders.get(func);
  }

  public void setCriterionBuilder(String func, CriterionBuilder builder)
                           throws OtmException {
    critBuilders.put(func, builder);
  }

  private static class ItqlStoreConnection implements Connection {
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
        abort();
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
        throw new OtmException("error starting transaction", re);
      }
      ItqlStore.returnItqlHelper(serverUri, itql);
      itql = null;
    }

    public void rollback() throws OtmException {
      if (itql == null) {
        log.warn("Called rollback but no transaction is active", new Throwable());
        return;
      }

      try {
        itql.rollbackTxn("");
      } catch (RemoteException re) {
        throw new OtmException("error starting transaction", re);
      }
      ItqlStore.returnItqlHelper(serverUri, itql);
      itql = null;
    }

    private void abort() {
      try {
        itql.rollbackTxn("");
        ItqlStore.returnItqlHelper(serverUri, itql);
      } catch (Exception e) {
        log.warn("Error during rollback", e);
      } finally {
        itql = null;
      }
    }

    public void close() {
      if (itql != null) {
        log.warn("Closing connection with an active transaction - rolling back current one",
                 new Throwable());
        abort();
      }
    }
  }
}
