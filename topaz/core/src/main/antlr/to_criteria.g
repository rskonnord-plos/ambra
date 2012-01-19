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

header
{
/*
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
import org.topazproject.otm.mapping.EmbeddedMapper;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import antlr.RecognitionException;
import antlr.collections.AST;
}

/**
 * This is an AST transformer for OQL that generates a Criteria definition for this query. Of
 * course this only works within the limitations of what is supported by Criteria:
 * <ul>
 *   <li>only a single entry in the select clause</li>
 *   <li>the projection expression in the select clause may only contain a variable or a
 *       simple dereference of a variable; it may not contain a constant, a function, or
 *       a wildcard projector.</li>
 *   <li>in the select or where clauses, dereferences may not contain an immediate url
 *       (x.&lt;foo:bar&gt;) or a predicate expression.</li>
 *   <li>one side of equality comparisons ('=', '!=', 'lt()', 'le()', etc) must be a constant;
 *       the other side must have at least one dereference, and the last dereference may not
 *       be cast. E.g.
 *       <pre>
 *       ... v.name = 'john' ...
 *       ... v.address.city = 'london' ...
 *       ... cast(v.item, Article).label = 'foo' ...
 *       </pre>
 *       are legal, but these are not:
 *       <pre>
 *       ... v = 'john' ...
 *       ... cast(v.item, Article) = 'foo' ...
 *       ... v.name = n ...
 *       </pre>
 *   </li>
 *   <li>if there are multiple constraints on an item (as represented by a variable) which are
 *       'and'd or 'or'd together, then all of these that contain a dereference of the variable
 *       which is an association must be 'and'd together and 'and'd with the rest of the constraints
 *       on that item. E.g.
 *       <pre>
 *       ... v.name = 'john' and v.address.zip = '42' ...
 *       ... v.address.city = 'london' and v.address.zip = '42' ...
 *       ... (v.name = 'john' or lt(v.age, 65)) and v.address.zip = '42' ...
 *       </pre>
 *       are legal, but these are not:
 *       <pre>
 *       ... v.name = 'john' or v.address.zip = '42' ...
 *       ... v.address.city = 'london' or v.address.zip = '42' ...
 *       </pre>
 *   </li>
 * </ul>
 *
 * <p>Implemenation:
 * <ol>
 *   <li>The parsing catches unsupported structures such as uri-ref, predicate expressions,
 *       wildcard expressions, multiple projection expressions, subqueries, and constants in the
 *       projection expression.</li>
 *   <li>During parsing, the types for variables in the from clause are registered and all aliases
 *       are registered.</li>
 *   <li>Resolve the types of all variables; since variables from the 'from' clause already have
 *       their types, this just involves the aliases.</li>
 *   <li>Build the criteria for the first projection expression (the root of the criteria tree).</li>
 *   <li>Build criteria for all aliases.</li>
 *   <li>The criteria and criterion for all the expressions in the where clause are built</li>
 * </ol>
 * Note regarding the building of the criteria tree: the only two ways to define a variable in OQL
 * are the 'from' clause and aliases. This means that once the types of the aliases are resolved we
 * have enough information to determine the type of any expression we may encounter. And once we've
 * created the criteria for the aliases, we have a criteria object to which we can attach the
 * expression results (be they criteria and/or criterion).
 *
 * @author Ronald Tschalär 
 */
class CriteriaGenerator extends TreeParser("OqlTreeParser");

options {
    importVocab = Query;
}

{
    private static final Log log = LogFactory.getLog(CriteriaGenerator.class);

    private Session                    sess;
    private Criteria                   rootCrit;
    private AST                        projExpr;
    private List<Order>                orders = new ArrayList<Order>();
    private int                        limit  = -1;
    private int                        offset = -1;
    private Map<String, AST>           aliasMap = new HashMap<String, AST>();
    private Map<String, ClassMetadata> typeMap  = new HashMap<String, ClassMetadata>();
    private Map<String, Criteria>      critMap  = new HashMap<String, Criteria>();

    /** 
     * Create a new translator instance.
     *
     * @param sessionFactory the session-factory to use to look up entity-metadata, models, etc
     */
    public CriteriaGenerator(Session session) {
      this();
      sess = session;
    }

    /**
     * @return the generated criteria
     */
    public Criteria getCriteria() {
      return rootCrit;
    }

    private void buildCriteria(AST root) throws RecognitionException {
      root = root.getFirstChild().getNextSibling();
      assert root.getType() == WHERE : "Unexpected type: " + root.getType();
      root = root.getFirstChild();

      resolveAliasTypes();
      createRootCriteria();
      createAliasCriteria();

      if (root.getType() == AND)
        buildCriteria(root, new HashMap<String, AST>());
      else
        processCriterion(root, null, null, new HashMap<String, AST>());
    }

    private void resolveAliasTypes() throws RecognitionException {
      Map<String, AST> aliases = new HashMap<String, AST>(aliasMap);

      while (aliases.size() > 0)
        resolveAliasType(aliases.keySet().iterator().next(), aliases);
    }

    private void resolveAliasType(String alias, Map<String, AST> aliases)
          throws RecognitionException {
      // get the rhs and mark that we've resolved this alias
      AST factor = aliases.remove(alias);

      if (log.isTraceEnabled())
        log.trace("resolving type for alias '" + alias + "'");

      // resolve
      switch (factor.getType()) {
        case REF:
          AST id = factor.getFirstChild();
          ClassMetadata cm;

          if (id.getType() == ID) {
            String var = id.getText();
            // get the variable's type, resolving the rhs variable if necessary
            if (!typeMap.containsKey(var) && aliases.containsKey(var))
              resolveAliasType(var, aliases);
            if (!typeMap.containsKey(var))
              throw new RecognitionException("'" + var + "' is not defined anywhere");

            cm = typeMap.get(var);
          } else {
            assert id.getType() == CAST : "Unexpected type: " + id.getType();
            String type = id.getFirstChild().getNextSibling().getText();

            cm = sess.getSessionFactory().getClassMetadata(type);
            if (cm == null)
              throw new RecognitionException("No metadata found for entity '" + type + "'");
          }

          // walk the references
          while ((id = id.getNextSibling()) != null)
             cm = getAssociationType(cm, id.getText());

          //save the type
          typeMap.put(alias, cm);
          break;

        case FUNC:
          throw new RecognitionException("Function '" + factor.getFirstChild().getText() +
                                         "' is not supported yet");

        default:
          typeMap.put(alias, null);
          break;
      }

      if (log.isTraceEnabled())
        log.trace("resolved type for alias '" + alias + "': " + typeMap.get(alias));
    }

    private void createAliasCriteria() throws RecognitionException {
      Map<String, AST> aliases = new HashMap<String, AST>(aliasMap);

      /* start with the alias for the projection expression, if any, in order that we correctly
       * walk up the tree and build referrer criteria where necessary.
       */
      assert critMap.size() == 1 : "Unexpected number of elements in criteria-map: " + critMap;
      String rootVar = critMap.keySet().iterator().next();
      if (aliases.containsKey(rootVar))
        createAliasCriteria(rootVar, aliases);

      // process all remaining aliases
      while (aliases.size() > 0)
        createAliasCriteria(aliases.keySet().iterator().next(), aliases);
    }

    /* It is not possible currently to create a standalone criteria and then attach it to a
     * parent later, i.e. child- and referrer-criteria must be created on their parent. This
     * means that we have to really build the criteria tree from the root (criterion can be
     * added at any time later). For aliases that result in child criteria this is easily
     * achieved by recursively creating the criteria for the top variable on the rhs and then
     * creating criteria for the dereferences. E.g. in
     * <pre>
     *   select o from X o where q := p.bar and p := o.foo and q.baz = '42'
     * </pre>
     * we resolve q by first (recursively) resolving the first variable ('p') and then
     * creating a child criteria on p's criteria. This works because this algorithm will
     * first walk to the top ('o') and then walk back down, creating criteria on the way.
     *
     * <p>For referrer criteria we need to traverse backwards. E.g. in
     * <pre>
     *   select p from X o where q := p.bar and p := o.foo and q.baz = '42'
     * </pre>
     * we need to realize when processing 'p' that we already have a criteria for it and
     * that we must therefore create referrer-criteria up to 'o' instead of a child-criteria
     * from 'o' down.
     */
    private void createAliasCriteria(String alias, Map<String, AST> aliases)
          throws RecognitionException {
      // get the rhs and mark that we've handled this alias
      AST factor = aliases.remove(alias);

      if (log.isTraceEnabled())
        log.trace("creating criteria for alias '" + alias + "'");

      // handle
      switch (factor.getType()) {
        case REF:
          String var = getTopVar(factor).getText();

          if (critMap.containsKey(alias)) {     // we're walking up the tree from the projection
            Criteria crit = critMap.get(alias);
            critMap.put(var, makeParents(factor, crit));
            assert typeMap.get(var) == critMap.get(var).getClassMetadata() :
                   "Internal type mismatch for '" + var + "': typeMap=" + typeMap.get(var) +
                   ", critMap=" + critMap.get(var).getClassMetadata();

            if (aliases.containsKey(var))
              createAliasCriteria(var, aliases);

          } else {                              // we're walking down the tree
            Criteria crit;
            if (!critMap.containsKey(var)) {
              if (aliases.containsKey(var)) {
                createAliasCriteria(var, aliases);
              } else {
                reportWarning("Unused alias definition for alias '" + alias + "'");
                // This is just to simplify the rest of the logic, but in the end it gets tossed
                crit = new Criteria(sess, null, null, false, typeMap.get(alias), null);
                makeParents(factor, crit);
                critMap.put(var, crit);
              }
            }
            crit = makeChildren(factor, critMap.get(var), true);

            critMap.put(alias, crit);
            assert typeMap.get(alias) == critMap.get(alias).getClassMetadata() :
                   "Internal type mismatch for '" + alias + "': typeMap=" + typeMap.get(alias) +
                   ", critMap=" + critMap.get(alias).getClassMetadata();
          }
          break;

        default:
          break;
      }

      if (log.isTraceEnabled())
        log.trace("created criteria for alias '" + alias + "': " + critMap.get(alias));
    }

    /**
     * Create a criteria object for the given ast.
     *
     * @param root the ast object to process
     * @param vars the currently active variables (not including aliases)
     */
    private void buildCriteria(AST root, Map<String, AST> vars) throws RecognitionException {
      for (AST n = root.getFirstChild(); n != null; n = n.getNextSibling()) {
        switch (n.getType()) {
          case AND:
            buildCriteria(n, vars);
            break;

          case OR:
          case EQ:
          case NE:
          case FUNC:
          case ASGN:
            processCriterion(n, null, null, vars);
            break;

          default:
            throw new RecognitionException("Unexpected node type '" + n.getType() + "' in " +
                                           root.toStringTree());
        }
      }
    }

    /** 
     * Process what must be a criterion.
     *
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

        case EQ:
        case NE:
          pVar = processEquality(node, parent, pVar, vars);
          break;

        case FUNC:
          pVar = processFunc(node, parent, pVar, vars);
          break;

        case ASGN:
          break;

        default:
          throw new RecognitionException("Unexpected node type '" + node.getType() + "' in " +
                                         node.toStringTree());
      }

      if (c != null) {
        if (parent == null)
          parent = critMap.get(pVar);
        if (parent == null)
          throw new RecognitionException("Unknown variable '" + pVar + "' in " +
                                         node.toStringTree());

        addCriterion(parent, c);
      }

      return pVar;
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

    /**
     * Create the Criterion for an equality expression. For discussion below, the rhs is assumed
     * to the constant and the lhs the dereference.
     *
     * @param node   the equality node with two children representing the lhs and rhs
     * @param parent if non-null then this is the Criteria or Criterion to which to attach the
     *               resulting Criterion; in this case the lhs must be of the form 'a.b', i.e.
     *               exactly one dereference and 'a' must be the same as <var>pVar</var> if that
     *               is non-null.
     * @param pVar   if non-null, then this must match the top-level item in the lhs; only non-null
     *               if <var>parent</var> is non-null.
     * @param vars   the currently active variables (not including aliases)
     * @return the top variable in the lhs; this will be <var>pVar</var> if that was non-null
     */
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
      if (l.getType() != REF || !isConstant(r))
        throw new RecognitionException("equality comparisons must be between a reference and a " +
                                       "constant: " + node.toStringTree());

      if (parent == null) {
        pVar   = checkFullDeref(l);
        parent = critMap.get(pVar);
        if (parent == null)
          throw new RecognitionException("unknown variable '" + pVar + "': " + node.toStringTree());

        parent = makeChildren(l, (Criteria) parent, false);
      } else {
        String p = checkSimpleDeref(l);
        if (pVar != null && !p.equals(pVar))
          throw new RecognitionException("criterion must all have same parent; found '" + pVar +
                                         "' and '" + p + "' in " + node.toStringTree());
        pVar = p;
      }

      // generate criterion
      String f = getLastField(l).getText();
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
        if (args == null || a2 == null || args.getType() != REF || !isConstant(a2))
          throw new RecognitionException("comparisons must be between a reference and a " +
                                         "constant: " + node.toStringTree());
      }

      boolean haveMulti = false;
      AST     ref       = null;
      for (n = args; n != null; n = n.getNextSibling()) {
        if (n.getType() != REF)
          continue;

        ref = n;
        AST top = getTopVar(ref);

        if (ref.getFirstChild().getNextSibling() == null)
          throw new RecognitionException("A trailing (non-casted) dereference is required by " +
                                         "criteria at this point: " + node.toStringTree());
        if (countDeref(ref) > 1) {
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

    /**
     * Constant substitution. If the node refers to a variable or an alias which is a constant,
     * then replace the node with the constant.
     */
    private AST replaceVars(AST node, AST parent, Map<String, AST> vars)
        throws RecognitionException {
      if (node.getType() != REF)
        return node;

      AST id = node.getFirstChild();

      AST repl = vars.get(id.getText());
      if (repl == null)
        repl = aliasMap.get(id.getText());
      if (repl == null || !isConstant(repl))
        return node;

      repl = astFactory.dupList(repl);
      repl.setNextSibling(node.getNextSibling());

      if (parent.getFirstChild() == node)
        parent.setFirstChild(repl);
      else
        getPreceedingChild(parent, node).setNextSibling(repl);

      return repl;
    }

    private static boolean isConstant(AST node) {
      int type = node.getType();
      return (type == QSTRING || type == URIREF || type == PARAM);
    }

    private String checkFullDeref(AST ref) throws RecognitionException {
      if (ref.getFirstChild().getNextSibling() == null)
        throw new RecognitionException("A trailing (non-casted) dereference is required by " +
                                       "criteria at this point: " + ref.toStringTree());

      return getTopVar(ref).getText();
    }

    private String checkSimpleDeref(AST ref) throws RecognitionException {
      AST n = ref.getFirstChild();
      if (n.getType() == CAST)
        throw new RecognitionException("Cast is not supported by criteria at this point: " +
                                       ref.toStringTree());

      n = n.getNextSibling();
      if (n == null || n.getNextSibling() != null)
        throw new RecognitionException("Exactly one dereference is required by criteria at " +
                                       "this point: " + ref.toStringTree());

      return ref.getFirstChild().getText();
    }

    private int countDeref(AST ref) throws RecognitionException {
      AST n   = ref.getFirstChild();
      int cnt = 0;

      for (; n.getNextSibling() != null; n = n.getNextSibling())
        cnt++;

      if (n.getType() == CAST)
        cnt += countDeref(n.getFirstChild());

      return cnt;
    }

    /**
     * Create all the children criteria for the given ref.
     *
     * @param ref     the reference for which to create the child criteria
     * @param p       the parent criteria to create the child criteria on
     * @param incLast if false don't create a criteria for the last child
     * @return the last child criteria created
     */
    private Criteria makeChildren(AST ref, Criteria p, boolean incLast)
        throws RecognitionException {
      return makeChildren(ref, p, null, incLast);
    }

    private Criteria makeChildren(AST ref, Criteria p, String type, boolean incLast)
        throws RecognitionException {
      AST n = ref.getFirstChild();

      if (n.getType() == CAST) {
        String t = n.getFirstChild().getNextSibling().getText();
        p = makeChildren(n.getFirstChild(), p, t, incLast || n.getNextSibling() != null);
        if (!p.getClassMetadata().getName().equals(normalizeType(t)))
          throw new RecognitionException("Found two different types for '" +
                                         n.getFirstChild().toStringTree() + "'; first type = '" +
                                         p.getClassMetadata().getName() + ", second type = '" + t +
                                         "'");
      }

      try {
        while ((n = n.getNextSibling()) != null && (incLast || n.getNextSibling() != null))
          p = p.createCriteria(n.getText(), (n.getNextSibling() == null) ? type : null);
      } catch (OtmException oe) {
        throw (RecognitionException)
            new RecognitionException("no field '" + n.getText() + "' in " + p.getClassMetadata() +
                                     ": " + ref.toStringTree()).initCause(oe);
      }
      return p;
    }

    private String normalizeType(String entity) {
      return sess.getSessionFactory().getClassMetadata(entity).getName();
    }

    /**
     * Create all the parent criteria for the given ref. I.e. this starts with the given criteria
     * representing the last id in the ref and builds referrer criteria for the id's in reverse
     * order.
     *
     * @param ref the reference representing the 
     * @param p   the child criteria to create the referrer criteria on
     * @return the last referrer criteria created (representing the first id of <var>ref</var>)
     */
    private Criteria makeParents(AST ref, Criteria p) throws RecognitionException {
      AST id = ref.getFirstChild();
      return makeParents(id, p, typeMap.get(id.getText()));
    }

    /**
     * @param id  the node up to which to create the parents (ID or CAST)
     * @param p   the child criteria to create the referrer criteria on
     * @param cm  the type of <var>id</var>
     * @return the last referrer criteria created (representing the first id of <var>ref</var>)
     */
    private Criteria makeParents(AST id, Criteria p, ClassMetadata cm) throws RecognitionException {
      // assert parent(id).getType() == REF

      if (id.getType() == CAST) {
        String type = id.getFirstChild().getNextSibling().getText();
        if (cm == null)
          cm = sess.getSessionFactory().getClassMetadata(type);
        else if (!cm.getName().equals(normalizeType(type)))
          throw new RecognitionException("Found two different types for '" +
                                         id.getFirstChild().toStringTree() + "'; first type = '" +
                                         cm.getName() + ", second type = '" + type + "'");
      }

      if (id.getNextSibling() != null) {
        if (cm == null)
          throw new RecognitionException("'" + id.getText() + "' is not a valid variable or is " +
                                         "not an association");

        AST f = id.getNextSibling();
        String field = f.getText();
        ClassMetadata at = cm;
        while (at.getMapperByName(f.getText()) instanceof EmbeddedMapper) {
          at     = getAssociationType(at, f.getText());
          f      = f.getNextSibling();
          field += "." + f.getText();
        }
        at = getAssociationType(at, f.getText());

        Criteria c = makeParents(f, p, at);
        p = c.createReferrerCriteria(cm.getName(), field, true);
      }

      if (id.getType() == CAST) {
        AST ref = id.getFirstChild();
        p = makeParents(ref.getFirstChild(), p, getIdType(ref.getFirstChild()));
      }

      return p;
    }

    private ClassMetadata getAssociationType(ClassMetadata cm, String field)
          throws RecognitionException {
      Mapper m = cm.getMapperByName(field);

      if (m instanceof RdfMapper)
        return sess.getSessionFactory().getClassMetadata(((RdfMapper) m).getAssociatedEntity());
      if (m instanceof EmbeddedMapper)
        return ((EmbeddedMapper) m).getEmbeddedClass();

      throw new RecognitionException("'" + field + "' is not an association in '" + cm.getName() +
                                     "'");
    }

    private ClassMetadata getIdType(AST id) throws RecognitionException {
      ClassMetadata cm;

      if (id.getType() == ID) {
        String var = id.getText();
        cm = typeMap.get(var);
        if (cm == null)
          throw new RecognitionException("No type found for variable '" + var + "'");
      } else {
        assert id.getType() == CAST : "Unexpected type: " + id.getType();
        String type = id.getFirstChild().getNextSibling().getText();
        cm = sess.getSessionFactory().getClassMetadata(type);
        if (cm == null)
          throw new RecognitionException("No metadata found for entity '" + type + "'");
      }

      return cm;
    }

    /**
     * Create a value object for the given node; for uri's a URI, for literals the literal
     * value, for parameters a parameter object, and for references the last id (i.e. 'z' in
     * 'x.y.z').
     */
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
        return getLastField(v).getText();
      }

      throw new RecognitionException("Internal error: unexpected value node '" + v.getType() + "'");
    }

    private static AST getLastField(AST ref) {
      AST fc = ref.getFirstChild();

      if (fc.getNextSibling() != null)
        return getPreceedingChild(ref, null);
      if (fc.getType() == ID)
        return fc;
      if (fc.getType() == CAST)
        return getLastField(fc.getFirstChild());

      assert false : "Unexpected type found: " + fc.getType();
      return null;      // not reached - just here to satisfy compiler
    }

    private static AST getPreceedingChild(AST parent, AST child) {
      AST n;
      for (n = parent.getFirstChild(); n.getNextSibling() != child; n = n.getNextSibling())
        ;
      return n;
    }

    private static AST getTopVar(AST ref) {
      AST n = ref.getFirstChild();
      while (n.getType() == CAST)
        n = n.getFirstChild().getFirstChild();
      return n;
    }

    private void createRootCriteria() throws RecognitionException {
      assert projExpr.getType() == REF : "Projection-expr type is not REF: " + projExpr.getType();
      assert projExpr.getFirstChild().getType() == ID || projExpr.getFirstChild().getType() == CAST:
             "Projection-expr child is not ID or CAST: " + projExpr.getFirstChild().getType();

      String var = getTopVar(projExpr).getText();

      ClassMetadata cm = typeMap.get(var);
      if (cm == null)
        throw new RecognitionException("projection variable '" + var + "' is not of type entity");

      rootCrit = new Criteria(sess, null, null, false, cm, null);

      critMap.put(var, makeParents(projExpr, rootCrit));
      assert typeMap.get(var) == critMap.get(var).getClassMetadata() :
             "Internal type mismatch for '" + var + "': typeMap=" + typeMap.get(var) +
             ", critMap=" + critMap.get(var).getClassMetadata();

      for (Order o : orders)
        rootCrit.addOrder(o);
      if (limit >= 0)
        rootCrit.setMaxResults(limit);
      if (offset >= 0)
        rootCrit.setFirstResult(offset);

      if (log.isTraceEnabled())
        log.trace("created root criteria for '" + var + "': " + rootCrit);
    }

    private void registerType(AST var, AST cls) throws RecognitionException {
      ClassMetadata cm = sess.getSessionFactory().getClassMetadata(cls.getText());
      if (cm == null)
        throw new RecognitionException("No metadata found for entity '" + cls.getText() + "'");

      if (log.isTraceEnabled())
        log.trace("registering type for variable '" + var.getText() + "': " + cm);

      typeMap.put(var.getText(), cm);
    }

    private void registerAlias(AST var, AST factor) throws RecognitionException {
      String v = var.getText();
      if (aliasMap.containsKey(v))
        throw new RecognitionException("multiple assignments of same variable is not supported " +
                                       "by criteria: var = '" + v + "'");

      if (log.isTraceEnabled())
        log.trace("registering alias '" + v + "': " + factor.toStringTree());

      aliasMap.put(v, factor);
    }
}


