/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.mulgara.itql;

import java.net.URI;

/**
 * This represents the answer to an itql command as a result-set (modeled after JDBC's
 * ResultSet). It is either the answer to a non-query, in which case {@link #getMessage
 * getMessage()} will return a message describing the command completion, or it is the
 * answer to a query, in which case all other methods should be used.
 * 
 * @author Ronald Tschalär
 */
public interface Answer {
  /** 
   * Get the result message from a non-query command.
   * 
   * @return the message, or null if this is a query answer
   */
  public String getMessage();

  /** 
   * Get the list of variables (columns) in the query answer. 
   * 
   * @return the list of variables, or null if this is a non-query answer
   */
  public String[] getVariables();

  /** 
   * Return in the index of the given variable. 
   * 
   * @param var the variable
   * @return the index, or -1 if <var>var</var> is not a variable in this answer
   */
  public int indexOf(String var);

  /** 
   * Reset the current position to before the first row. 
   */
  public void beforeFirst() throws AnswerException;

  /** 
   * Move the current position to the next row.
   *
   * @return true if the new current row is valid, false if there are no more answers or
   *         this is a non-query answer
   */
  public boolean next() throws AnswerException;

  /** 
   * Close this answer.
   */
  public void close();

  /** 
   * Tests if the value of the specified column in the current row is a Literal.
   * 
   * @param idx the column index (0-based)
   * @return true if the column has a non-null value and is a Literal
   */
  public boolean isLiteral(int idx) throws AnswerException;

  /** 
   * Get the data type of the specified Literal column in the current row as a String.
   * 
   * @param idx the column index (0-based)
   * @return the data type URI or null for untyped
   * @throws AnswerException if the column refers to a non Literal
   */
  public String getLiteralDataType(int idx) throws AnswerException;

  /** 
   * Get the language tag of the specified Literal column in the current row as a String.
   * 
   * @param idx the column index (0-based)
   * @return the language tag, or null if there was none
   * @throws AnswerException if the column refers to a non Literal
   */
  public String getLiteralLangTag(int idx) throws AnswerException;

  /** 
   * Get the value of the specified column in the current row as a String.
   * 
   * @param idx the column index (0-based)
   * @return if the value is a URI then the URI as a string; if the value is literal then the
   *         literal's value; if the value is a blank-node then a temporary internal id.
   * @throws AnswerException if the column refers to a subquery answer
   */
  public String getString(int idx) throws AnswerException;

  /** 
   * Get the value for the specified variable in the current row as a String.
   * 
   * @param var name of the variable
   * @return if the value is a URI then the URI as a string; if the value is literal then the
   *         literal's value; if the value is a blank-node then null.
   * @throws AnswerException if the column refers to a subquery answer
   */
  public String getString(String  var) throws AnswerException;

  /** 
   * Tests if the value of the specified column in the current row a URI.
   * 
   * @param idx the column index (0-based)
   * @return true if the column has a non-null value and is a URI 
   */
  public boolean isURI(int idx) throws AnswerException;

  /** 
   * Get the value of the specified column in the current row as a URI.
   * 
   * @param idx the column index (0-based)
   * @return the URI
   * @throws AnswerException if the value isn't a URI
   */
  public URI getURI(int idx) throws AnswerException;

  /** 
   * Get the value for the specified variable in the current row as a URI.
   * 
   * @param var name of the variable
   * @return the URI
   * @throws AnswerException if the value isn't a URI
   */
  public URI getURI(String var) throws AnswerException;

  /** 
   * Tests if the value of the specified column in the current row a blank node.
   * 
   * @param idx the column index (0-based)
   * @return true if the column has a non-null value and is a blank node 
   */
  public boolean isBlankNode(int idx) throws AnswerException;

  /** 
   * Get the id of the blank node. This id may be temporary and internal and is only valid
   * within the current answer-set.
   * 
   * @param idx the column index (0-based)
   * @return the blank node's id
   * @throws AnswerException if the value isn't a blank node
   */
  public String getBlankNode(int idx) throws AnswerException;

  /** 
   * Get the id of the blank node. This id may be temporary and internal and is only valid
   * within the current answer-set.
   * 
   * @param var name of the variable
   * @return the blank node's id
   * @throws AnswerException if the value isn't a blank node
   */
  public String getBlankNode(String var) throws AnswerException;

  /** 
   * Tests if the value of the specified column in the current row is a subquery result.
   * 
   * @param idx the column index (0-based)
   * @return true if the column has a non-null value and is a URI 
   */
  public boolean isSubQueryResults(int idx) throws AnswerException;

  /** 
   * Get the subquery result at the specified column in the current row.
   * 
   * @param idx the column index (0-based)
   * @return the result
   * @throws AnswerException if the value isn't a subquery result
   */
  public Answer getSubQueryResults(int idx) throws AnswerException;

  /** 
   * Get the subquery result for the specified variable in the current row.
   * 
   * @param var name of the variable
   * @return the result
   * @throws AnswerException if the value isn't a subquery result
   */
  public Answer getSubQueryResults(String var) throws AnswerException;
}
