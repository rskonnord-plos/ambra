/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

header
{
/*
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.query;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.Junction;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.Parameter;
import org.topazproject.otm.criterion.Restrictions;

import antlr.RecognitionException;
import antlr.collections.AST;
}

/**
 * This is an AST transformer for OQL that generates a Criteria definition for this query. Of
 * course this only works within certain limitations, such as only a single entry in the from
 * clause.
 *
 * @author Ronald Tschalär 
 */
class CriteriaGenerator extends TreeParser("OqlTreeParser");

options {
    importVocab = Query;
}

{
    private Session  sess;
    private Criteria crit;
    private String   topVar;
    private Map<String, Criteria> critMap = new HashMap<String, Criteria>();

    /** 
     * Create a new translator instance.
     *
     * @param sessionFactory the session-factory to use to look up class-metatdata, models, etc
     */
    public CriteriaGenerator(Session session) {
      this();
      sess = session;
    }

    /**
     * @return the generated criteria
     */
    public Criteria getCriteria() {
      return crit;
    }

    private void buildCriteria(AST root) throws RecognitionException {
      assert root.getFirstChild().getNextSibling().getType() == WHERE;
      root = root.getFirstChild().getNextSibling().getFirstChild();

      if (root.getType() == AND)
        buildCriteria(root, new HashMap<String, AST>());
      else
        processCriterion(root, null, null, new HashMap<String, AST>());
    }

    private void buildCriteria(AST root, Map<String, AST> vars)
        throws RecognitionException {
      for (AST n = root.getFirstChild(); n != null; n = n.getNextSibling()) {
        switch (n.getType()) {
          case AND:
            buildCriteria(n, vars);
            break;

          case OR:
            processCriterion(n, null, null, vars);
            break;

          case ASGN:
            processAssign(n, vars);
            break;

          case EQ:
          case NE:
            processEquality(n, null, null, vars);
            break;

          case FUNC:
            processFunc(n, null, null, vars);
            break;
        }
      }
    }

    /** 
     * @param node   the node to process
     * @param parent the parent Criteria or Criterion object to which to add the parsed criterion;
     *               may be null to indicate unknown as of yet
     * @param pVar   the variable name corresponding to the parent criteria; will be null if
     *               <var>parent</var> is null
     * @param vars   the current map of variables pointing to constants
     * @return the variable name indicating the parent criteria
     */
    private String processCriterion(AST node, Object parent, String pVar, Map<String, AST> vars)
        throws RecognitionException {
      Criterion c = null;
      AST       n;

      switch (node.getType()) {
        case AND:
          c = Restrictions.conjunction();
          for (n = node.getFirstChild(); n != null; n = n.getNextSibling())
            pVar = processCriterion(n, c, pVar, vars);
          break;

        case OR:
          c = Restrictions.disjunction();
          for (n = node.getFirstChild(); n != null; n = n.getNextSibling())
            pVar = processCriterion(n, c, pVar, new HashMap<String, AST>(vars));
          break;

        case ASGN:
          processAssign(node, vars);
          break;

        case EQ:
        case NE:
          pVar = processEquality(node, parent, pVar, vars);
          break;

        case FUNC:
          pVar = processFunc(node, parent, pVar, vars);
          break;
      }

      if (parent == null)
        parent = critMap.get(pVar);
      if (c != null)
        addCriterion(parent, c);

      return pVar;
    }

    private void processAssign(AST node, Map<String, AST> vars) {
      AST v = node.getFirstChild();
      AST n = v.getNextSibling();
      if (n.getType() != REF)
        vars.put(v.getText(), n);
    }

    private void addCriterion(Object parent, Criterion c) throws RecognitionException {
      if (parent instanceof Criteria)
        ((Criteria) parent).add(c);
      else if (parent instanceof Junction)
        ((Junction) parent).add(c);
      else
        throw new RecognitionException("Internal error: unexpected parent '" +
                                       parent.getClass().getName() + "'");
    }

    private String processEquality(AST node, Object parent, String pVar, Map<String, AST> vars)
        throws RecognitionException {
      // get the two factors
      AST l = node.getFirstChild();
      AST r = l.getNextSibling();
      l = replaceVars(l, node, vars);
      r = replaceVars(r, node, vars);

      // normalize so ref is on the left
      if (l.getType() != REF) {
        AST tmp = l; l = r; r = tmp;
      }

      // validate and get criteria to attach to
      if (l.getType() != REF || (r.getType() != QSTRING && r.getType() != URIREF &&
          r.getType() != PARAM))
        throw new RecognitionException("equality comparisons must be between a reference and a " +
                                       "constant: " + node.toStringTree());

      if (parent == null) {
        pVar   = l.getFirstChild().getText();
        parent = critMap.get(pVar);
        if (parent == null)
          throw new RecognitionException("unknown variable '" + pVar + "': " + node.toStringTree());

        parent = makeChildren(l, (Criteria) parent, false);
      } else {
        String p = checkSingleLevelRef(l);
        if (pVar != null && !p.equals(pVar))
          throw new RecognitionException("criterion must all have same parent; found '" + pVar +
                                         "' and '" + p + "' in " + node.toStringTree());
        pVar = p;
      }

      // generate criterion
      String f = getLastChild(l).getText();
      Object val = toValueObject(r);
      Criterion c = (node.getType() == EQ) ? Restrictions.eq(f, val) : Restrictions.ne(f, val);
      addCriterion(parent, c);

      return pVar;
    }

    private String processFunc(AST node, Object parent, String pVar, Map<String, AST> vars)
        throws RecognitionException {
      // process the factors
      AST n = node.getFirstChild();
      String op = n.getText();
      if (n.getNextSibling() != null && n.getNextSibling().getType() == COLON) {
        n = n.getNextSibling().getNextSibling();
        op += ":" + n.getText();
      }

      AST args = n.getNextSibling();
      for (n = args; n != null; n = n.getNextSibling())
        n = replaceVars(n, node, vars);

      // normalize so ref is first
      if (op.equals("lt") || op.equals("le") || op.equals("gt") || op.equals("ge")) {
        if (args != null && args.getNextSibling() != null && args.getType() != REF) {
          AST tmp = args.getNextSibling();
          args.setNextSibling(tmp.getNextSibling());
          tmp.setNextSibling(args);
          args = tmp;
        }
      }

      // validate and get criteria to attach to
      if (op.equals("lt") || op.equals("le") || op.equals("gt") || op.equals("ge")) {
        AST a2 = (args != null) ? args.getNextSibling() : null;
        if (args == null || a2 == null || args.getType() != REF ||
            (a2.getType() != QSTRING && a2.getType() != URIREF && a2.getType() != PARAM))
          throw new RecognitionException("comparisons must be between a reference and a " +
                                         "constant: " + node.toStringTree());
      }

      boolean haveMulti = false;
      AST     ref       = null;
      for (n = args; n != null; n = n.getNextSibling()) {
        if (n.getType() != REF)
          continue;

        ref = n;
        AST top = n.getFirstChild();
        AST f   = top.getNextSibling();

        if (f == null)
          throw new RecognitionException("need at least one level of dereferencing: " +
                                         node.toStringTree());
        if (f.getNextSibling() != null) {
          if (haveMulti)
            throw new RecognitionException("can't handle multiple multi-level dereferencings: " +
                                           node.toStringTree());
          haveMulti = true;
        }

        if (pVar == null)
          pVar = top.getText();
        else if (!pVar.equals(top.getText()))
          throw new RecognitionException("criterion must all have same parent; found '" + pVar +
                                         "' and '" + top.getText() + "' in " + node.toStringTree());
      }
      if (ref == null)
        throw new RecognitionException("no dereference found: " + node.toStringTree());

      if (parent == null) {
        parent = critMap.get(pVar);
        if (parent == null)
          throw new RecognitionException("unknown variable '" + pVar + "': " + node.toStringTree());

        if (haveMulti)
          parent = makeChildren(ref, (Criteria) parent, false);
      } else {
        if (haveMulti)
          throw new RecognitionException("Only single level dereference supported by criteria at " +
                                         "this point: " + node.toStringTree());
      }

      // generate criterion
      List<Object> argList = new ArrayList<Object>();
      for (n = args; n != null; n = n.getNextSibling())
        argList.add(toValueObject(n));
      Criterion c = Restrictions.func(op, argList.toArray());
      addCriterion(parent, c);

      return pVar;
    }

    /** Constant substitution */
    private AST replaceVars(AST node, AST parent, Map<String, AST> vars)
        throws RecognitionException {
      if (node.getType() != REF)
        return node;

      AST repl = vars.get(node.getFirstChild().getText());
      if (repl == null)
        return node;

      if (node.getFirstChild().getNextSibling() != null)
        throw new RecognitionException("Can't dereference a constant: " + node.toStringTree());

      repl = astFactory.dupList(repl);
      repl.setNextSibling(node.getNextSibling());

      if (parent.getFirstChild() == node)
        parent.setFirstChild(repl);
      else
        getPreceedingChild(parent, node).setNextSibling(repl);

      return repl;
    }

    private String checkSingleLevelRef(AST ref) throws RecognitionException {
      AST n = ref.getFirstChild().getNextSibling();
      if (n == null || n.getNextSibling() != null)
        throw new RecognitionException("Only single level reference supported by criteria at " +
                                       "this point: " + ref.toStringTree());
      return ref.getFirstChild().getText();
    }

    private Criteria makeChildren(AST ref, Criteria p, boolean incLast)
        throws RecognitionException {
      AST n = ref.getFirstChild();
      try {
        while ((n = n.getNextSibling()) != null && (incLast || n.getNextSibling() != null))
          p = p.createCriteria(n.getText());
      } catch (OtmException oe) {
        throw (RecognitionException)
            new RecognitionException("no field '" + n.getText() + "' in " + p.getClassMetadata() +
                                     ": " + ref.toStringTree()).initCause(oe);
      }
      return p;
    }

    private Object toValueObject(AST v) throws RecognitionException {
      String t = v.getText();

      if (v.getType() == URIREF) {
        try {
          return new URI(t.substring(1, t.length() - 1));
        } catch (URISyntaxException use) {
          throw (RecognitionException)
              new RecognitionException("invalid uri '" + t.substring(1, t.length() - 1) + "'").
                  initCause(use);
        }
      }

      if (v.getType() == QSTRING) {
        if (v.getNextSibling() != null)
          reportWarning("language tags and datatypes on literals are not supported by criteria - " +
                        "dropping them (" + v.toStringTree() + ")");
        return t.substring(1, t.length() - 1).replaceAll("\\\\'", "'").
                 replaceAll("\\\\\\\\", "\\\\");
      }

      if (v.getType() == PARAM) {
        return new Parameter(v.getFirstChild().getText());
      }

      if (v.getType() == REF) {
        return getLastChild(v).getText();
      }

      throw new RecognitionException("Internal error: unexpected value node '" + v.getType() + "'");
    }

    private static AST getLastChild(AST n) {
      return getPreceedingChild(n, null);
    }

    private static AST getPreceedingChild(AST parent, AST child) {
      AST n;
      for (n = parent.getFirstChild(); n.getNextSibling() != child; n = n.getNextSibling())
        ;
      return n;
    }

    private void createRootCriteria(AST cls, AST var) throws RecognitionException {
      ClassMetadata cm = sess.getSessionFactory().getClassMetadata(cls.getText());
      if (cm == null)
        throw new RecognitionException("No metadata found for class '" + cls.getText() + "'");

      crit   = new Criteria(sess, null, null, false, cm, null);
      topVar = var.getText();

      critMap.put(topVar, crit);
    }

    private void registerVar(AST var, AST factor) throws RecognitionException {
      if (factor.getType() != REF)
        return;

      String v = var.getText();
      if (critMap.containsKey(v))
        throw new RecognitionException("multiple assignments of same variable is not supported " +
                                       "by criteria: var = '" + v + "'");

      Criteria c = critMap.get(factor.getFirstChild().getText());
      critMap.put(v, makeChildren(factor, c, true));
    }
}


query
    : #(SELECT #(FROM fclause) #(WHERE wclause) #(PROJ sclause) (oclause)? (lclause)? (tclause)?) {
        buildCriteria(#query);
      }
    ;


fclause
    : #(COMMA fclause fclause) {
        throw new RecognitionException("criteria supports only a single class in from clause");
      }
    | cls:ID var:ID  { createRootCriteria(#cls, #var); }
    ;


sclause
    : #(COMMA sclause sclause) {
        throw new RecognitionException("criteria supports only a single result in select clause");
      }
    | (ID)? pexpr
    ;

pexpr
    : (REF) => #(REF v:ID (x:ID)?) {
        if (#x != null || !#v.getText().equals(topVar))
          throw new RecognitionException("criteria doesn't support projections");
      }
    | #(SUBQ query) { throw new RecognitionException("criteria doesn't support subqueries"); }
    | factor { throw new RecognitionException("criteria doesn't support projections"); }
    ;


wclause
    : (expr)?
    ;

expr
    : #(AND (expr)+)
    | #(OR  (expr)+)
    | #(ASGN v:ID f:factor) { registerVar(#v, #f); }
    | #(EQ factor factor)
    | #(NE factor factor)
    | fcall
    ;

factor
    : constant
    | fcall
    | deref
    ;

constant
    : QSTRING ((DHAT type:URIREF) | (AT lang:ID))?
    | URIREF
    | #(PARAM ID)
    ;

fcall
    : #(FUNC ID (COLON ID)? (factor)*)
    ;

deref
    : #(REF (ID | cast) (ID | URIREF | #(EXPR ID (expr)?))* (STAR)?) {
        for (AST n = #deref.getFirstChild(); n != null; n = n.getNextSibling())
          if (n.getType() != ID)
            throw new RecognitionException("only field references supported by criteria in " +
                                           "constraint: " + #deref.toStringTree());
      }
    ;

cast
    : #(CAST factor ID) { throw new RecognitionException("criteria doesn't support casts"); }
    ;


oclause
    : #(ORDER (oitem)+)
    ;

oitem
    : var:ID (ASC|d:DESC)? { crit.addOrder(new Order(#var.getText(), #d == null)); }
    ;

lclause
    : #(LIMIT n:NUM) { crit.setMaxResults(Integer.parseInt(#n.getText())); }
    ;

tclause
    : #(OFFSET n:NUM) { crit.setFirstResult(Integer.parseInt(#n.getText())); }
    ;

