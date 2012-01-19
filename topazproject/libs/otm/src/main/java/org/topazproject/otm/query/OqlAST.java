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

import org.topazproject.otm.ClassMetadata;
import antlr.CommonAST;
import antlr.collections.AST;

/** 
 * This extends the normal AST to be able to store OTM meta data with each node.
 * 
 * @author Ronald Tschalär
 */
public class OqlAST extends CommonAST {
  private ExprType type;
  private String   model;
  private boolean  isVar;
  private boolean  isInv;

  public void initialize(AST t) {
    super.initialize(t);

    if (t instanceof OqlAST) {
      OqlAST o  = (OqlAST) t;
      type      = o.type;
      model     = o.model;
      isVar     = o.isVar;
      isInv     = o.isInv;
    }
  }

  /**
   * Get the expression type.
   *
   * @return the type.
   */
  public ExprType getExprType() {
    return type;
  }

  /**
   * Set the expression type.
   *
   * @param type the type.
   */
  public void setExprType(ExprType type) {
    this.type = type;
  }

  /**
   * Get model.
   *
   * @return model as String.
   */
  public String getModel()
  {
      return model;
  }

  /**
   * Set model.
   *
   * @param model the value to set.
   */
  public void setModel(String model)
  {
      this.model = model;
  }

  /**
   * Whether this node represents a variable.
   *
   * @return true if this is a variable
   */
  public boolean isVar() {
    return isVar;
  }

  /**
   * Set whether this node represents a variable.
   *
   * @param isVar true if this is a variable
   */
  public void setIsVar(boolean isVar) {
    this.isVar = isVar;
  }

  /**
   * Whether this node represents an inverse predicate.
   *
   * @return true if this is an inverse predicate
   */
  public boolean isInverse() {
    return isInv;
  }

  /**
   * Set the inverse flag.
   *
   * @param isInv true if this is an inverse predicate
   */
  public void setIsInverse(boolean isInv) {
    this.isInv = isInv;
  }
}
