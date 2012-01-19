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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.Session;

import antlr.RecognitionException;
import antlr.ASTPair;
import antlr.collections.AST;
}

/**
 * This generates an iTQL AST from an OQL AST. It is assumed that field-name-to-predicate expansion
 * and parameter resolution have already been done.
 *
 * <p>Only the select and where clauses are transformed; the resulting AST has the following
 * form:
 * <pre>
 *   query: #(SELECT #(FROM ...) #(WHERE where) #(PROJ (pexpr)+) (oclause)? (lclause)? (tclause)?)
 *   pexpr:   clist
 *          | #(SUBQ query)
 *          | #(COUNT query)
 *   where: oexpr
 *   oexpr: #(OR  (mexpr)+)
 *   mexpr: #(MINUS aexpr oexpr)
 *   aexpr: #(AND (cfact)+)
 *   cfact:   clist
 *          | oexpr
 *          | #(TRANS cnstr cnstr? )
 *          | #(WALK  cnstr cnstr )
 *   clist:   #(AND (cnstr)+)
 *          | cnstr
 *   cnstr: #(TRIPLE ID ID ID ID?)
 * </pre>
 *
 * @author Ronald Tschalär
 */
class ItqlConstraintGenerator extends TreeParser("OqlTreeParser");

options {
    importVocab = Query;
    exportVocab = Constraints;
    buildAST    = true;
}

tokens {
    TRIPLE  = "triple";
    TRANS   = "trans";
    WALK    = "walk";
    COUNT   = "count";
}

