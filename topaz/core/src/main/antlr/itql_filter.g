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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Session;
import org.topazproject.otm.stores.ItqlFilter;

import antlr.RecognitionException;
import antlr.ASTPair;
import antlr.collections.AST;
}

/**
 * This applies any relevant filters to the where clause of an iTQL AST. The filter application
 * is done recursively in subqueries too.
 *
 * <h2>Filter Processing Notes</h2>
 *
 * <p>Filter processing would be simple, if it weren't for casts. Casts mean that a variable may
 * have different types in different parts of the where clause, and hence the filter constraints
 * can't just be and'd onto the whole where clause. However, not all variables can be subject to
 * casting; specifically, generated "internal" variables will always have a single type. Also,
 * casts are likely to be rare, and cases where a variable ends up with different types even rarer.
 *
 * <p>So we use a slightly more elaborate scheme: we find all variables and note their type and
 * "location" (the parent AND - we also make sure such a thing always exists).  Then we go through
 * and for all variables with a single type we add filter expressions (if needed) directly to the
 * where clause's top AND; for variables with multiple types we add the filter expressions (if
 * needed) to the local AND's.
 *
 * @author Ronald Tschalär 
 */
class ItqlFilterApplicator extends TreeParser("OqlTreeParser");

options {
    importVocab = Constraints;
    buildAST    = true;
}

