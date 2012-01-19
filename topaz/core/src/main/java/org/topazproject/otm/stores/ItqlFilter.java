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

package org.topazproject.otm.stores;

import java.util.Set;

import org.topazproject.otm.Filter;
import org.topazproject.otm.filter.ConjunctiveFilterDefinition;
import org.topazproject.otm.filter.JunctionFilterDefinition;

import antlr.collections.AST;

/** 
 * This holds the information representing a parsed filter for iTQL.
 *
 * @author Ronald Tschalär
 */
public class ItqlFilter {
  public static enum Type { PLAIN, AND, OR };

  private final String          filteredClass;
  private final Type            type;
  private final AST             filterDef;
  private final String          classVar;
  private final Set<ItqlFilter> filters;

  /** 
   * Create a new plain filter-info instance. 
   * 
   * @param f           the original filter this is for
   * @param parsedQuery the preparsed filter expression, as returned by ItqlConstraingGenerator
   */
  ItqlFilter(Filter f, AST parsedQuery) {
    this.filteredClass = f.getFilterDefinition().getFilteredClass();
    this.type = Type.PLAIN;

    AST from = parsedQuery.getFirstChild();
    assert from.getText().equals("from");

    AST where = from.getNextSibling();
    assert where.getText().equals("where");

    AST select = where.getNextSibling();
    assert select.getText().equals("projection");
    assert !select.getFirstChild().getText().equals("comma");

    this.filterDef = where.getFirstChild();
    this.classVar  = getFirstProjExpr(select).getText();
    this.filters   = null;
  }

  private static AST getFirstProjExpr(AST select) {
    AST ast = select.getFirstChild().getNextSibling();
    return (ast.getNextSibling() != null) ? ast.getNextSibling() : ast;
  }

  /** 
   * Create a new junction filter-info instance. 
   * 
   * @param jf      the original junction filter this is for
   * @param filters the component filters
   */
  ItqlFilter(JunctionFilterDefinition.JunctionFilter jf, Set<ItqlFilter> filters) {
    this.filteredClass = jf.getFilterDefinition().getFilteredClass();
    this.type = (jf instanceof ConjunctiveFilterDefinition.ConjunctiveFilter) ? Type.AND : Type.OR;
    this.filters   = filters;
    this.filterDef = null;
    this.classVar  = null;
  }

  /** 
   * @return the entity name or fully-qualified class name of the class being filtered
   */
  public String getFilteredClass() {
    return filteredClass;
  }

  /** 
   * @return the type of filter this represents
   */
  public Type getType() {
    return type;
  }

  /** 
   * @return the where clause of preparsed filter expression, as returned by
   *         ItqlConstraingGenerator, or null if this has a junction-type
   */
  public AST getDef() {
    return filterDef;
  }

  /** 
   * @return the variable in the filter expression representing the filtered class, or null if this
   *         has a junction-type
   */
  public String getVar() {
    return classVar;
  }

  /** 
   * @return the component filters, or null if this is a plain filter
   */
  public Set<ItqlFilter> getFilters() {
    return filters;
  }
}