{
    private static final URI PREFIX_GRAPH_TYPE =
                                            URI.create("http://mulgara.org/mulgara#PrefixGraph");

    private Session          sess;
    private String           tmpVarPfx;
    private boolean          addTypeConstr;
    private int              varCnt = 0;

    public ItqlConstraintGenerator(Session session, String tmpVarPfx, boolean addTypeConstr) {
      this();
      this.sess          = session;
      this.tmpVarPfx     = tmpVarPfx;
      this.addTypeConstr = addTypeConstr;
    }

    private OqlAST nextVar() {
      String v = tmpVarPfx + varCnt++;
      OqlAST res = (OqlAST) #([ID, v]);
      res.setIsVar(true);
      return res;
    }

    private AST buildSClauseFrag(List<OqlAST> proj, AST var, QueryFunction f) {
      AST exp = astFactory.dup(proj.get(0));
      ((OqlAST) exp).setFunction(f);

      var.setNextSibling(exp);
      exp.setNextSibling(null);

      if (proj.size() <= 1)
        return var;

      return #([COMMA,"comma"], var, buildSClauseFrag(proj.subList(1, proj.size()), nextVar(), f));
    }

    private OqlAST addTriple(OqlAST list, OqlAST prevVar, OqlAST prevPred) {
      if (prevPred != null) {
        OqlAST obj = nextVar();
        obj.setExprType(prevPred.getExprType());
        list.addChild(makeTriple(prevVar, prevPred, obj));
        prevVar = obj;
      }
      return prevVar;
    }

    private OqlAST[] addColTriples(OqlAST list, OqlAST curVar, OqlAST curPred)
        throws RecognitionException {
      ExprType type = curPred.getExprType();
      if (type == null) {
        notifyDeref(curPred, new OqlAST[] { curVar, curPred });
        return new OqlAST[] { curVar, curPred };
      }

      switch (type.getCollectionType()) {
        case PREDICATE:
          notifyDeref(curPred, new OqlAST[] { curVar, curPred });
          return new OqlAST[] { curVar, curPred };

        case RDFBAG:
        case RDFSEQ:
        case RDFALT:
          OqlAST prevVar = curVar;
          curVar         = addTriple(list, curVar, curPred);

          OqlAST listPred = nextVar();
          listPred.setGraph(curPred.getGraph());
          listPred.setExprType(curPred.getExprType());

          list.addChild(makeTriple(listPred, "<mulgara:prefix>", "<rdf:_>",
                                   getGraphUri(PREFIX_GRAPH_TYPE)));

          notifyDeref(curPred, new OqlAST[] { prevVar, curPred, curVar, listPred });
          return new OqlAST[] { curVar, listPred };

        case RDFLIST:
          prevVar = curVar;
          curVar  = addTriple(list, curVar, curPred);

          OqlAST s = nextVar();
          OqlAST n = nextVar();
          /* FIXME: doesn't work! Need to "fix" mulgara
          AST walk = #([WALK,"walk"], makeTriple(curVar, "<rdf:rest>", n, curPred.getGraph()),
                                      makeTriple(s, "<rdf:rest>", n, curPred.getGraph()));
          list.addChild(walk);

           * instead we use a less efficient method for now (taken from ItqlStore):
           *   (trans($cur <rdf:rest> $s) or $cur <rdf:rest> $s or $prevVar $prevPred $s)
           *     and $prevVar $prevPred $cur and $s <rdf:first> $o and $s <rdf:rest> $n

           * FIXME: can't specify an 'in' clause in trans()
           */
          AST trans = #([TRANS,"trans"], makeTriple(curVar, "<rdf:rest>", s));
          AST exist = makeTriple(curVar, "<rdf:rest>", s, curPred.getGraph());
          AST first = makeTriple(prevVar, curPred, s, curPred.getGraph());
          list.addChild(#([OR,"or"], trans, exist, first));
          list.addChild(makeTriple(s, "<rdf:rest>", n, curPred.getGraph()));
          /* end trans version */

          listPred = ASTUtil.makeID("<rdf:first>", astFactory);
          listPred.setGraph(curPred.getGraph());
          listPred.setExprType(curPred.getExprType());
          s.setExprType(curPred.getExprType());
          n.setExprType(ExprType.uriType(null));

          notifyDeref(curPred, new OqlAST[] { prevVar, curPred, s, n });
          return new OqlAST[] { s, listPred };

        default:
          throw new RecognitionException("Unknown mapper-type '" + type.getCollectionType() + "'");
      }
    }

    private void notifyDeref(OqlAST reg, OqlAST[] nodes) throws RecognitionException {
      Set<TransformListener> listeners = reg.getListeners();
      if (listeners == null)
        return;

      for (TransformListener tl : listeners)
        tl.deref(reg, nodes);
    }

    private String getGraphUri(String graphId) throws RecognitionException {
      return ASTUtil.getGraphUri(graphId, sess.getSessionFactory());
    }

    private String getGraphUri(URI graphType) throws RecognitionException {
      return ASTUtil.getGraphUri(graphType, sess.getSessionFactory());
    }

    private AST makeTriple(Object s, Object p, Object o) {
      return ASTUtil.makeTriple(s, p, o, astFactory);
    }

    private AST makeTriple(Object s, Object p, Object o, Object g) {
      return ASTUtil.makeTriple(s, p, o, g, astFactory);
    }

    private String expandAlias(String uri) {
      return sess.getSessionFactory().expandAlias(uri.substring(1, uri.length() - 1));
    }
}


query
{ OqlAST tc = (OqlAST) #([AND, "and"]); }
    :   #(SELECT #(FROM fclause[tc]) #(WHERE w:wclause[tc]) #(PROJ sclause[#w]) (oclause)? (lclause)? (tclause)?)
    ;


fclause[OqlAST tc]
    :   #(COMMA fclause[tc] fclause[tc])
    |   cls:ID var:ID {
          ClassMetadata cm   = ((OqlAST) #var).getExprType().getMeta();
          if (addTypeConstr)
            for (String type : cm.getTypes())
              tc.addChild(
                    makeTriple(#var, "<rdf:type>", "<" + type + ">", getGraphUri(cm.getGraph())));
        }
    ;


sclause[AST w]
    :   #(COMMA sclause[w] sclause[w])
    |   ! v:ID e:pexpr[(OqlAST) #v, w] {
          if (#e.getType() == COMMA)
            #sclause = #e;
          else {
            #sclause = #v;
            #v.setNextSibling(#e);
          }
        }
    ;

pexpr[OqlAST var, AST where]
    :   #(SUBQ query)
    |   ! f:factor[var, true] {
          OqlAST ff = (OqlAST) f;
          // all expressions except subqueries get added to the where clause
          if (#f.getType() == SUBQ || #f.getType() == COUNT)
            #pexpr = #f;
          else {
            where.addChild(#f);

            OqlAST[] proj = (f.getType() == FUNC) ?
                ((ProjectionFunction) ff.getFunction()).getItqlProjections() : null;
            if (proj == null)
              #pexpr = astFactory.dup(var);
            else
              #pexpr = buildSClauseFrag(Arrays.asList(proj), astFactory.dup(var), ff.getFunction());
          }

          if (ff.getFunction() != null && ff.getFunction() instanceof ProjectionFunction)
            ((OqlAST) #pexpr).setFunction(ff.getFunction());
        }
    ;   // pexpr is now either a subquery, the variable, or a comma-tree of sclause


wclause[OqlAST tc]
    : ! (e:expr)? { tc.addChild(#e); #wclause = tc; }
    ;

expr
{ OqlAST var; }
    : #(AND (expr)+)
    | #(MINUS expr expr)
    | #(OR  (expr)+)

    | ! #(ASGN ID af:factor[(OqlAST) #ID, false]) { #expr = #af; }

    | ! #(EQ { var = nextVar(); } ef1:factor[var, false] ef2:factor[var, false]) {
        #expr = #([AND,"and"], #ef1, #ef2);
      }

    | ! #(NE { var = nextVar(); } nf1:factor[var, false] nf2:factor[var, false]) {
        #expr = #([MINUS,"minus"], #nf1, #nf2);
      }

    | fcall[nextVar(), false]
    ;

factor[OqlAST var, boolean isProj]
    : constant[var]
    | fcall[var, isProj]
    | deref[var, isProj]
    ;

constant[OqlAST var]
    : ! QSTRING ((DHAT type:URIREF) | (AT lang:ID))? {
          String lit = #QSTRING.getText();
          String dt  = (#type != null) ? expandAlias(#type.getText()) : null;
          if (dt != null)
            lit += "^^<" + dt + ">";
          else if (#lang != null)
            lit += "@" + #lang.getText();
          #constant = makeTriple(var, "<mulgara:is>", lit);

          if (dt != null)
            var.setExprType(ExprType.literalType(dt, null));
          else
            var.setExprType(ExprType.literalType(null));
        }

    | ! URIREF {
          #constant = makeTriple(var, "<mulgara:is>", #URIREF);
          ((OqlAST) var).setExprType(ExprType.uriType(null));
        }
    ;

fcall[OqlAST var, boolean isProj]
{ List<OqlAST> args = new ArrayList<OqlAST>(), vars = new ArrayList<OqlAST>(); OqlAST v; }
    : ! #(FUNC ID (COLON ID)?
               (arg:factor[v = nextVar(), isProj] { vars.add(v); args.add((OqlAST) #arg); })*) {
          #fcall = ((OqlAST) #FUNC).getFunction().toItql(args, vars, var, astFactory,
                                                         tmpVarPfx + varCnt++ + "f");
        }
    ;

deref[OqlAST var, boolean isProj]
{
OqlAST prevVar, prevPred = null, res = (OqlAST) #([AND,"and"]);
boolean wantPred = false, isId = false;
}
    :! #(REF (   v:ID { prevVar = (OqlAST) #v; }
               | r:deref[prevVar = nextVar(), isProj] { res.addChild(#r); }
             )
             (   p:URIREF {
                   prevVar    = addTriple(res, prevVar, prevPred);
                   OqlAST[] x = addColTriples(res, prevVar, (OqlAST) #p);
                   prevVar    = x[0];
                   prevPred   = x[1];
                 }
               | #(EXPR pv:ID (e:expr)?) {
                   prevVar  = addTriple(res, prevVar, prevPred);
                   prevPred = (OqlAST) #pv;
                   res.addChild(#e);
                 }
             )*
             (   STAR {
                   prevVar  = addTriple(res, prevVar, prevPred);
                   wantPred = true;
                 }
               | ID {
                   assert #ID.getText().equals(".id");
                   isId = true;
                 }
             )?
       ) {
         if (isId && isProj)                    // would like a nicer solution here...
           var.setExprType(ExprType.uriType(null));
         else if (wantPred)
           var.setExprType(ExprType.uriType(null));
         else if (prevPred != null)
           var.setExprType(prevPred.getExprType());
         else
           var.setExprType(prevVar.getExprType());

         if (wantPred)
           res.addChild(makeTriple(prevVar, var, nextVar()));
         else if (prevPred != null)
           res.addChild(makeTriple(prevVar, prevPred, var));
         else if (!prevVar.equals(var))         // XXX
           res.addChild(makeTriple(prevVar, "<mulgara:equals>", var));

         #deref = res;
      }
    ;


oclause
    : #(ORDER (oitem)+)
    ;

oitem
    : ID (ASC|DESC)?
    ;

lclause
    : #(LIMIT NUM)
    ;

tclause
    : #(OFFSET NUM)
    ;