{
    private Session                sess;
    private String                 tmpVarPfx;
    private int                    varCnt = 0;
    private Collection<ItqlFilter> filters;

    public ItqlFilterApplicator(Session session, String tmpVarPfx, Collection<ItqlFilter> filters) {
      this();
      this.sess          = session;
      this.tmpVarPfx     = tmpVarPfx;
      this.filters       = filters;
    }

    private void applyFilters(AST query, Set<String> outerVars) {
      // avoid work if possible
      if (filters == null || filters.size() == 0)
        return;

      // #(SELECT #(FROM fclause) #(WHERE wclause) #(PROJ sclause) (oclause)? (lclause)? (tclause)?)
      assert query.getType() == SELECT;

      AST f = query.getFirstChild();
      AST w = f.getNextSibling();
      AST p = w.getNextSibling();

      assert f.getType() == FROM;
      assert w.getType() == WHERE;
      assert p.getType() == PROJ;

      AST fc = f.getFirstChild();
      AST wc = w.getFirstChild();
      AST pc = p.getFirstChild();

      // process the 'where' clause
      applyFilters(wc, outerVars, tmpVarPfx + varCnt++ + "_");

      // find and process subqueries
      findSubqueries(pc, outerVars);
    }

    private void findSubqueries(AST node, Set<String> outerVars) {
      if (node.getType() == SUBQ || node.getType() == COUNT)
        applyFilters(node.getFirstChild(), new HashSet<String>(outerVars));
      else {
        for (AST n = node.getFirstChild(); n != null; n = n.getNextSibling())
          findSubqueries(n, outerVars);
      }
    }

    private void applyFilters(AST root, Set<String> outerVars, String pfx) {
      org.apache.commons.logging.LogFactory.getLog(ItqlFilterApplicator.class).debug("applying Filters: " + root.toStringTree());
      // make a note of all variables and their types
      Map<String, Map<ExprType, Collection<ASTPair>>> varTypes =
                                          new HashMap<String, Map<ExprType, Collection<ASTPair>>>();
      findVarsAndTypes(root, new ArrayList<AST>(), varTypes);

      // process each variable
      int idx = 0;
      for (Map.Entry<String, Map<ExprType, Collection<ASTPair>>> ve : varTypes.entrySet()) {
        // outer variables are "constant", and hence shouldn't be filtered
        if (outerVars.contains(ve.getKey()))
          continue;

        if (ve.getValue().size() == 1) {         // single type - add globally
          Collection<ItqlFilter> flist = findFilters(ve.getValue().keySet().iterator().next());
          if (flist == null)
            continue;
          for (ItqlFilter f : flist)
            root.addChild(getFilterAST(f, ve.getKey(), pfx + idx++ + "_"));

        } else {                                // multiple types - add locally
          for (Map.Entry<ExprType, Collection<ASTPair>> te : ve.getValue().entrySet()) {
            Collection<ItqlFilter> flist = findFilters(te.getKey());
            if (flist == null || flist.isEmpty())
              continue;
            for (ASTPair p : te.getValue()) {
              if (p.root.getType() != AND)
                insertAnd(p);
              for (ItqlFilter f : flist)
                p.root.addChild(getFilterAST(f, ve.getKey(), pfx + idx++ + "_"));
            }
          }
        }
      }

      outerVars.addAll(varTypes.keySet());

      org.apache.commons.logging.LogFactory.getLog(ItqlFilterApplicator.class).debug("Filters applied: " + root.toStringTree());
    }

    private void findVarsAndTypes(AST node, List<AST> ancestors,
                                  Map<String, Map<ExprType, Collection<ASTPair>>> varTypes) {
      noteType(node, ancestors, varTypes);
      for (AST n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
        ancestors.add(node);
        findVarsAndTypes(n, ancestors, varTypes);
        ancestors.remove(ancestors.size() - 1);
      }
    }

    private void noteType(AST node, List<AST> ancestors,
                          Map<String, Map<ExprType, Collection<ASTPair>>> varTypes) {
      // is this thing eligible for filtering?
      OqlAST n = (OqlAST) node;
      if (!n.isVar())
        return;

      // ok, take note of it
      Map<ExprType, Collection<ASTPair>> types = varTypes.get(n.getText());
      if (types == null)
        varTypes.put(n.getText(), types = new HashMap<ExprType, Collection<ASTPair>>());

      Collection<ASTPair> ps = types.get(n.getExprType());
      if (ps == null)
        types.put(n.getExprType(), ps = new ArrayList<ASTPair>());

      assert ancestors.get(ancestors.size() - 1).getType() == TRIPLE;
      ASTPair p = new ASTPair();
      p.root  = ancestors.get(ancestors.size() - 2);
      p.child = ancestors.get(ancestors.size() - 1);
      ps.add(p);
    }

    private Collection<ItqlFilter> findFilters(ExprType type) {
      if (type == null)
        return null;

      ClassMetadata cm = type.getExprClass();
      if (cm == null)
        return null;

      Collection<ItqlFilter> list = new ArrayList<ItqlFilter>();
      for (ItqlFilter f : filters) {
        ClassMetadata fcm = sess.getSessionFactory().getClassMetadata(f.getFilteredClass());
        if (fcm != null && fcm.isAssignableFrom(cm))
          list.add(f);
      }

      return list;
    }

    private AST getFilterAST(ItqlFilter f, String var, String pfx) {
      AST res;
      int idx = 0;

      switch (f.getType()) {
        case PLAIN:
          Map renMap = new HashMap<String, String>();
          renMap.put(f.getVar(), var);
          return renameVariables(astFactory.dupTree(f.getDef()), renMap, pfx + "f");

        case AND:
          res = #([AND, "and"]);
          for (ItqlFilter cf : f.getFilters())
            res.addChild(getFilterAST(cf, var, pfx + "a" + idx++ + "_"));
          return res;

        case OR:
          res = #([OR, "or"]);
          for (ItqlFilter cf : f.getFilters())
            res.addChild(getFilterAST(cf, var, pfx + "o" + idx++ + "_"));
          return res;

        default:
          throw new Error("Unknown filter type '" + f.getType() + "'");
      }
    }

    private void insertAnd(ASTPair parents) {
      AST and = #([AND, "and"], parents.child);
      and.setNextSibling(parents.child.getNextSibling());

      if (parents.root.getFirstChild() == parents.child) {
        parents.root.setFirstChild(and);
      } else {
        AST prev;
        for (prev = parents.root.getFirstChild(); prev.getNextSibling() != parents.child;
             prev = prev.getNextSibling())
          ;
        prev.setNextSibling(and);
      }

      parents.root = and;
    }

    /**
     * Rename all the variables in the filter. This is to ensure uniqueness when the same filter
     * gets applied more than once due to multiple variables having the same type. For each
     * variable found, if a variable is already in the rename-map then it is renamed accordingly;
     * otherwise a new entry is created in the map, mapping the variable to a new unique name.
     *
     * @param node   the filter on which to perform the renames
     * @param renMap the map of variable renamings; this may be preloaded with mappings, and
     *               will be expanded with new ones as needed
     * @param pfx    the prefix to use for new variables
     */
    private static AST renameVariables(AST node, Map<String, String> renMap, String pfx) {
      OqlAST n = (OqlAST) node;
      if (n.isVar()) {
        String f = n.getText();
        String t = renMap.get(f);
        if (t == null)
          renMap.put(f, t = pfx);
        n.setText(t);
      } else {
        int idx = 0;
        for (AST nn = node.getFirstChild(); nn != null; nn = nn.getNextSibling())
          renameVariables(nn, renMap, pfx + idx++ + "_");
      }
      return node;
    }
}


query
    : ! {
        if (_t == null)         // dummy throws to satisfy compiler
          throw new RecognitionException();
        applyFilters(_t, new HashSet<String>());
        #query = _t;
      }
    ;

