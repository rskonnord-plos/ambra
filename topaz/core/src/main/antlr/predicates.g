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

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.mapping.EmbeddedMapper;
import org.topazproject.otm.mapping.Binder;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;

import antlr.ASTPair;
import antlr.RecognitionException;
import antlr.collections.AST;
}

/**
 * This is an AST transformer for OQL that replaces field references by their predicate URI's. It
 * also resolves away casts, does some checks the variables (duplicate declarations, order-by not
 * referencing projections, etc), and creates dummy variables for projections where no variable was
 * specified. And finally it associates types and models with the various nodes.
 *
 * @author Ronald Tschalär 
 */
class FieldTranslator extends TreeParser("OqlTreeParser");

options {
    importVocab = Query;
    buildAST    = true;
}

{
    private static final String   TMP_VAR_PFX = "oqltmp1_";
    // TODO: get these from the list of registered functions
    private static final String[] binaryComparisonFuncs = new String[] { "lt", "le", "gt", "ge" };

    private final Map<String, ExprType> vars = new HashMap<String, ExprType>();
    private final Map<String, ExprType> prms = new HashMap<String, ExprType>();
    private final Set<String>           prjs = new HashSet<String>();
    private final Set<QueryFunction>    qfs  = new HashSet<QueryFunction>();
    private SessionFactory              sessFactory;
    private int                         varCnt = 0;

    /** 
     * Create a new translator instance.
     *
     * @param sessionFactory the session-factory to use to look up class-metatdata, models, etc
     */
    public FieldTranslator(SessionFactory sessionFactory) {
      this();
      sessFactory = sessionFactory;
    }

    /** 
     * Get the list of parameters found in the query. This is only valid after invoking
     * <code>query()</code>.
     *
     * @return the found parameters; the keys are the parameter names.
     */
    public Map<String, ExprType> getParameters() {
      return prms;
    }

    private ExprType getTypeForVar(AST id) throws RecognitionException {
      if (!vars.containsKey(id.getText()))
        throw new RecognitionException("no variable '" + id.getText() +
                                       "' defined in from or where clauses");
      return vars.get(id.getText());
    }

    private ExprType getTypeForClass(AST clazz, String loc) throws RecognitionException {
      ClassMetadata md = sessFactory.getClassMetadata(clazz.getText());
      if (md == null && loc != null)
        throw new RecognitionException("unknown class '" + clazz.getText() + "' in " + loc);
      return ExprType.classType(md, null);
    }

    private ExprType resolveField(ASTPair cur, ExprType type, AST field)
        throws RecognitionException {
      // see if this is dereferenceable type (class or embedded-class)
      if (type == null ||
          type.getType() != ExprType.Type.CLASS && type.getType() != ExprType.Type.EMB_CLASS)
        throw new RecognitionException("can't dereference type '" + type + "'; " +
                                       "current node: '" + cur.root + "', field: '" + field +
                                       "'");

      // for embedded classes get the fully-qualified field-name
      String fname = "";
      if (type.getType() == ExprType.Type.EMB_CLASS) {
        EmbeddedMapper last = null;
        for (EmbeddedMapper m : type.getEmbeddedFields()) {
          fname += m.getName() + ".";
          last = m;
        }

        // see if this is a partial match on a hierarchy of embedded classes
        Mapper m = last.getEmbeddedClass().getMapperByName(field.getText());
        if (m instanceof EmbeddedMapper) {
          type.getEmbeddedFields().add((EmbeddedMapper)m);
          return type;
        }
      }
      fname += field.getText();

      // see if we have a mapper for the field; if so, great
      Mapper m = type.getMeta().getMapperByName(fname);
      if (m instanceof RdfMapper) {
        RdfMapper r = (RdfMapper)m;
        ExprType cType = getTypeForMapper(r);

        String uri = "<" + r.getUri() + ">";
        AST ref = #([URIREF, uri]);
        updateAST(ref, type, cType, r, false);
        astFactory.addASTChild(cur, ref);

        return cType;
      }
      
      if (m instanceof EmbeddedMapper) {
        type = ExprType.embeddedClassType(type.getMeta(), (EmbeddedMapper)m);
        getCurAST(cur).setExprType(type);
        return type;
      }

      // see if this is the id-field
      m = type.getMeta().getIdField();
      if (m != null && fname.equals(m.getName())) {
        ExprType cType = ExprType.uriType(null);

        AST ref = #([ID, ".id"]);
        updateAST(ref, type, cType, m, false);
        astFactory.addASTChild(cur, ref);

        return cType;
      }

      // nope, nothing found, must be a user error
      throw new RecognitionException("no field '" + field.getText() + "' in " +
                                     type.getExprClass());
    }

    private static OqlAST getCurAST(ASTPair cur) {
      AST last = cur.child;
      if (last == null)
        last = cur.root;
      return (OqlAST) last;
    }


    private ExprType handlePredicate(ASTPair cur, ExprType type, AST ref)
        throws RecognitionException {
      if (type != null && type.getType() != ExprType.Type.URI &&
          type.getType() != ExprType.Type.CLASS && type.getType() != ExprType.Type.EMB_CLASS)
        throw new RecognitionException("can't dereference type '" + type + "'; " +
                                       "current node: '" + cur.root + "', reference: '" + ref +
                                       "'");

      astFactory.addASTChild(cur, ref);
      updateAST(ref, type, null, null, false);

      return null;
    }

    private ExprType getTypeForMapper(RdfMapper m) {
      if (m == null)
        return null;

      ClassMetadata md;
      if ((md = sessFactory.getClassMetadata(m.getAssociatedEntity())) != null)
        return ExprType.classType(md, m.getColType());

      if (m.typeIsUri())
        return ExprType.uriType(m.getColType());

      if (m.getDataType() != null)
        return ExprType.literalType(m.getDataType(), m.getColType());

      return ExprType.literalType(m.getColType());
    }

    private void updateAST(AST ast, ExprType prntType, ExprType chldType, Mapper m, boolean isVar)
        throws RecognitionException {
      OqlAST a = (OqlAST) ast;
      if (chldType != null)
        a.setExprType(chldType);

      if (prntType != null && prntType.getType() == ExprType.Type.CLASS)
        a.setModel(getModelUri(prntType.getMeta().getModel()));
      else if (prntType != null && prntType.getType() == ExprType.Type.EMB_CLASS)
        a.setModel(getModelUri(findEmbModel(prntType)));

      if (m instanceof RdfMapper) {
        RdfMapper r = (RdfMapper)m;
        a.setIsInverse(r.hasInverseUri());
        if (r.getModel() != null)
          a.setModel(getModelUri(r.getModel()));
      }

      a.setIsVar(isVar);
    }

    private String getModelUri(String modelId) throws RecognitionException {
      ModelConfig mc = sessFactory.getModel(modelId);
      if (mc == null)
        throw new RecognitionException("Unable to find model '" + modelId + "'");
      return mc.getUri().toString();
    }

    private String findEmbModel(ExprType type) {
      // it is the same as the enclosing class for the same reason super-class
      // models are ignored (ie. embedding is a way of sub-classing)
      return type.getMeta().getModel();
    }

    private void addVar(AST var, AST clazz) throws RecognitionException {
      ExprType type = (clazz != null) ? getTypeForClass(clazz, "from clause") : null;
      addVar(var, type);
    }

    private void addVar(AST var, ExprType type) throws RecognitionException {
      if (vars.containsKey(var.getText()))
        throw new RecognitionException("Duplicate variable declaration: var='" + var.getText() +
                                       "', prev type='" + vars.get(var.getText()) +
                                       "', new type='" + type + "'");
      vars.put(var.getText(), type);
      updateAST(var, type, type, null, true);
    }

    private void addParam(AST param, ExprType type) throws RecognitionException {
      if (prms.containsKey(param.getText())) {
        ExprType prev = prms.get(param.getText());
        if (prev == null)
          prms.put(param.getText(), type);
        else if (type != null && !type.equals(prev))
          reportWarning("type mismatch for parameter '" + param.getText() + "': previous type " +
                        "was '" + prev + "' but current type is '" + type + "'");
      } else
        prms.put(param.getText(), type);

      updateAST(param, null, type, null, false);
    }

    private AST nextVar() {
      String v = TMP_VAR_PFX + varCnt++;
      return #([ID, v]);
    }

    private void checkProjVar(String var) throws RecognitionException {
      if (prjs.contains(var))
        throw new RecognitionException("Duplicate projection variable declaration: var='" + var +
                                       "'");
      prjs.add(var);
    }

    private void checkTypeCompatibility(ExprType et1, ExprType et2, AST ex1, AST ex2, AST expr)
        throws RecognitionException {
      // handle parameters
      if (ex1.getType() == PARAM) {
        if (et2 != null && et2.getType() != ExprType.Type.URI &&
            et2.getType() != ExprType.Type.UNTYPED_LIT && et2.getType() != ExprType.Type.TYPED_LIT)
          reportWarning("type mismatch in expression '" + expr.toStringTree() + "': parameter" +
                        " is not comparable to " + et2);

        addParam(ex1, et2);
        return;
      } else if (ex2.getType() == PARAM) {
        if (et1 != null && et1.getType() != ExprType.Type.URI &&
            et1.getType() != ExprType.Type.UNTYPED_LIT && et1.getType() != ExprType.Type.TYPED_LIT)
          reportWarning("type mismatch in expression '" + expr.toStringTree() + "': parameter" +
                        " is not comparable to " + et1);

        addParam(ex2, et1);
        return;
      }

      // assume unknown type is compatible with anything
      if (et1 == null || et2 == null)
        return;

      ExprType.Type t1 = et1.getType();
      ExprType.Type t2 = et2.getType();

      // same type (TODO: we should probably check class compatibility too)
      if (t1 == t2 &&
            (t1 == ExprType.Type.URI || t1 == ExprType.Type.UNTYPED_LIT ||
             t1 == ExprType.Type.CLASS && et1.getMeta().equals(et2.getMeta()) ||
             t1 == ExprType.Type.EMB_CLASS && et1.getMeta().equals(et2.getMeta()) &&
               et1.getEmbeddedFields().equals(et2.getEmbeddedFields()) ||
             t1 == ExprType.Type.TYPED_LIT && et1.getDataType().equals(et2.getDataType())))
        return;

      // compatible type
      if (t1 == ExprType.Type.URI && (t2 == ExprType.Type.CLASS || t2 == ExprType.Type.EMB_CLASS) ||
          (t1 == ExprType.Type.CLASS || t1 == ExprType.Type.EMB_CLASS) && t2 == ExprType.Type.URI)
        return;

      // nope, they won't match
      reportWarning("type mismatch in expression '" + expr.toStringTree() + "': " + et1 +
                    " is not comparable to " + et2);
    }

    private static boolean isBinaryCompare(String fname) {
      for (String f : binaryComparisonFuncs) {
        if (f.equals(fname))
          return true;
      }
      return false;
    }

    private String expandAlias(String uri) {
      return sessFactory.expandAlias(uri.substring(1, uri.length() - 1));
    }
}


