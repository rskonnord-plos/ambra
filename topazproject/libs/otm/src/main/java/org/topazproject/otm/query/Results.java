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
import org.topazproject.otm.OtmException;

/** 
 * This holds the results from a query. It is structured similar to a jdbc ResultSet.
 * 
 * @author Ronald Tschalär
 */
public abstract class Results {
  private final String[] variables;
  protected Type[]   types;
  private final String[] warnings;

  protected int      pos = -1;
  protected boolean  eor = false;
  protected Object[] curRow;

  /** possible result types */
  public enum Type { CLASS, LITERAL, URI, SUBQ_RESULTS, UNKNOWN };

  /** a literal in a result */
  public static class Literal {
    private final String value;
    private final String lang;
    private final URI    dtype;

    /** 
     * Create a new literal instance. 
     * 
     * @param value  the literal's value
     * @param lang   the literal's language tag; may be null
     * @param dtype   the literal's datatype; may be null
     * @throws NullPointerException if <var>value</var> is null
     * @throws IllegalArgumentException if both <var>lang</var> and <var>dtype</var> are non-null
     */
    public Literal(String value, String lang, URI dtype) {
      if (value == null)
        throw new NullPointerException("value may not be null");
      if (lang != null && dtype != null)
        throw new IllegalArgumentException("only one of language or datatype may be given");

      this.value = value;
      this.lang  = lang;
      this.dtype = dtype;
    }

    /** 
     * Get the literal's value. 
     * 
     * @return the literal's value
     */
    public String getValue() {
      return value;
    }

    /** 
     * Get the literal's language tag. 
     * 
     * @return the literal's language tag, or null if there is none
     */
    public String getLanguage() {
      return lang;
    }

    /** 
     * Get the literal's datatype. 
     * 
     * @return the literal's datatype, or null if this is an untyped literal
     */
    public URI getDatatype() {
      return dtype;
    }
  }

  protected Results(String[] variables, Type[] types, String[] warnings) {
    if (variables.length != types.length)
      throw new IllegalArgumentException("the number variables does not match the number of " +

                                         "types: " + variables.length + " != " + types.length);
    this.variables = variables;
    this.types     = types;
    this.warnings  = (warnings != null && warnings.length > 0) ? warnings : null;
  }

  protected Results(String[] variables, String[] warnings) {
    this.variables = variables;
    this.warnings  = (warnings != null && warnings.length > 0) ? warnings : null;
    this.types = new Type[variables.length];
    Arrays.fill(this.types, Type.UNKNOWN);
  }

  /** 
   * Subclasses must do the work of loading a new row of results here. They are expected to
   * <ul>
   *   <li>Advance to the next row, or set <var>eor</var> if no more rows are available.</li>
   *   <li>populate <var>curRow</var> with the current row; alternatively the subclass may
   *       override {@link #getRow getRow()} and {@link #get(int) get(idx)} and provide the
   *       results at that time instead.</li>
   * </ul>
   * 
   * @throws OtmException 
   */
  protected abstract void loadRow() throws OtmException;

  /**
   * Get the list of warnings generated while processing the query.
   *
   * @return the warnings, or null if there were none
   */
  public String[] getWarnings() {
    return (warnings != null) ? warnings.clone() : null;
  }

  /** 
   * Position cursor before the first row. 
   * 
   * @throws OtmException 
   */
  public void beforeFirst() throws OtmException {
    pos = -1;
    eor = false;
  }

  /** 
   * Move the cursor to the next row. 
   * 
   * @return true if the cursor is on a valid row; false if we just hit the end
   * @throws OtmException if this is called after having returned false
   */
  public boolean next() throws OtmException {
    if (eor)
      throw new QueryException("already at end of results");

    pos++;
    loadRow();

    return !eor;
  }

  /** 
   * Return the current row number. The number is zero based. 
   * 
   * @return the current row number, or -1 if not on a valid row
   */
  public int getRowNumber() {
    if (pos < 0 || eor)
      return -1;
    return pos;
  }

  /** 
   * Get the list of variables (columns) in the answer. 
   * 
   * @return the list of variables
   */
  public String[] getVariables() {
    return variables;
  }

  /** 
   * Return in the index of the given variable. 
   * 
   * @param var the variable
   * @return the index
   * @throws OtmException if <var>var</var> is not a variable in this result 
   */
  public int findVariable(String var) throws OtmException {
    for (int idx = 0; idx < variables.length; idx++) {
      if (variables[idx].equals(var))
        return idx;
    }

    throw new QueryException("no variable named '" + var + "' in the result");
  }

  /** 
   * Get the column type of the current row. The value is undefined
   * if the cursor is not on a valid row.
   * 
   * @param var  the variable identifying the column
   * @return the type
   * @throws OtmException if the variable does not exist
   */
  public Type getType(String var) throws OtmException {
    return getType(findVariable(var));
  }

  /** 
   * Get the column type of the current row. The value is undefined
   * if the cursor is not on a valid row.
   * 
   * @param idx  the column for which to get the type
   * @return the type
   */
  public Type getType(int idx) {
    return types[idx];
  }

  public Object[] getRow() throws OtmException {
    return curRow.clone();
  }

  public Object get(String var) throws OtmException {
    return get(findVariable(var));
  }

  public Object get(int idx) throws OtmException {
    return curRow[idx];
  }

  public String getString(String var) throws OtmException {
    return getString(findVariable(var));
  }

  public String getString(int idx) throws OtmException {
    switch (types[idx]) {
      case CLASS:
        return get(idx).toString();
      case LITERAL:
        return ((Literal) get(idx)).getValue();
      case URI:
        return ((URI) get(idx)).toString();
      case SUBQ_RESULTS:
        throw new QueryException("cannot convert subquery result to a string");

      default:
        throw new Error("unknown type " + types[idx] + " encountered");
    }
  }

  public Literal getLiteral(String var) throws OtmException {
    return getLiteral(findVariable(var));
  }

  public Literal getLiteral(int idx) throws OtmException {
    switch (types[idx]) {
      case LITERAL:
        return (Literal) get(idx);

      case CLASS:
      case URI:
      case SUBQ_RESULTS:
        throw new QueryException("result object is not a literal; type=" + types[idx]);

      default:
        throw new Error("unknown type " + types[idx] + " encountered");
    }
  }

  public URI getURI(String var) throws OtmException {
    return getURI(findVariable(var));
  }

  public URI getURI(int idx) throws OtmException {
    switch (types[idx]) {
      case URI:
        return (URI) get(idx);

      case LITERAL:
      case CLASS:
      case SUBQ_RESULTS:
        throw new QueryException("result object is not a uri; type=" + types[idx]);

      default:
        throw new Error("unknown type " + types[idx] + " encountered");
    }
  }

  public Results getSubQueryResults(String var) throws OtmException {
    return getSubQueryResults(findVariable(var));
  }

  public Results getSubQueryResults(int idx) throws OtmException {
    switch (types[idx]) {
      case SUBQ_RESULTS:
        return (Results) get(idx);

      case LITERAL:
      case CLASS:
      case URI:
        throw new QueryException("result object is not a subquery result; type=" + types[idx]);

      default:
        throw new Error("unknown type " + types[idx] + " encountered");
    }
  }
}
