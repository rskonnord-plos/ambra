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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import antlr.collections.AST;

import org.topazproject.otm.ClassMetadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
}

/**
 * This tries to simplify the where clauses by expanding the &lt;mulgara:is&gt; and
 * &lt;mulgara:equals&gt; constraints. Most of these are unnecessary, but are generated
 * because doing so greatly simplifies the previous step.
 *
 * @author Ronald Tschalär 
 */
class ItqlRedux extends TreeParser("OqlTreeParser");

options {
    importVocab = Constraints;
    buildAST    = true;
}

{
    private static final Log log = LogFactory.getLog(ItqlRedux.class);
    private int tidx = 0;

    /**
     * Simplifying a query works as follows. First, the where clause is simplified; then
     * subqueries in the projections are simplified, recursively.
     *
     * <p>When simplifying a where clause, variables that are bound from the "outside" (e.g.
     * variables specified in the from clause) can't be replaced; these variables form the
     * context-variables. Additionally, while processing the where clause, any time a non-AND is
     * encountered (e.g. an OR, MINUS, etc) the variables "above" and the variables in the other
     * branches are also part of the context for each branch. Furthermore, when simplifying
     * subqueries all variables from the main query (recursively up) are part of the context
     * too.</p>
     *
     * <p>Once a set of replacements has been determined, these replacements are applied to
     * the current subtree in the where clause, all variables in the projections, as well as in
     * all the projections and where clauses of subqueries (recursively).</p>
     *
     * <p>For each group of variables that are equal (and hence are being replaced with the same 
     * variable) a common type for the replacement variable is determined, and during replacement
     * the node's type is changed to this common type. This is needed for correct filter
     * application.</p>
     *
     * <p>Fun, isn't it?</p>
     */
    private void simplifyQuery(AST select, Set<String> ctxtVars) {
      // #(SELECT #(FROM fclause) #(WHERE wclause) #(PROJ sclause) (oclause)? (lclause)? (tclause)?)
      assert select.getType() == SELECT;

      AST f = select.getFirstChild();
      AST w = f.getNextSibling();
      AST p = w.getNextSibling();
      AST o = p.getNextSibling();

      assert f.getType() == FROM;
      assert w.getType() == WHERE;
      assert p.getType() == PROJ;

      AST fc = f.getFirstChild();
      AST wc = w.getFirstChild();
      AST pc = p.getFirstChild();

      // 0: clone the context
      Set<String> outerCtxtVars = ctxtVars;
      ctxtVars = new HashSet<String>(ctxtVars);

      // 1: extend the context with all the from vars
      addFromVars(fc, ctxtVars);

      // 2: find all replaceable vars and where clauses in subqueries, recursively
      List<AST> vars = new ArrayList<AST>();
      List<AST> wcls = new ArrayList<AST>();
      findVarsAndWClausesInProj(pc, vars, wcls);
      if (o != null && o.getType() == ORDER)
        findVarsInOrder(o.getFirstChild(), vars);

      // 3: simplify the where clause
      simplify(wc, ctxtVars, vars, wcls);

      // 4: ensure all select variables are constrained
      for (String var : findUnconstrainedVars(p, w, outerCtxtVars)) {
        String pVar = "t$" + tidx++;
        String oVar = "t$" + tidx++;
        wc.addChild(#([TRIPLE, "triple"], createVar(var), createVar(pVar), createVar(oVar)));
      }

      // 5: clean out constants from order
      if (o != null && o.getType() == ORDER) {
        removeConstFromOrder(o);
        if (o.getFirstChild() == null)
          p.setNextSibling(o.getNextSibling());
      }

      // 6: simplify subqueries, recursively
      simplifySubQueries(pc, ctxtVars);
    }

    private int addFromVars(AST fclause, Set<String> ctxtVars) {
      // #(COMMA fclause fclause) | cls:ID var:ID
      if (fclause.getType() == COMMA) {
        for (AST n = fclause.getFirstChild(); n != null; n = n.getNextSibling()) {
          int skip = addFromVars(n, ctxtVars);
          while (skip-- > 0)
            n = n.getNextSibling();
        }
        return 0;
      } else {
        AST var = fclause.getNextSibling();
        assert var.getType() == ID;

        if (((OqlAST) var).isVar())
          ctxtVars.add(var.getText());

        return 1;
      }
    }

    /**
     * Finds all projection and order variables, and all where clauses, in all subqueries
     * (recursively).
     */
    private int findVarsAndWClausesInProj(AST sclause, List<AST> vars, List<AST> wcls) {
      // #(COMMA sclause sclause) | ID pexpr
      if (sclause.getType() == COMMA) {
        for (AST n = sclause.getFirstChild(); n != null; n = n.getNextSibling()) {
          int skip = findVarsAndWClausesInProj(n, vars, wcls);
          while (skip-- > 0)
            n = n.getNextSibling();
        }
        return 0;
      } else {
        // ID
        AST id = sclause;
        assert id.getType() == ID;

        // #(SUBQ query) | #(COUNT query) | (QSTRING|URIREF) | ID
        AST pexpr = id.getNextSibling();
        switch (pexpr.getType()) {
          case SUBQ:
          case COUNT:
            findQueryVarsAndWClauses(pexpr.getFirstChild(), vars, wcls);
            break;

          case ID:
            if (((OqlAST) pexpr).isVar())
              vars.add(pexpr);
            break;
        }

        return 1;
      }
    }

    private void findQueryVarsAndWClauses(AST select, List<AST> vars, List<AST> wcls) {
      // #(SELECT #(FROM fclause) #(WHERE wclause) #(PROJ sclause) (oclause)? (lclause)? (tclause)?)
      assert select.getType() == SELECT;

      AST f = select.getFirstChild();
      AST w = f.getNextSibling();
      AST p = w.getNextSibling();
      AST o = p.getNextSibling();

      assert f.getType() == FROM;
      assert w.getType() == WHERE;
      assert p.getType() == PROJ;

      wcls.add(w.getFirstChild());
      findVarsAndWClausesInProj(p.getFirstChild(), vars, wcls);
      if (o != null && o.getType() == ORDER)
        findVarsInOrder(o.getFirstChild(), vars);
    }

    private void findVarsInOrder(AST oitem, List<AST> vars) {
      // (ID (ASC|DESC)?)+
      for (; oitem != null; oitem = oitem.getNextSibling()) {
        if (oitem.getType() == ID && ((OqlAST) oitem).isVar())
          vars.add(oitem);
      }
    }

    private int simplifySubQueries(AST sclause, Set<String> ctxtVars) {
      // #(COMMA sclause sclause) | ID pexpr
      if (sclause.getType() == COMMA) {
        for (AST n = sclause.getFirstChild(); n != null; n = n.getNextSibling()) {
          int skip = simplifySubQueries(n, ctxtVars);
          while (skip-- > 0)
            n = n.getNextSibling();
        }
        return 0;
      } else {
        // #(SUBQ query) | #(COUNT query) | (QSTRING|URIREF) | ID
        AST pexpr = sclause.getNextSibling();
        switch (pexpr.getType()) {
          case SUBQ:
          case COUNT:
            simplifyQuery(pexpr.getFirstChild(), ctxtVars);
        }
        return 1;
      }
    }

    private void simplify(AST node, Set<String> ctxtVars, List<AST> extVars, List<AST> extWcls) {
      if (node.getType() == AND) {
        if (log.isTraceEnabled())
          log.trace("simplifying: " + node.toStringTree());

        // find all $a = $b
        Map<String, Set<String>> eqls  = new HashMap<String, Set<String>>();
        Map<String, String>      is    = new HashMap<String, String>();
        Map<String, ExprType>    types = new HashMap<String, ExprType>();
        findEqualities(node, eqls, is, types);

        // find replacements, transitively
        Map<String, String> repl = new HashMap<String, String>();
        findReplacements(eqls, repl, is, types, ctxtVars);

        if (log.isTraceEnabled()) {
          log.trace("replacements:  " + repl);
          log.trace("is-expansions: " + is);
        }

        // rewrite our expression tree
        if (repl.size() > 0 || is.size() > 0) {
          applyReplacements(node, null, null, repl, is, types);
          for (AST n : extWcls)
            applyReplacements(n, null, null, repl, is, types);
          for (AST var : extVars)
            applyReplacements(var, repl, is);
        }

        if (log.isTraceEnabled())
          log.trace("done simplifying: " + node.toStringTree());
      }

      // process subtrees
      simplifySubTree(node, ctxtVars, extVars, extWcls);
    }

    private void simplifySubTree(AST node, Set<String> ctxtVars, List<AST> extVars,
                                 List<AST> extWcls) {
      if (node.getType() == TRIPLE)
        ;
      else if (node.getType() == AND) {
        // equality can only be propagated up past an AND
        for (AST n = node.getFirstChild(); n != null; n = n.getNextSibling())
            simplifySubTree(n, ctxtVars, extVars, extWcls);
      } else {
        /* collect variables in each branch - variables in other branches will be part of
         * the untouchable context for any given branch.
         */
        List<Set<String>> branchVars = new ArrayList<Set<String>>();
        for (AST n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
          Set<String> vars = new HashSet<String>();
          collectVars(n, vars);
          branchVars.add(vars);
        }

        // now run through each branch with the correct context
        for (AST n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
          Set<String> locCtxtVars = new HashSet<String>(ctxtVars);
          int idx = 0;
          for (AST nn = node.getFirstChild(); nn != null; nn = nn.getNextSibling(), idx++) {
            if (nn != n)
              locCtxtVars.addAll(branchVars.get(idx));
          }

          simplify(n, locCtxtVars, extVars, extWcls);
        }
      }
    }

    private void collectVars(AST node, Set<String> vars) {
      if (node.getType() == TRIPLE) {
        OqlAST s = (OqlAST) node.getFirstChild();
        OqlAST p = (OqlAST) s.getNextSibling();
        OqlAST o = (OqlAST) p.getNextSibling();

        if (s.isVar())
          vars.add(s.getText());
        if (p.isVar())
          vars.add(p.getText());
        if (o.isVar())
          vars.add(o.getText());
      } else {
        for (AST n = node.getFirstChild(); n != null; n = n.getNextSibling())
          collectVars(n, vars);
      }
    }

    private void findEqualities(AST node, Map<String, Set<String>> eqls, Map<String, String> is,
                                Map<String, ExprType> types) {
      if (node.getType() == TRIPLE) {
        OqlAST s = (OqlAST) node.getFirstChild();
        OqlAST p = (OqlAST) s.getNextSibling();
        OqlAST o = (OqlAST) p.getNextSibling();

        if (p.getText().equals("<mulgara:equals>")) {
          addValToList(eqls, s.getText(), o.getText());
          addValToList(eqls, o.getText(), s.getText());

          ExprType commonType = getCommonType(s.getExprType(), o.getExprType());
          types.put(s.getText(), commonType);
          types.put(o.getText(), commonType);
        } else if (p.getText().equals("<mulgara:is>")) {
          is.put(s.getText(), o.getText());
        }
      } else {
        for (AST n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
          // equality can only be propagated up past an AND
          if (n.getType() == AND || n.getType() == TRIPLE)
            findEqualities(n, eqls, is, types);
        }
      }
    }

    private static void addValToList(Map<String, Set<String>> map, String key, String val) {
      Set<String> vals = map.get(key);
      if (vals == null)
        map.put(key, vals = new HashSet<String>());
      vals.add(val);
    }

    /**
     * Determine the common type for the types of two nodes being merged. Class wins over known
     * type which wins over unknown. In case of two classes, the superclass wins. These rules are
     * so filters get properly attached.
     */
    private static ExprType getCommonType(ExprType t1, ExprType t2) {
      // if either is unknown, the other wins
      if (t1 == null)
        return t2;
      if (t2 == null)
        return t1;

      boolean is1Class =
          t1.getType() == ExprType.Type.CLASS || t1.getType() == ExprType.Type.EMB_CLASS;
      boolean is2Class =
          t2.getType() == ExprType.Type.CLASS || t2.getType() == ExprType.Type.EMB_CLASS;

      // if neither is a class, then we pick a random type
      if (!is1Class && !is2Class)
        return t1;

      // class always wins over non-class
      if (is1Class && !is2Class)
        return t1;
      if (!is1Class && is2Class)
        return t2;

      // both are class-types: choose super-class
      ClassMetadata c1 = t1.getExprClass();
      ClassMetadata c2 = t2.getExprClass();

      if (c1.isAssignableFrom(c2))
        return t1;
      else
        return t2;
    }

    /**
     * Build a map of replacements to apply.
     */
    private void findReplacements(Map<String, Set<String>> eqls, Map<String, String> repl,
                                  Map<String, String> is, Map<String, ExprType> types,
                                  Set<String> ctxtVars) {
      // create complete groups of equal nodes
      List<Set<String>> eqlSets = new ArrayList<Set<String>>();
      while (eqls.size() > 0) {
        String key = eqls.keySet().iterator().next();
        Set<String> eqlSet = eqls.remove(key);
        eqlSets.add(eqlSet);

        Set<String> tmp = new HashSet<String>();
        do {
          tmp.clear();
          for (String var : eqlSet) {
            Set<String> set = eqls.remove(var);
            if (set != null)
              tmp.addAll(set);
          }
          eqlSet.addAll(tmp);
        } while (tmp.size() > 0);
      }

      // choose a survivor for each group, preferring context over user over temporary vars
      List<String> srvs = new ArrayList<String>();
      for (Set<String> eqlSet : eqlSets) {
        String surv = null;
        String cand = null;
        for (String node : eqlSet) {
          if (ctxtVars.contains(node)) {
            surv = node;
            break;
          }
          if (node.indexOf('$') < 0)
            cand = node;
        }

        if (surv == null)
          surv = (cand != null) ? cand : eqlSet.iterator().next();

        srvs.add(surv);
        eqlSet.remove(surv);
      }

      // build replacement map, excluding context vars; figure out the survivor's type too.
      for (int idx = 0; idx < eqlSets.size(); idx++) {
        String surv = srvs.get(idx);
        for (String node : eqlSets.get(idx)) {
          if (!ctxtVars.contains(node)) {
            repl.put(node, surv);
            types.put(surv, getCommonType(types.get(node), types.get(surv)));
          }

          String c = is.remove(node);
          if (c != null && !ctxtVars.contains(surv))
            is.put(surv, c);
        }
      }

      // update context vars
      ctxtVars.addAll(srvs);
    }

    /**
     * Apply the replacements to the subject, predicate, and object of all triples. Resulting
     * tautologies are removed, as are empty logical nodes; logical nodes with a single child
     * are flattened. The types of the replaced nodes are updated too.
     */
    private boolean applyReplacements(AST node, AST prnt, AST prev, Map<String, String> repl,
                                      Map<String, String> is, Map<String, ExprType> types) {
      boolean removed = false;

      if (node.getType() == TRIPLE) {
        OqlAST s = (OqlAST) node.getFirstChild();
        OqlAST p = (OqlAST) s.getNextSibling();
        OqlAST o = (OqlAST) p.getNextSibling();

        // apply replacements
        String r = repl.get(s.getText());
        if (r != null) {
          s.setText(r);
          s.setExprType(types.get(r));
        }

        r = repl.get(p.getText());
        if (r != null) {
          p.setText(r);
          p.setExprType(types.get(r));
        }

        r = repl.get(o.getText());
        if (r != null) {
          o.setText(r);
          o.setExprType(types.get(r));
        }

        r = is.get(s.getText());
        if (r != null) {
          s.setText(r);
          s.setIsVar(false);
        }

        r = is.get(p.getText());
        if (r != null) {
          p.setText(r);
          p.setIsVar(false);
        }

        r = is.get(o.getText());
        if (r != null) {
          o.setText(r);
          o.setIsVar(false);
        }

        // remove eliminated nodes
        if (p.getText().equals("<mulgara:equals>") || p.getText().equals("<mulgara:is>")) {
          if (s.getText().equals(o.getText()))
            removed = removeNode(node, prnt, prev);
        }
      } else {
        for (AST n = node.getFirstChild(), p = null; n != null; n = n.getNextSibling()) {
          if (applyReplacements(n, node, p, repl, is, types))
            ;
          else if (p == null)
            p = node.getFirstChild();
          else
            p = p.getNextSibling();
        }

        // remove empty nodes and flatten single-child AND/OR nodes
        if (node.getFirstChild() == null)
          removed = removeNode(node, prnt, prev);
        else if (node.getFirstChild().getNextSibling() == null &&
                 (node.getType() == AND || node.getType() == OR))
          flattenNode(node, prnt, prev);
      }

      return removed;
    }

    /**
     * Apply the replacements to the projection variables (types are not changed).
     */
    private void applyReplacements(AST var, Map<String, String> repl, Map<String, String> is) {
      String r = repl.get(var.getText());
      if (r != null)
        var.setText(r);

      r = is.get(var.getText());
      if (r != null) {
        var.setText(r);
        ((OqlAST) var).setIsVar(false);
      }
    }

    private static boolean removeNode(AST node, AST prnt, AST prev) {
      if (prev == null && prnt == null)
        return false;

      if (prev != null)
        prev.setNextSibling(node.getNextSibling());
      else
        prnt.setFirstChild(node.getNextSibling());

      return true;
    }

    private static void flattenNode(AST node, AST prnt, AST prev) {
      if (prev == null && prnt == null)
        return;

      AST chld = node.getFirstChild();
      chld.setNextSibling(node.getNextSibling());

      if (prev != null)
        prev.setNextSibling(chld);
      else
        prnt.setFirstChild(chld);
    }

    private void removeConstFromOrder(AST order) {
      // (ID (ASC|DESC)?)+
      for (AST oitem = order.getFirstChild(), prev = null; oitem != null; ) {
        if (oitem.getType() == ID && !((OqlAST) oitem).isVar()) {
          oitem = oitem.getNextSibling();
          if (oitem != null && oitem.getType() != ID)
            oitem = oitem.getNextSibling();

          if (prev != null)
            prev.setNextSibling(oitem);
          else
            order.setFirstChild(oitem);
        } else {
          prev  = oitem;
          oitem = oitem.getNextSibling();
        }
      }
    }

    private OqlAST createVar(String name) {
      OqlAST res = (OqlAST) #([ID, name]);
      res.setIsVar(true);
      return res;
    }

    private Set<String> findUnconstrainedVars(AST proj, AST where, Set<String> ctxtVars) {
      Set<String> projVars = new HashSet<String>();
      findProjVars(proj, projVars);
      projVars.removeAll(ctxtVars);

      findUnconstrainedVars(where, projVars);
      return projVars;
    }

    private void findProjVars(AST node, Set<String> projVars) {
      for (AST n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
        if (n.getType() == COMMA) {
          findProjVars(n, projVars);
        } else {
          n = n.getNextSibling();
          if (n.getType() == ID && ((OqlAST) n).isVar())
            projVars.add(n.getText());
        }
      }
    }

    private void findUnconstrainedVars(AST node, Set<String> projVars) {
      for (AST n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
        if (((OqlAST) n).isVar())
          projVars.remove(n.getText());
        else
          findUnconstrainedVars(n, projVars);

        if (projVars.size() == 0)
          return;
      }
    }
}


query
    : ! {
        if (_t == null)         // dummy throws to satisfy compiler
          throw new RecognitionException();
        simplifyQuery(_t, new HashSet<String>());
        #query = _t;
      }
    ;

