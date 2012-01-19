/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
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
    assert !from.getFirstChild().getText().equals("comma");

    AST where = from.getNextSibling();
    assert where.getText().equals("where");

    this.filterDef = where.getFirstChild();
    this.classVar  = from.getFirstChild().getNextSibling().getText();
    this.filters   = null;
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