query
    : #(SELECT #(FROM fclause) #(WHERE wclause) #(PROJ sclause) (oclause)? (lclause)? (tclause)?) {
        buildCriteria(#query);
      }
    ;


fclause
    : #(COMMA fclause fclause)
    | cls:ID var:ID  { registerType(#var, #cls); }
    ;


sclause
    : #(COMMA sclause sclause) {
        if (true)
          throw new RecognitionException("criteria supports only a single result in select clause");
      }
    | (ID)? pexpr
    ;

pexpr
    : #(SUBQ query) {
        if (true)       // dummy for compiler
          throw new RecognitionException("criteria doesn't support subqueries");
      }
    | factor {
        if (#pexpr.getType() == REF)
          projExpr = #pexpr;
        else
          throw new RecognitionException("only field references supported by criteria in " +
                                         "projections: " + #pexpr.toStringTree());
      }
    ;


wclause
    : (expr)?
    ;

expr
    : #(AND (expr)+)
    | #(OR  (expr)+)
    | #(ASGN v:ID f:factor) { registerAlias(#v, #f); }
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
          if (n.getType() != ID && n.getType() != CAST)
            throw new RecognitionException("only field references are supported by criteria in " +
                                           "constraints (no uri-ref or wildcards): " +
                                           #deref.toStringTree());
      }
    ;

cast
    : #(CAST factor ID) {
        if (#cast.getFirstChild().getType() != REF)
          throw new RecognitionException("only field references are supported by criteria in " +
                                         "constraints (no constants or function calls): " +
                                         #cast.toStringTree());
      }
    ;


oclause
    : #(ORDER (oitem)+)
    ;

oitem
    : var:ID (ASC|d:DESC)? { orders.add(new Order(#var.getText(), #d == null)); }
    ;

lclause
    : #(LIMIT n:NUM) { limit = Integer.parseInt(#n.getText()); }
    ;

tclause
    : #(OFFSET n:NUM) { offset = Integer.parseInt(#n.getText()); }
    ;