query
    : #(SELECT #(FROM fclause) #(WHERE wclause) #(PROJ sclause) (oclause)? (lclause)? (tclause)?) {
        for (QueryFunction qf : qfs)
          qf.postPredicatesHook((OqlAST) query_AST_in, (OqlAST) #query);
      }
    ;


fclause
    : #(COMMA fclause fclause)
    | cls:ID var:ID  { addVar(#var, #cls); }
    ;


sclause
{ ExprType type; }
    :   #(COMMA sclause sclause)
    |   (var:ID)? type=e:pexpr! {
          // check user variable exists, or create one
          if (#var != null)
            ;
          else if (#e.getType() == REF && #e.getNumberOfChildren() == 1)
            astFactory.addASTChild(currentAST, #var = astFactory.dup(#e.getFirstChild()));
          else
            astFactory.addASTChild(currentAST, #var = nextVar());
          checkProjVar(#var.getText());

          // remember the variable's type
          updateAST(#var, null, type, null, true);

          // don't forget our expression
          astFactory.addASTChild(currentAST, #e);
        }
    ;

pexpr returns [ExprType type = null]
    :   #(SUBQ query)
    |   type=factor[true, false]
    ;


wclause
    : (expr[false])?
    ;

expr[boolean isProj]
{ ExprType type, type2; }
    : #(AND (expr[isProj])+)
    | #(OR  (expr[isProj])+)
    | #(ASGN ID type=factor[isProj, false]) {
        vars.put(#ID.getText(), type);
        updateAST(#ID, null, type, null, true);
      }
    | #(EQ type=e1:factor[isProj, false] type2=e2:factor[isProj, false]) {
        checkTypeCompatibility(type, type2, #e1, #e2, #expr);
      }
    | #(NE type=n1:factor[isProj, false] type2=n2:factor[isProj, false]) {
        checkTypeCompatibility(type, type2, #n1, #n2, #expr);
      }
    | factor[isProj, true]
    ;

factor[boolean isProj, boolean isComp] returns [ExprType type = null]
    : QSTRING ((DHAT t:URIREF) | (AT ID))? {
        type = (#t != null) ? ExprType.literalType(expandAlias(#t.getText()), null) :
                              ExprType.literalType(null);
      }
    | URIREF                 { type = ExprType.uriType(null); }
    | #(PARAM ID)
    | type=fcall[isProj, isComp]
    | #(REF (   v:ID                 { updateAST(#v, null, type = getTypeForVar(#v), null, true); }
              | type=c:cast[isProj]  { updateAST(#c, null, type, null, ((OqlAST) #c).isVar()); }
            )
            (   ! ID         { type = resolveField(currentAST, type, #ID); }
              | ! URIREF     { type = handlePredicate(currentAST, type, #URIREF); }
              | #(EXPR pv:ID { addVar(#pv, ExprType.uriType(null)); } (expr[isProj])?) { type = null; }
            )*
            (STAR)?
      )
    ;

fcall[boolean isProj, boolean isComp] returns [ExprType type = null]
{
  List<OqlAST>   args  = new ArrayList<OqlAST>();
  List<ExprType> types = new ArrayList<ExprType>();
  ExprType t;
}
    : #(FUNC fp:ID (COLON fn:ID)? (t=a:factor[isProj, false] { args.add((OqlAST) #a); types.add(t); })*) {
        String fname = #fp.getText() + (#fn != null ? ":" + #fn.getText() : "");

        QueryFunctionFactory qff = sessFactory.getQueryFunctionFactory(fname);
        if (qff == null)
          throw new RecognitionException("Unknown function '" + fname + "' in " +
                                         #fcall.toStringTree());

        QueryFunction qf = qff.createFunction(fname, args, types, sessFactory);
        if (isProj ^ (qf instanceof ProjectionFunction))
          throw new RecognitionException("function '" + fname + "' can only be used in the " +
                                         (isProj ? "where clause" : "projection-list"));
        if (isComp ^ (qf instanceof BooleanConditionFunction))
          throw new RecognitionException("function '" + fname + "' is " +
                                         (isComp ? "not " : "") + "a boolean function");

        if (args.size() == 2 && qf instanceof BooleanConditionFunction &&
            ((BooleanConditionFunction) qf).isBinaryCompare())
          checkTypeCompatibility(types.get(0), types.get(1), args.get(0), args.get(1), #fcall);

        qfs.add(qf);
        ((OqlAST) #fcall).setFunction(qf);
        type = qf.getReturnType();
      }
    ;

cast[boolean isProj] returns [ExprType type = null]
    : ! #(CAST f:factor[isProj, false] t:ID) {
        type = getTypeForClass(#t, "cast");
        #cast = #f;     // replace CAST node by casted expression
      }
    ;

oclause
    : #(ORDER (oitem)+)
    ;

oitem
    : var:ID (ASC|DESC)? {
        if (!prjs.contains(#var.getText()))
          throw new RecognitionException("Order item '" + #var.getText() + "' is not defined in " +
                                         "the select clause");
        ((OqlAST) #var).setIsVar(true);
      }
    ;

lclause
    : #(LIMIT NUM)
    ;

tclause
    : #(OFFSET NUM)
    ;

