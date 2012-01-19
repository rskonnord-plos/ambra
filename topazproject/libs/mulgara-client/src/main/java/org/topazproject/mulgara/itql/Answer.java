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
 * This represents a list of answers to a list of commands. The number of answers equals the
 * number of commands sent. Each answer in the list is either a {@link QueryAnswer QueryAnswer}
 * or a {@link java.lang.String String}, depending on whether the command was a query or not.
 * 
 * @author Ronald Tschalär
 */
public class Answer extends AbstractAnswer {
  private final List answers;

  /** 
   * Parse the xml answer.
   * 
   * @param xml the xml answer
   * @throws AnswerException if an exception occurred parsing the query response
   */
  public Answer(String xml) throws AnswerException {
    answers = new ArrayList();

    Element root = XmlHelper.parseXml(xml).getDocumentElement();
    if (!root.getTagName().equals(ANSWER))
      throw new AnswerException("Unexpected response: '" + xml + "'");

    parse(root, xml);
  }

  /** 
   * The returned list of answers. Each element is either a {@link QueryAnswer QueryAnswer} or a
   * {@link java.lang.String String}. The list may be empty if no answers were returned.
   * 
   * @return the answers
   */
  public List getAnswers() {
    return answers;
  }

  protected void parseMessage(String msg) {
    answers.add(msg);
  }

  protected void parseQueryAnswer(Element query, GraphElementFactory gef)
      throws URISyntaxException, GraphElementFactoryException, AnswerException {
    answers.add(new QueryAnswer(query, gef));
  }

  /**
   * This represents an answer to a single iTQL query. It consists of a number of variables (the
   * "columns" in each row) and a number of "rows" (query matches).
   */
  public static class QueryAnswer extends AbstractQueryAnswer {
    private final List rows;

    protected QueryAnswer(Element query, GraphElementFactory gef)
        throws URISyntaxException, GraphElementFactoryException, AnswerException {
      rows = new ArrayList();
      parseQueryAnswer(query, gef);
    }

    protected void addRow(Object[] row) {
      rows.add(row);
    }

    protected Object parseVariable(Element v, GraphElementFactory gef)
        throws URISyntaxException, GraphElementFactoryException, AnswerException {
      if (v == null)
        return null;

      String res = v.getAttribute(RSRC_ATTR);
      if (res.length() > 0)
        return gef.createResource(new URI(res));

      if (v.hasAttribute(BNODE_ATTR))
        return gef.createResource();

      if (v.getFirstChild() instanceof Element)
        return new QueryAnswer(v, gef);

      return gef.createLiteral(XmlHelper.getText(v));
    }

    /** 
     * Get the list of variables in the answer. 
     * 
     * @return the list of variables
     */
    public String[] getVariables() {
      return variables;
    }

    /** 
     * Get the rows. Each row consists an array of {@link java.lang.Object Object}'s, where each
     * element is the value of the corresponding variable in the variables list. Each element in the
     * list can be one of: null, a {@link org.jrdf.graph.Literal Literal}, a {@link
     * org.jrdf.graph.URIReference URIReference}, a {@link org.jrdf.graph.BlankNode BlankNode}, or
     * a {@link QueryAnswer QueryAnswer} (in case of subqueries).
     * 
     * @return the list of rows
     */
    public List getRows() {
      return rows;
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
     * Helper method to retrieve value for the given variable.
     * 
     * @param row the row index
     * @param var the variable's name
     * @return the variable's value in the row
     */
    public Object getVar(int row, String var) {
      return ((Object[]) rows.get(row))[indexOf(var)];
    }
  }
}
