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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphElementFactoryException;

import org.w3c.dom.Element;

/**
 * This represents the answers to a list of commands as a result-set (modelled after JDBC's
 * ResultSet). The number of answers equals the number of commands sent.
 * 
 * @author Ronald Tschalär
 */
public class AnswerSet extends AbstractAnswer {
  private final List answers;
  private       int  curPos = -1;

  /** 
   * Parse the xml answer.
   * 
   * @param xml the xml answer
   * @throws AnswerException if an exception occurred parsing the query response
   */
  public AnswerSet(String xml) throws AnswerException {
    answers = new ArrayList();

    Element root = XmlHelper.parseXml(xml).getDocumentElement();
    if (!root.getTagName().equals(ANSWER))
      throw new AnswerException("Unexpected response: '" + xml + "'");

    parse(root, xml);
  }

  protected void parseMessage(String msg) {
    answers.add(msg);
  }

  protected void parseQueryAnswer(Element query, GraphElementFactory gef)
      throws URISyntaxException, GraphElementFactoryException, AnswerException {
    answers.add(new QueryAnswerSet(query, gef));
  }

  /** 
   * Reset the current position to before the first answer. 
   */
  public void beforeFirst() {
    curPos = -1;
  }

  /** 
   * Move the current position to the next answer. 
   *
   * @return true if the new current answer is valid, false if there are no more answers
   */
  public boolean next() {
    curPos++;
    return (curPos < answers.size());
  }

  /** 
   * @return true if the current answer is a query-result, false if it's a message
   */
  public boolean isQueryResult() {
    return (answers.get(curPos) instanceof QueryAnswerSet);
  }

  /** 
   * Get the message. 
   * 
   * @return the message, or null if the current answer is a query-result
   */
  public String getMessage() {
    Object x = answers.get(curPos);
    return (x instanceof String) ? (String) x : null;
  }

  /** 
   * Get the query results. 
   * 
   * @return the query results, or null if the current answer is a message
   */
  public QueryAnswerSet getQueryResults() {
    Object x = answers.get(curPos);
    return (x instanceof QueryAnswerSet) ? (QueryAnswerSet) x : null;
  }

  /**
   * This represents an answer to a single iTQL query.
   */
  public static class QueryAnswerSet extends AbstractQueryAnswer {
    private final List rows;
    private       int  curPos = -1;

    protected QueryAnswerSet(Element query, GraphElementFactory gef) throws AnswerException {
      rows = new ArrayList();
      try {
        parseQueryAnswer(query, gef);
      } catch (URISyntaxException use) {
        throw new AnswerException("Impossible", use);   // can't happen
      } catch (GraphElementFactoryException gefe) {
        throw new AnswerException("Impossible", gefe);  // can't happen
      }
    }

    protected void addRow(Object[] row) {
      rows.add(row);
    }

