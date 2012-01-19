/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.query;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import antlr.ASTFactory;
import antlr.RecognitionException;

import org.topazproject.otm.SessionFactory;

/** 
 * Binary compare functions. This currently implements gt, ge, lt, and le.
 *
 * @author Ronald Tschalär
 */
class BinaryCompare implements BooleanConditionFunction, ConstraintsTokenTypes {
  private static final String   XSD        = "http://www.w3.org/2001/XMLSchema#";
  private static final String[] DATE_TYPES = {
      XSD + "date", XSD + "dateTime", XSD + "gYear", XSD + "gYearMonth",
  };
  private static final String[] NUM_TYPES = {
      XSD + "decimal", XSD + "float", XSD + "double", XSD + "integer", XSD + "nonPositiveInteger",
      XSD + "negativeInteger", XSD + "long", XSD + "int", XSD + "short", XSD + "byte",
      XSD + "nonNegativeInteger", XSD + "unsignedLong", XSD + "unsignedInt",
      XSD + "unsignedShort", XSD + "unsignedByte", XSD + "positiveInteger",
  };
  private static final URI      XSD_MODEL_TYPE =
                                        URI.create("http://mulgara.org/mulgara#XMLSchemaModel");
  private static final URI      STR_MODEL_TYPE =
                                        URI.create("http://topazproject.org/models#StringCompare");

  static {
    Arrays.sort(DATE_TYPES);
    Arrays.sort(NUM_TYPES);
  }

  /** the function name */
  protected final String         name;
  /** the current session-factory */
  protected final SessionFactory sf;

  /** 
   * Create a new binary-compare function instance. 
   * 
   * @param name  the function name
   * @param args  the arguments to the function
   * @param types the argument types
   * @param sf    the current session-factory
   * @throws RecognitionException if the number of arguments is not 2 or if the argument's types
   *                              are not identical
   */
  protected BinaryCompare(String name, List<OqlAST> args, List<ExprType> types, SessionFactory sf)
      throws RecognitionException {
    this.name = name;
    this.sf   = sf;

    // check argument type compatiblity
    if (args.size() != 2)
      throw new RecognitionException(name + "() must have exactly 2 arguments, but found " +
                                     args.size());

    // check argument type compatiblity
    ExprType ltype = types.get(0);
    ExprType rtype = types.get(1);

    if (ltype != null && rtype != null) {
      ExprType.Type lt = ltype.getType();
      ExprType.Type rt = rtype.getType();

      if (lt == ExprType.Type.CLASS || lt == ExprType.Type.EMB_CLASS ||
          rt == ExprType.Type.CLASS || rt == ExprType.Type.EMB_CLASS)
        throw new RecognitionException(name + "(): can't compare class types");

      if (lt != rt)
        throw new RecognitionException(name + "(): left and right side are not of same type: " +
                                       lt + " != " + rt);
      if (lt == ExprType.Type.TYPED_LIT && !ltype.getDataType().equals(rtype.getDataType()))
        throw new RecognitionException(name + "(): left and right side are not of same " +
                                       "data-type: " + ltype.getDataType() + " != " +
                                       rtype.getDataType());
    }
  }

  public String getName() {
    return name;
  }

  public ExprType getReturnType() {
    return null;
  }

  public boolean isBinaryCompare() {
    return true;
  }

  public void postPredicatesHook(OqlAST pre, OqlAST post) {
  }

  public OqlAST toItql(List<OqlAST> args, List<OqlAST> vars, OqlAST resVar, ASTFactory af)
      throws RecognitionException {
    return toItql(args.get(0), vars.get(0), args.get(1), vars.get(1), af);
  }

  /**
   * Implements gt, ge, lt, and le.
   *
   * @param larg the left argument
   * @param lvar the left argument's result variable
   * @param rarg the right argument
   * @param rvar the right argument's result variable
   * @param af   the ast-factory to use
   */
  protected OqlAST toItql(OqlAST larg, OqlAST lvar, OqlAST rarg, OqlAST rvar, ASTFactory af)
      throws RecognitionException {
    // create expression
    String pred, iprd;
    URI    modelType;
    if (isDate(lvar)) {
      pred  = (name.charAt(0) == 'l') ? "<mulgara:before>" : "<mulgara:after>";
      iprd  = (name.charAt(0) != 'l') ? "<mulgara:before>" : "<mulgara:after>";
      modelType = XSD_MODEL_TYPE;
    } else if (isNum(lvar)) {
      pred  = (name.charAt(0) == 'l') ? "<mulgara:lt>" : "<mulgara:gt>";
      iprd  = (name.charAt(0) != 'l') ? "<mulgara:lt>" : "<mulgara:gt>";
      modelType = XSD_MODEL_TYPE;
    } else {
      pred  = (name.charAt(0) == 'l') ? "<topaz:lt>" : "<topaz:gt>";
      iprd  = (name.charAt(0) != 'l') ? "<topaz:lt>" : "<topaz:gt>";
      modelType = STR_MODEL_TYPE;
    }

    String modelUri = ASTUtil.getModelUri(modelType, sf);

    OqlAST res = ASTUtil.makeTree(af, AND, "and", af.dupTree(larg), af.dupTree(rarg));
    if (name.equals("ge") || name.equals("le"))
      /* do this when mulgara supports <mulgara:equals>
      res.addChild(ASTUtil.makeTree(af, OR, "or",
                                    ASTUtil.makeTriple(lvar, pred, rvar, modelUri, af),
                                    ASTUtil.makeTriple(lvar, "<mulgara:equals>", rvar, af)));
      */
      /* this requires the functions (minus) to support variables on both sides
      res = ASTUtil.makeTree(af, MINUS, "minus", res,
                             ASTUtil.makeTriple(lvar, iprd, rvar, modelUri, af));
      */
      res = ASTUtil.makeTree(af, MINUS, "minus", af.dupTree(res),
              ASTUtil.makeTree(af, AND, "and", res,
                               ASTUtil.makeTriple(lvar, iprd, rvar, modelUri, af)));
    else
      res.addChild(ASTUtil.makeTriple(lvar, pred, rvar, modelUri, af));

    return res;
  }

  protected static boolean isDate(OqlAST var) {
    ExprType type = var.getExprType();
    return (type != null) && (type.getType() == ExprType.Type.TYPED_LIT) &&
           isDt(type.getDataType(), DATE_TYPES);
  }

  protected static boolean isNum(OqlAST var) {
    ExprType type = var.getExprType();
    return (type != null) && (type.getType() == ExprType.Type.TYPED_LIT) &&
           isDt(type.getDataType(), NUM_TYPES);
  }

  private static boolean isDt(String v, String[] list) {
    return Arrays.binarySearch(list, v) >= 0;
  }
}
