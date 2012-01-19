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

import java.net.URISyntaxException;

import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphElementFactoryException;

import org.w3c.dom.Element;

/**
 * This is a simplified version of {@link Answer Answer}. The returned rows contain just String's,
 * representing either a url or a literal's contents; results from subqueries are not supported;
 * blank nodes are returned as null's.
 * 
 * @author Ronald Tschalär
 */
public class StringAnswer extends Answer {
  public StringAnswer(String xml) throws AnswerException {
    super(xml);
  }

  protected void parseQueryAnswer(Element query, GraphElementFactory gef)
      throws URISyntaxException, GraphElementFactoryException, AnswerException {
    getAnswers().add(new StringQueryAnswer(query));
  }

  /**
   * This represents an answer to a single iTQL query. It consists of a number of variables (the
   * "columns" in each row) and a number of "rows" (query matches).
   */
  public static class StringQueryAnswer extends QueryAnswer {
    protected StringQueryAnswer(Element query)
        throws URISyntaxException, GraphElementFactoryException, AnswerException {
      super(query, null);
    }

    protected void parseRow(Element solution, GraphElementFactory gef)
        throws URISyntaxException, GraphElementFactoryException, AnswerException {
      String[] row = new String[variables.length];

      for (int idx = 0; idx < row.length; idx++)
        row[idx] =
            (String) parseVariable(XmlHelper.getOnlyChild(solution, variables[idx], null), gef);

      addRow(row);
    }

    protected Object parseVariable(Element v, GraphElementFactory gef) throws AnswerException {
      if (v == null)
        return null;

      String res = v.getAttribute(RSRC_ATTR);
      if (res.length() > 0)
        return res;

      if (v.hasAttribute(BNODE_ATTR))
        return null;

      if (v.getFirstChild() instanceof Element)
        throw new AnswerException("subquery results not supported");

      return XmlHelper.getText(v);
    }
  }
}
