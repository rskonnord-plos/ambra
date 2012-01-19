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

package org.topazproject.otm.query;

import java.util.HashSet;
import java.util.Set;

import antlr.CommonAST;
import antlr.collections.AST;

/**
 * This extends the normal AST to be able to store OTM meta data with each node.
 *
 * @author Ronald Tschalär
 */
public class OqlAST extends CommonAST {
  private ExprType               type;
  private String                 propName;
  private String                 graph;
  private boolean                isVar;
  private boolean                isInv;
  private QueryFunction          func;
  private Set<TransformListener> listeners;

  public void initialize(AST t) {
    super.initialize(t);

    if (t instanceof OqlAST) {
      OqlAST o  = (OqlAST) t;
      type      = o.type;
      propName  = o.propName;
      graph     = o.graph;
      isVar     = o.isVar;
      isInv     = o.isInv;
      func      = o.func;
      listeners = o.listeners;
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
   * Get the property-name.
   *
   * @return the property-name
   */
  public String getPropName() {
      return propName;
  }

  /**
   * Set the property-name.
   *
   * @param propName the value to set.
   */
  public void setPropName(String propName) {
      this.propName = propName;
  }

  /**
   * Get the graph.
   *
   * @return graph as String.
   */
  public String getGraph() {
      return graph;
  }

  /**
   * Set the graph.
   *
   * @param graph the value to set.
   */
  public void setGraph(String graph) {
      this.graph = graph;
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

  /**
   * Get the associated function.
   *
   * @return the function.
   */
  public QueryFunction getFunction() {
    return func;
  }

  /**
   * Set the associated function.
   *
   * @param func the function.
   */
  public void setFunction(QueryFunction func) {
    this.func = func;
  }

  /**
   * Get the registered transform listeners.
   *
   * @return the listeners; may be null.
   */
  public Set<TransformListener> getListeners() {
    return listeners;
  }

  /**
   * Add a transform listener.
   *
   * @param listener the listener to add.
   */
  public void addListener(TransformListener listener) {
    if (listeners == null)
      listeners = new HashSet<TransformListener>();
    listeners.add(listener);
  }
}
