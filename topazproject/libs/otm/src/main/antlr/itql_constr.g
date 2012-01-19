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

import java.util.Set;

import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.Session;

import antlr.RecognitionException;
import antlr.collections.AST;
}

/**
 * This generates an iTQL AST from an OQL AST. It is assumed that field-name-to-predicate
 * expansion has already been done.
 *
 * <p>Only the select and where clauses are transformed; the resulting AST has the following
 * form:
 * <pre>
 *   pexpr:   clist
 *          | #(SUBQ query)
 *          | #(COUNT query)
 *   where: oexpr
 *   oexpr: #(OR  (aexpr)+)
 *   aexpr: #(AND (dterm)+)
 *   dterm:   cfact
 *          | #(MINUS dterm oexpr)
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
    MINUS   = "minus";
    TRANS   = "trans";
    WALK    = "walk";
    COUNT   = "count";
}

{
    private static final String   TMP_VAR_PFX = "oqltmp2_";
    private static final String   XSD         = "http://www.w3.org/2001/XMLSchema#";
    private static final String[] DATE_TYPES = {
        XSD + "date", XSD + "dateTime", XSD + "gYear", XSD + "gYearMonth",
    };
    private static final String[] NUM_TYPES = {
        XSD + "decimal", XSD + "float", XSD + "double", XSD + "integer", XSD + "nonPositiveInteger",
        XSD + "negativeInteger", XSD + "long", XSD + "int", XSD + "short", XSD + "byte",
        XSD + "nonNegativeInteger", XSD + "unsignedLong", XSD + "unsignedInt",
        XSD + "unsignedShort", XSD + "unsignedByte", XSD + "positiveInteger",
    };

    private              Session sess;
    private              int     varCnt = 0;

    public ItqlConstraintGenerator(Session session) {
      this();
      sess = session;
    }

    private OqlAST nextVar() {
      String v = TMP_VAR_PFX + varCnt++;
      OqlAST res = (OqlAST) #([ID, v]);
      res.setIsVar(true);
      return res;
    }

    private AST addTriple(AST list, AST prevVar, AST prevPred) {
      if (prevPred != null) {
        AST obj = nextVar();
        list.addChild(makeTriple(prevVar, prevPred, obj));
        prevVar = obj;
        ((OqlAST) prevVar).setExprType(((OqlAST) prevPred).getExprType());
      }
      return prevVar;
    }

    private AST[] addColTriples(AST list, AST curVar, OqlAST curPred) throws RecognitionException {
      ExprType type = curPred.getExprType();
      if (type == null)
        return new AST[] { curVar, curPred };

      switch (type.getCollectionType()) {
        case PREDICATE:
          return new AST[] { curVar, curPred };

        case RDFBAG:
        case RDFSEQ:
        case RDFALT:
          curVar  = addTriple(list, curVar, curPred);

          OqlAST listPred = nextVar();
          listPred.setModel(curPred.getModel());
          listPred.setExprType(curPred.getExprType());

          // FIXME: avoid hardcoded model-name
          list.addChild(makeTriple(listPred, "<mulgara:prefix>", "<rdf:_>", getModelUri("prefix")));

          return new AST[] { curVar, listPred };

        case RDFLIST:
          if (curVar != null)
            throw new RecognitionException("RdfList not supported, pending Mulgara fix");

          curVar  = addTriple(list, curVar, curPred);

          OqlAST s = nextVar();
          OqlAST o = nextVar();
          // XXX: doesn't work!!! Need to "fix" mulgara
          AST walk = #([WALK,"walk"], makeTriple(curVar, "<rdf:rest>", o, curPred.getModel()),
                                      makeTriple(s, "<rdf:rest>", o, curPred.getModel()));
          list.addChild(walk);

          listPred = makeID("<rdf:first>");
          listPred.setModel(curPred.getModel());
          listPred.setExprType(curPred.getExprType());
          ((OqlAST) s).setExprType(curPred.getExprType());

          return new AST[] { s, listPred };

        case PREDICATE_MAP:
          throw new RecognitionException("Predicate-Map not supported (yet)");

        default:
          throw new RecognitionException("Unknown mapper-type '" + type.getCollectionType() + "'");
      }
    }

    private String getModelUri(String modelId) throws RecognitionException {
      ModelConfig mc = sess.getSessionFactory().getModel(modelId);
      if (mc == null)
        throw new RecognitionException("Unable to find model '" + modelId + "'");
      return mc.getUri().toString();
    }

    private OqlAST makeID(Object obj) {
      if (obj instanceof OqlAST) {
        OqlAST ast = (OqlAST) astFactory.dup((OqlAST) obj);
        if (ast.getType() != ID)
          ast.setType(ID);
        return ast;
      }

      String str = (String) obj;
      return (OqlAST) #([ID, str]);
    }

    private AST makeTriple(Object s, Object p, Object o) {
      return makeTriple(s, p, o, (p instanceof OqlAST) ? ((OqlAST) p).getModel() : null);
    }

    private AST makeTriple(Object s, Object p, Object o, Object m) {
      OqlAST sa = makeID(s);
      OqlAST pa = makeID(p);
      OqlAST oa = makeID(o);
      OqlAST ma = (m != null) ? makeID(m) : null;

      if (pa.isInverse())
        return #([TRIPLE,"triple"], oa, pa, sa, ma);
      else
        return #([TRIPLE,"triple"], sa, pa, oa, ma);
    }

    private AST handleFunction(AST ns, AST fname, AST args, AST resVar, boolean isProj)
        throws RecognitionException {
      String f = fname.getText();

      if (isProj && ns == null && f.equals("count"))
        return handleCount(args, resVar);

      if (!isProj && ns == null &&
          (f.equals("lt") || f.equals("gt") || f.equals("le") || f.equals("ge")))
        return handleCompare(f, args);

      throw new RecognitionException("unrecognized function call '" +
                                     (ns != null ? ns.getText() + ":" : "") + f + "'");
    }

    private AST handleCount(AST args, AST resVar) throws RecognitionException {
      // parse args
      if (args.getNumberOfChildren() != 2)
        throw new RecognitionException("count() must have exactly 1 argument, but found " +
                                       (args.getNumberOfChildren() / 2));
      AST var = args.getFirstChild();
      AST arg = var.getNextSibling();

      // handle count(<constant>)
      if (arg.getType() == TRIPLE)
        return #([QSTRING, "'1'"]);

      assert arg.getType() == AND;

      // need to equate the expression's end var with the expected result var
      AST pexpr = astFactory.dupTree(arg);
      if (!var.equals(resVar))
        pexpr.addChild(makeTriple(var, "<mulgara:equals>", resVar));
      ((OqlAST) resVar).setExprType(((OqlAST) var).getExprType());

      // create subquery
      AST from  = #([FROM, "from"], #([ID, "dummy"]), #([ID, "dummy"]));
      AST where = #([WHERE, "where"], pexpr);
      AST proj  = #([PROJ, "projection"], astFactory.dup(resVar), astFactory.dup(resVar));

      return #([COUNT, "count"], #([SELECT, "select"], from, where, proj));
    }

    private AST handleCompare(String f, AST args) throws RecognitionException {
      // parse args
      if (args.getNumberOfChildren() != 4)
        throw new RecognitionException("count() must have exactly 2 arguments, but found " +
                                       (args.getNumberOfChildren() / 2));
      AST lvar = args.getFirstChild();
      AST larg = lvar.getNextSibling();
      AST rvar = larg.getNextSibling();
      AST rarg = rvar.getNextSibling();

      // check argument type compatiblity
      ExprType ltype = ((OqlAST) lvar).getExprType();
      ExprType rtype = ((OqlAST) rvar).getExprType();

      if (ltype != null && rtype != null) {
        ExprType.Type lt = ltype.getType();
        ExprType.Type rt = rtype.getType();

        if (lt == ExprType.Type.CLASS || lt == ExprType.Type.EMB_CLASS ||
            rt == ExprType.Type.CLASS || rt == ExprType.Type.EMB_CLASS)
          throw new RecognitionException("Can't compare class types");

        if (lt != rt)
          throw new RecognitionException("left and right side are not of same type: " + lt +
                                         " != " + rt);
        if (lt == ExprType.Type.TYPED_LIT && !ltype.getDataType().equals(rtype.getDataType()))
          throw new RecognitionException("left and right side are not of same data-type: " +
                                         ltype.getDataType() + " != " + rtype.getDataType());
      }

      // create expression
      String pred, model;
      if (isDate(ltype)) {
        pred  = (f.charAt(0) == 'l') ? "<mulgara:before>" : "<mulgara:after>";
        model = "xsd";
      } else if (isNum(ltype)) {
        pred  = (f.charAt(0) == 'l') ? "<mulgara:lt>" : "<mulgara:gt>";
        model = "xsd";
      } else {
        pred  = (f.charAt(0) == 'l') ? "<topaz:lt>" : "<topaz:gt>";
        model = "str";
      }

      AST res = #([AND, "and"], astFactory.dupTree(larg), astFactory.dupTree(rarg));
      if (f.equals("ge") || f.equals("le"))
        res.addChild(#([OR, "or"],
                       makeTriple(lvar, pred, rvar, getModelUri(model)),
                       makeTriple(lvar, "<mulgara:equals>", rvar)));
      else
        res.addChild(makeTriple(lvar, pred, rvar, getModelUri(model)));

      return res;
    }

    private static boolean isDate(ExprType type) {
      return (type != null) && (type.getType() == ExprType.Type.TYPED_LIT) &&
             isDt(type.getDataType(), DATE_TYPES);
    }

    private static boolean isNum(ExprType type) {
      return (type != null) && (type.getType() == ExprType.Type.TYPED_LIT) &&
             isDt(type.getDataType(), NUM_TYPES);
    }

    private static boolean isDt(String v, String[] list) {
      for (String s : list) {
        if (v.equals(s))
          return true;
      }
      return false;
    }

    private String expandAliases(String uri) {
      // TODO: should use aliases in SessionFactory
      for (String alias : (Set<String>) ItqlHelper.getDefaultAliases().keySet())
        uri = uri.replace("<" + alias + ":", "<" + ItqlHelper.getDefaultAliases().get(alias));
      return uri.substring(1, uri.length() - 1);
    }
}


query
{ AST tc = #([AND, "and"]); }
    :   #(SELECT #(FROM fclause[tc]) #(WHERE w:wclause[tc]) #(PROJ sclause[#w]) (oclause)? (lclause)? (tclause)?)
    ;


fclause[AST tc]
    :   #(COMMA fclause[tc] fclause[tc])
    |   cls:ID var:ID {
          ClassMetadata cm   = ((OqlAST) #var).getExprType().getMeta();
          String        type = cm.getType();
          if (type != null)
            tc.addChild(
                    makeTriple(#var, "<rdf:type>", "<" + type + ">", getModelUri(cm.getModel())));
        }
    ;


sclause[AST w]
    :   #(COMMA sclause[w] sclause[w])
    |   v:ID pexpr[#v, w]
    ;

pexpr[AST var, AST where]
    :   #(SUBQ query)
    |   ! f:factor[var, true] {
          // all expressions except count (which really is a subquery) get added to the where clause
          if (#f.getType() == COUNT)
            #pexpr = #f;
          else {
            where.addChild(#f);
            #pexpr = astFactory.dup(var);
          }
        }
    ;   // pexpr is now either a subquery or the variable


wclause[AST tc]
    : ! (e:expr)? { #wclause = #([AND, "and"], tc, #e); }
    ;

expr
{ AST var; }
    : #(AND (expr)+)
    | #(OR  (expr)+)

    | ! #(ASGN ID af:factor[#ID, false]) { #expr = #af; }

    | ! #(EQ { var = nextVar(); } ef1:factor[var, false] ef2:factor[var, false]) {
        #expr = #([AND,"and"], #ef1, #ef2);
      }

    | ! #(NE { var = nextVar(); } nf1:factor[var, false] nf2:factor[var, false]) {
        #expr = #([MINUS,"minus"], #nf1, #nf2);
      }

    | fcall[nextVar(), false]
    ;

factor[AST var, boolean isProj]
    : constant[var]
    | fcall[var, isProj]
    | deref[var]
    ;

constant[AST var]
    : ! QSTRING ((DHAT type:URIREF) | (AT lang:ID))? {
          String lit = #QSTRING.getText();
          String dt  = (#type != null) ? expandAliases(#type.getText()) : null;
          if (dt != null)
            lit += "^^<" + dt + ">";
          else if (#lang != null)
            lit += "@" + #lang.getText();
          #constant = makeTriple(var, "<mulgara:is>", lit);

          if (dt != null)
            ((OqlAST) var).setExprType(ExprType.literalType(dt, null));
          else
            ((OqlAST) var).setExprType(ExprType.literalType(null));
        }

    | ! URIREF {
          #constant = makeTriple(var, "<mulgara:is>", #URIREF);
          ((OqlAST) var).setExprType(ExprType.uriType(null));
        }
    ;

fcall[AST var, boolean isProj]
{ AST args = #([COMMA]), va; }
    : ! #(FUNC ns:ID (COLON fn:ID)?
               (arg:factor[va = nextVar(), isProj] { args.addChild(#va); args.addChild(#arg); })*) {
          if (#fn == null) {
            #fn = #ns;
            #ns = null;
          }

          #fcall = handleFunction(#ns, #fn, args, var, isProj);
        }
    ;

deref[AST var]
{ AST prevVar, prevPred = null, res = #([AND,"and"]); boolean wantPred = false; }
    :! #(REF (   v:ID { prevVar = #v; }
               | r:deref[prevVar = nextVar()] { res.addChild(#r); }
             )
             (   p:URIREF {
                   prevVar  = addTriple(res, prevVar, prevPred);
                   AST[] x  = addColTriples(res, prevVar, (OqlAST) #p);
                   prevVar  = x[0];
                   prevPred = x[1];
                 }
               | #(EXPR pv:ID (e:expr)?) {
                   prevVar  = addTriple(res, prevVar, prevPred);
                   prevPred = #pv;
                   res.addChild(#e);
                 }
             )*
             (STAR {
                 prevVar  = addTriple(res, prevVar, prevPred);
                 wantPred = true;
             }
             )?
       ) {
         if (wantPred) {
           res.addChild(makeTriple(prevVar, var, nextVar()));
           ((OqlAST) var).setExprType(ExprType.uriType(null));
         } else if (prevPred != null) {
           res.addChild(makeTriple(prevVar, prevPred, var));
           ((OqlAST) var).setExprType(((OqlAST) prevPred).getExprType());
         } else if (!prevVar.equals(var)) {       // XXX
           res.addChild(makeTriple(prevVar, "<mulgara:equals>", var));
           ((OqlAST) var).setExprType(((OqlAST) prevVar).getExprType());
         }

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