    protected Object parseVariable(Element v, GraphElementFactory gef) {
      return v;
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
     * @return the index, or -1 if <var>var</var> is not a variable in this answer
     */
    public int indexOf(String var) {
      for (int idx = 0; idx < variables.length; idx++) {
        if (variables[idx].equals(var))
          return idx;
      }

      return -1;
    }

    /** 
     * Reset the current position to before the first answer. 
     */
    public void beforeFirst() {
      curPos = -1;
    }

    /** 
     * Move the current position to the next answer. 
     *
     * @return true if the new current answer is valid, false if there are no more answers
     */
    public boolean next() {
      curPos++;
      return (curPos < rows.size());
    }

    /** 
     * Tests if the value of the specified column in the current row is a Literal.
     * 
     * @param idx the column index (0-based)
     * @return true if the column has a non-null value and is a Literal
     */
    public boolean isLiteral(int idx) {
      Element v = (Element) ((Object[]) rows.get(curPos))[idx];
      return (v != null) && !v.hasAttribute(RSRC_ATTR) && !(v.getFirstChild() instanceof Element)
                && !v.hasAttribute(BNODE_ATTR);
    }

    /** 
     * Get the data type of the specified Literal column in the current row as a String.
     * 
     * @param idx the column index (0-based)
     * @return the data type URI or null for untyped
     * @throws AnswerException if the column refers to a non Literal
     */
    public String getLiteralDataType(int idx) throws AnswerException {
      if (!isLiteral(idx))
        throw new AnswerException("is not a literal");

      Element v = (Element) ((Object[]) rows.get(curPos))[idx];
      String res = v.getAttribute("datatype");
      return (res.length() > 0) ? res : null;
    }

    /** 
     * Get the value of the specified column in the current row as a String.
     * 
     * @param idx the column index (0-based)
     * @return if the value is a URI then the URI as a string; if the value is literal then the
     *         literal's value; if the value is a blank-node then null.
     * @throws AnswerException if the column refers to a subquery answer
     */
    public String getString(int idx) throws AnswerException {
      Element v = (Element) ((Object[]) rows.get(curPos))[idx];

      if (v == null)
        return null;

      String res = v.getAttribute(RSRC_ATTR);
      if (res.length() > 0)
        return res;

      if (v.hasAttribute(BNODE_ATTR))
        return null;

      if (v.getFirstChild() instanceof Element)
        throw new AnswerException("is a subquery result");

      return XmlHelper.getText(v);
    }

    /** 
     * Get the value for the specified variable in the current row as a String.
     * 
     * @param var name of the variable
     * @return if the value is a URI then the URI as a string; if the value is literal then the
     *         literal's value; if the value is a blank-node then null.
     * @throws AnswerException if the column refers to a subquery answer
     */
    public String getString(String  var) throws AnswerException {
      return getString(indexOf(var));
    }

    /** 
     * Tests if the value of the specified column in the current row a URI.
     * 
     * @param idx the column index (0-based)
     * @return true if the column has a non-null value and is a URI 
     */
    public boolean isURI(int idx) {
      Element v = (Element) ((Object[]) rows.get(curPos))[idx];
      return (v != null) && v.hasAttribute(RSRC_ATTR);
    }

    /** 
     * Get the value of the specified column in the current row as a URI.
     * 
     * @param idx the column index (0-based)
     * @return the URI
     * @throws AnswerException if the value isn't a URI
     */
    public URI getURI(int idx) throws AnswerException {
      Element v = (Element) ((Object[]) rows.get(curPos))[idx];

      if (v == null)
        return null;

      String res = v.getAttribute(RSRC_ATTR);
      if (res.length() > 0)
        return URI.create(res);

      throw new AnswerException("is not a URI");
    }

    /** 
     * Get the value for the specified variable in the current row as a URI.
     * 
     * @param var name of the variable
     * @return the URI
     * @throws AnswerException if the value isn't a URI
     */
    public URI getURI(String var) throws AnswerException {
      return getURI(indexOf(var));
    }

    /** 
     * Tests if the value of the specified column in the current row a blank node.
     * 
     * @param idx the column index (0-based)
     * @return true if the column has a non-null value and is a blank node 
     */
    public boolean isBlankNode(int idx) {
      Element v = (Element) ((Object[]) rows.get(curPos))[idx];
      return (v != null) && v.hasAttribute(BNODE_ATTR);
    }

    /** 
     * Get the id of the blank node. This id may be temporary and internal and is only valid
     * within the current answer-set.
     * 
     * @param idx the column index (0-based)
     * @return the blank node's id
     * @throws AnswerException if the value isn't a blank node
     */
    public String getBlankNode(int idx) throws AnswerException {
      Element v = (Element) ((Object[]) rows.get(curPos))[idx];

      if (v == null)
        return null;

      String res = v.getAttribute(BNODE_ATTR);
      if (res.length() > 0)
        return res;

      throw new AnswerException("is not a blank node");
    }

    /** 
     * Get the id of the blank node. This id may be temporary and internal and is only valid
     * within the current answer-set.
     * 
     * @param var name of the variable
     * @return the blank node's id
     * @throws AnswerException if the value isn't a blank node
     */
    public String getBlankNode(String var) throws AnswerException {
      return getBlankNode(indexOf(var));
    }

    /** 
     * Tests if the value of the specified column in the current row is a subquery result.
     * 
     * @param idx the column index (0-based)
     * @return true if the column has a non-null value and is a URI 
     */
    public boolean isSubQueryResults(int idx) {
      Element v = (Element) ((Object[]) rows.get(curPos))[idx];
      return (v != null) && (v.getFirstChild() instanceof Element);
    }

    /** 
     * Get the subquery result at the specified column in the current row.
     * 
     * @param idx the column index (0-based)
     * @return the result
     * @throws AnswerException if the value isn't a subquery result
     */
    public QueryAnswerSet getSubQueryResults(int idx) throws AnswerException {
      Element v = (Element) ((Object[]) rows.get(curPos))[idx];

      if (v == null)
        return null;

      if (v.getFirstChild() instanceof Element)
        return new QueryAnswerSet(v, null);

      throw new AnswerException("is not a sub-query answer");
    }

    /** 
     * Get the subquery result for the specified variable in the current row.
     * 
     * @param var name of the variable
     * @return the result
     * @throws AnswerException if the value isn't a subquery result
     */
    public QueryAnswerSet getSubQueryResults(String var) throws AnswerException {
      return getSubQueryResults(indexOf(var));
    }
  }
}
