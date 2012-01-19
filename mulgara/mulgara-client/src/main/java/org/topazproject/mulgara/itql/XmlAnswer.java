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
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** 
 * This parses an XML representation of an answer.
 * 
 * @author Ronald Tschalär
 */
class XmlAnswer extends AbstractAnswer {
  private static final String ANSWER     = "answer";
  private static final String QUERY      = "query";
  private static final String MESSAGE    = "message";

  private static final String VARS       = "variables";
  private static final String SOLUTION   = "solution";
  private static final String RSRC_ATTR  = "resource";
  private static final String BNODE_ATTR = "blank-node";

  private List<Element[]> rows;
  private int             curPos = -1;

  /** 
   * Parse the xml answer.
   * 
   * @param xml the xml answer
   * @return the list of parsed answers
   * @throws AnswerException if an exception occurred while parsing the query response
   */
  public static List<Answer> parseAnswer(String xml) throws AnswerException {
    Element root = XmlHelper.parseXml(xml).getDocumentElement();
    if (!root.getTagName().equals(ANSWER))
      throw new AnswerException("Unexpected response: '" + xml + "'");

    return parseAnswer(root);
  }

  /**
   * Parse an answer. This has the following structure:
   *
   * <pre>
   * &lt;answer xmlns="http://tucana.org/tql#"&gt;
   *   &lt;query&gt;...&lt;/query&gt;
   *   &lt;query&gt;...&lt;/query&gt;
   *   ...
   * &lt;/answer&gt;
   * </pre>
   */
  private static List<Answer> parseAnswer(Element ansElem) throws AnswerException {
    List<Answer> res = new ArrayList<Answer>();

    for (Element q = XmlHelper.getFirstChild(ansElem, QUERY); q != null;
         q = XmlHelper.getNextSibling(q, QUERY))
      res.add(new XmlAnswer(q));

    return res;
  }

  /**
   * Create a new xml-answer from the given parsed xml doc.
   *
   * @param query the "query" element
   */
  private XmlAnswer(Element query) throws AnswerException {
    Element first = XmlHelper.getFirstChild(query, "*");
    if (first != null && first.getTagName().equals(MESSAGE))
      parseMessage(first);
    else
      parseQueryAnswer(query);
  }

  /**
   * Parse a message response to a single non-query command. The structure is
   *
   * <pre>
   *   &lt;query&gt;
   *     &lt;message&gt;blah blah&lt;/message&gt;
   *   &lt;/query&gt;
   * </pre>
   */
  private void parseMessage(Element message) {
    this.message = XmlHelper.getText(message);
  }

  /**
   * Parse an answer to a single query command. The structure is
   *
   * <pre>
   *   &lt;query&gt;
   *     &lt;variables&gt;...&lt;/variables&gt;
   *     &lt;solution&gt;...&lt;/solution&gt;
   *     &lt;solution&gt;...&lt;/solution&gt;
   *     ...
   *   &lt;/query&gt;
   * </pre>
   */
  private void parseQueryAnswer(Element query) throws AnswerException {
    Element varsElem = XmlHelper.getFirstChild(query, VARS);
    if (varsElem == null)
      throw new IllegalArgumentException("could not parse query element - no variables found");

    NodeList varElems = XmlHelper.getChildren(varsElem, "*");
    variables = new String[varElems.getLength()];
    for (int idx = 0; idx < varElems.getLength(); idx++)
      variables[idx] = varElems.item(idx).getNodeName();

    rows = new ArrayList<Element[]>();

    for (Element sol = XmlHelper.getFirstChild(query, SOLUTION); sol != null;
         sol = XmlHelper.getNextSibling(sol, SOLUTION)) {
      parseRow(sol);
    }
  }

  /** 
   * Parse a single solution (row).
   *
   * @param solution the solution element
   */
  private void parseRow(Element solution) throws AnswerException {
    Element[] row = new Element[variables.length];

    for (int idx = 0; idx < row.length; idx++)
      row[idx] = XmlHelper.getOnlyChild(solution, variables[idx], null);

    rows.add(row);
  }

  /**********************************************************************************/

  public void beforeFirst() {
    curPos = -1;
  }

  public boolean next() {
    curPos++;
    return (rows != null && curPos < rows.size());
  }

  public void close() {
  }

  public boolean isLiteral(int idx) {
    Element v = rows.get(curPos)[idx];
    return (v != null) && !v.hasAttribute(RSRC_ATTR) && !(v.getFirstChild() instanceof Element) &&
           !v.hasAttribute(BNODE_ATTR);
  }

  public String getLiteralDataType(int idx) throws AnswerException {
    return getLiteralAttribute(idx, "datatype");
  }

  public String getLiteralLangTag(int idx) throws AnswerException {
    return getLiteralAttribute(idx, "language");
  }

  private String getLiteralAttribute(int idx, String attr) throws AnswerException {
    if (!isLiteral(idx))
      throw new AnswerException("is not a literal");

    Element v = rows.get(curPos)[idx];
    String res = v.getAttribute(attr);
    return (res.length() > 0) ? res : null;
  }

  public String getString(int idx) throws AnswerException {
    Element v = rows.get(curPos)[idx];

    if (v == null)
      return null;

    String res = v.getAttribute(RSRC_ATTR);
    if (res.length() > 0)
      return res;

    if (v.hasAttribute(BNODE_ATTR))
      return v.getAttribute(BNODE_ATTR);

    if (v.getFirstChild() instanceof Element)
      throw new AnswerException("is a subquery result");

    return XmlHelper.getText(v);
  }

  public boolean isURI(int idx) {
    Element v = rows.get(curPos)[idx];
    return (v != null) && v.hasAttribute(RSRC_ATTR);
  }

  public URI getURI(int idx) throws AnswerException {
    Element v = rows.get(curPos)[idx];

    if (v == null)
      return null;

    String res = v.getAttribute(RSRC_ATTR);
    if (res.length() > 0)
      return URI.create(res);

    throw new AnswerException("is not a URI");
  }

  public boolean isBlankNode(int idx) {
    Element v = rows.get(curPos)[idx];
    return (v != null) && v.hasAttribute(BNODE_ATTR);
  }

  public String getBlankNode(int idx) throws AnswerException {
    Element v = rows.get(curPos)[idx];

    if (v == null)
      return null;

    String res = v.getAttribute(BNODE_ATTR);
    if (res.length() > 0)
      return res;

    throw new AnswerException("is not a blank node");
  }

  public boolean isSubQueryResults(int idx) {
    Element v = rows.get(curPos)[idx];
    return (v != null) && (v.getFirstChild() instanceof Element);
  }

  public XmlAnswer getSubQueryResults(int idx) throws AnswerException {
    Element v = rows.get(curPos)[idx];

    if (v == null)
      return null;

    if (v.getFirstChild() instanceof Element)
      return new XmlAnswer(v);

    throw new AnswerException("is not a sub-query answer");
  }
}
