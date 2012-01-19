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

      // 4: create dummy constraint if wclause otherwise empty
      if (!hasConstraints(wc))
        wc.addChild(#([TRIPLE,"triple"], #([ID,"$t$1"]), #([ID,"$t$2"]), #([ID,"$t$3"])));

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
        Map<String, Set<String>> eqls = new HashMap<String, Set<String>>();
        Map<String, String>      is   = new HashMap<String, String>();
        findEqualities(node, eqls, is);

        // find replacements, transitively
        Map<String, String> repl = new HashMap<String, String>();
        findReplacements(eqls, repl, is, ctxtVars);

        if (log.isTraceEnabled()) {
          log.trace("replacements:  " + repl);
          log.trace("is-expansions: " + is);
        }

        // rewrite our expression tree
        if (repl.size() > 0 || is.size() > 0) {
          applyReplacements(node, null, null, repl, is);
          for (AST n : extWcls)
            applyReplacements(n, null, null, repl, is);
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

    private void findEqualities(AST node, Map<String, Set<String>> eqls, Map<String, String> is) {
      if (node.getType() == TRIPLE) {
        OqlAST s = (OqlAST) node.getFirstChild();
        OqlAST p = (OqlAST) s.getNextSibling();
        OqlAST o = (OqlAST) p.getNextSibling();

        if (p.getText().equals("<mulgara:equals>")) {
          addValToList(eqls, s.getText(), o.getText());
          addValToList(eqls, o.getText(), s.getText());
        } else if (p.getText().equals("<mulgara:is>")) {
          is.put(s.getText(), o.getText());
        }
      } else {
        for (AST n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
          // equality can only be propagated up past an AND
          if (n.getType() == AND || n.getType() == TRIPLE)
            findEqualities(n, eqls, is);
        }
      }
    }

    private static void addValToList(Map<String, Set<String>> map, String key, String val) {
      Set<String> vals = map.get(key);
      if (vals == null)
        map.put(key, vals = new HashSet<String>());
      vals.add(val);
    }

    private void findReplacements(Map<String, Set<String>> eqls, Map<String, String> repl,
                                  Map<String, String> is, Set<String> ctxtVars) {
      // create complete groups of equal nodes
      List<Set<String>> eqlSets = new ArrayList<Set<String>>();
      while (eqls.size() > 0) {
        String key = eqls.keySet().iterator().next();
        Set<String> eqlSet = eqls.remove(key);
        eqlSets.add(eqlSet);

        boolean found;
        do {
          found = false;
          for (String var : eqlSet) {
            Set<String> set = eqls.remove(var);
            if (set != null) {
              eqlSet.addAll(set);
              found = true;
            }
          }
        } while (found);
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

      // build replacement map, excluding context vars
      for (int idx = 0; idx < eqlSets.size(); idx++) {
        String surv = srvs.get(idx);
        for (String node : eqlSets.get(idx)) {
          if (!ctxtVars.contains(node))
            repl.put(node, surv);

          String c = is.remove(node);
          if (c != null && !ctxtVars.contains(surv))
            is.put(surv, c);
        }
      }

      // update context vars
      ctxtVars.addAll(srvs);
    }

    private void applyReplacements(AST node, AST prnt, AST prev, Map<String, String> repl,
                                   Map<String, String> is) {
      if (node.getType() == TRIPLE) {
        OqlAST s = (OqlAST) node.getFirstChild();
        OqlAST p = (OqlAST) s.getNextSibling();
        OqlAST o = (OqlAST) p.getNextSibling();

        // apply replacements
        String r = repl.get(s.getText());
        if (r != null) {
          s.setText(r);
        }

        r = repl.get(o.getText());
        if (r != null) {
          o.setText(r);
        }

        r = is.get(s.getText());
        if (r != null) {
          s.setText(r);
          s.setIsVar(false);
        }

        r = is.get(o.getText());
        if (r != null) {
          o.setText(r);
          o.setIsVar(false);
        }

        // remove eliminated nodes
        if (p.getText().equals("<mulgara:equals>") || p.getText().equals("<mulgara:is>")) {
          if (s.getText().equals(o.getText())) {
            if (prev != null)
              prev.setNextSibling(node.getNextSibling());
            else if (prnt != null)
              prnt.setFirstChild(node.getNextSibling());
          }
        }
      } else {
        prev = null;
        for (AST n = node.getFirstChild(); n != null; prev = n, n = n.getNextSibling())
          applyReplacements(n, node, prev, repl, is);
      }
    }

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

    private void removeConstFromOrder(AST order) {
      // (ID (ASC|DESC)?)+
      for (AST oitem = order.getFirstChild(), prev = null; oitem != null;
           prev = oitem, oitem = oitem.getNextSibling()) {
        if (oitem.getType() == ID && !((OqlAST) oitem).isVar()) {
          AST nxt = oitem.getNextSibling();
          if (nxt != null && nxt.getType() != ID)
            nxt = nxt.getNextSibling();

          if (prev != null)
            prev.setNextSibling(nxt);
          else
            order.setFirstChild(nxt);
        }
      }
    }

    private boolean hasConstraints(AST node) {
      for (AST n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
        switch (n.getType()) {
          case AND:
          case OR:
            if (hasConstraints(n))
              return true;
            break;

          default:
            return true;
        }
      }

      return false;
    }
}


query
    : (any)+ { simplifyQuery(#query, new HashSet<String>()); }
    ;

any
    : #(. (any)*)
    ;

