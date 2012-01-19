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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jrdf.graph.Literal;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.URIReference;

import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/** 
 * This wraps a mulgara Answer object.
 * 
 * @author Ronald Tschalär
 */
class AnswerAnswer extends AbstractAnswer {
  private static final Log  log = LogFactory.getLog(AbstractAnswer.class);

  private final Answer         ans;
  private final Object[]       rowCache;
  private final AnswerAnswer[] ansCache;
  private final boolean[]      needsClose;

  /** 
   * Create a query answer.
   * 
   * @param ans the mulgara answer
   */
  public AnswerAnswer(Answer ans) {
    this.ans = ans;

    Variable[] vars = ans.getVariables();
    variables = new String[vars.length];
    for (int idx = 0; idx < vars.length; idx++)
      variables[idx] = vars[idx].getName();

    this.rowCache   = new Object[vars.length];
    this.ansCache   = new AnswerAnswer[vars.length];
    this.needsClose = new boolean[vars.length];
  }

  /** 
   * Create a non-query answer.
   * 
   * @param msg the message
   */
  public AnswerAnswer(String msg) {
    this.ans        = null;
    this.rowCache   = null;
    this.ansCache   = null;
    this.needsClose = null;
    message         = msg;
  }

  /**********************************************************************************/

  public void beforeFirst() throws AnswerException {
    if (ans == null)
      return;

    try {
      clearRowCache();
      ans.beforeFirst();
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }
  }

  public boolean next() throws AnswerException {
    if (ans == null)
      return false;

    try {
      clearRowCache();
      return ans.next();
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }
  }

  public void close() {
    try {
      clearRowCache();
      ans.close();
    } catch (TuplesException te) {
      log.error("Error closing answer", te);
    }
  }

  private Object getObject(int idx, boolean nc) throws TuplesException {
    if (rowCache[idx] == null)
      rowCache[idx] = ans.getObject(idx);
    needsClose[idx] &= nc;
    return rowCache[idx];
  }

  private void clearRowCache() throws TuplesException {
    for (int idx = 0; idx < rowCache.length; idx++) {
      if (needsClose[idx] && rowCache[idx] instanceof Answer)
        ((Answer) rowCache[idx]).close();

      rowCache[idx]   = null;
      ansCache[idx]   = null;
      needsClose[idx] = true;
    }
  }

  public boolean isLiteral(int idx) throws AnswerException {
    try {
      return getObject(idx, true) instanceof Literal;
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }
  }

  public String getLiteralDataType(int idx) throws AnswerException {
    Object o;
    try {
      o = getObject(idx, true);
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }

    if (o instanceof Literal) {
      URI dt = ((Literal) o).getDatatypeURI();
      return (dt != null) ? dt.toString() : null;
    }

    throw new AnswerException("is not a Literal");
  }

  public String getLiteralLangTag(int idx) throws AnswerException {
    Object o;
    try {
      o = getObject(idx, true);
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }

    if (o instanceof Literal) {
      String lang = ((Literal) o).getLanguage();
      return (lang != null && lang.length() > 0) ? lang : null;
    }

    throw new AnswerException("is not a Literal");
  }

  public String getString(int idx) throws AnswerException {
    Object o;
    try {
      o = getObject(idx, true);
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }

    if (o instanceof Literal)
      return ((Literal) o).getLexicalForm();

    if (o instanceof URIReference)
      return ((URIReference) o).getURI().toString();

    if (o instanceof BlankNode)
      return o.toString();

    if (o instanceof Answer)
      throw new AnswerException("is a subquery result");

    throw new AnswerException("unknown object: '" + o + "'");
  }

  public boolean isURI(int idx) throws AnswerException {
    try {
      return getObject(idx, true) instanceof URIReference;
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }
  }

  public URI getURI(int idx) throws AnswerException {
    Object o;
    try {
      o = getObject(idx, true);
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }

    if (o instanceof URIReference)
      return ((URIReference) o).getURI();

    throw new AnswerException("is not a URI");
  }

  public boolean isBlankNode(int idx) throws AnswerException {
    try {
      return getObject(idx, true) instanceof BlankNode;
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }
  }

  public String getBlankNode(int idx) throws AnswerException {
    Object o;
    try {
      o = getObject(idx, true);
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }

    if (o instanceof BlankNode)
      return o.toString();

    throw new AnswerException("is not a blank node");
  }

  public boolean isSubQueryResults(int idx) throws AnswerException {
    try {
      return getObject(idx, true) instanceof Answer;
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }
  }

  public AnswerAnswer getSubQueryResults(int idx) throws AnswerException {
    if (ansCache[idx] != null)
      return ansCache[idx];

    Object o;
    try {
      o = getObject(idx, false);
    } catch (TuplesException te) {
      throw new AnswerException(te);
    }

    if (o instanceof Answer) {
      ansCache[idx] = new AnswerAnswer((Answer) o);
      return ansCache[idx];
    }

    throw new AnswerException("is not a sub-query answer");
  }
}
