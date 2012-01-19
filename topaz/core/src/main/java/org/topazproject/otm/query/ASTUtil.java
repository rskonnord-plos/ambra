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
import java.util.List;

import antlr.ASTFactory;
import antlr.RecognitionException;
import antlr.collections.AST;

import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.SessionFactory;

/** 
 * @author Ronald Tschalär
 */
public class ASTUtil implements ConstraintsTokenTypes {
  private ASTUtil() {
  }

  public static OqlAST makeID(Object obj, ASTFactory af) {
    if (obj instanceof OqlAST) {
      OqlAST ast = (OqlAST) af.dup((OqlAST) obj);
      if (ast.getType() != ID)
        ast.setType(ID);
      return ast;
    }

    return (OqlAST) af.create(ID, (String) obj);
  }

  public static OqlAST makeTriple(Object s, Object p, Object o, ASTFactory af) {
    return makeTriple(s, p, o, (p instanceof OqlAST) ? ((OqlAST) p).getModel() : null, af);
  }

  public static OqlAST makeTriple(Object s, Object p, Object o, Object m, ASTFactory af) {
    OqlAST sa = makeID(s, af);
    OqlAST pa = makeID(p, af);
    OqlAST oa = makeID(o, af);
    OqlAST ma = (m != null) ? makeID(m, af) : null;

    AST tr = af.create(TRIPLE, "triple");
    if (pa.isInverse())
      return (OqlAST) af.make(new AST[] { tr, oa, pa, sa, ma });
    else
      return (OqlAST) af.make(new AST[] { tr, sa, pa, oa, ma });
  }

  public static String getModelUri(String modelId, SessionFactory sf) throws RecognitionException {
    ModelConfig mc = sf.getModel(modelId);
    if (mc == null)
      throw new RecognitionException("Unable to find model '" + modelId + "'");
    return mc.getUri().toString();
  }

  public static String getModelUri(URI modelType, SessionFactory sf) throws RecognitionException {
    List<ModelConfig> mc = sf.getModels(modelType);
    if (mc == null)
      throw new RecognitionException("Unable to find a model of type '" + modelType +
                                     "' - please make sure you've created and configured a model" +
                                     " for this type");
    return mc.get(0).getUri().toString();
  }

  public static OqlAST makeTree(ASTFactory af, int rootType, String rootName, AST... children) {
    AST[] nodes = new AST[children.length + 1];
    nodes[0] = af.create(rootType, rootName);
    System.arraycopy(children, 0, nodes, 1, children.length);
    return (OqlAST) af.make(nodes);
  }
}
