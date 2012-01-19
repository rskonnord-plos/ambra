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

import java.net.URI;

import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.query.QueryException;
import org.topazproject.otm.query.Results;

/** 
 * This processes the Itql results into an OTM Results object. Indivual result objects in each row
 * are loaded on-demand.
 * 
 * @author Ronald Tschalär
 */
abstract class ItqlResults extends Results {
  protected final Answer  qa;
  protected final Session sess;
  protected final Type[]  origTypes;

  /** 
   * Create a new itql-results instance.
   * 
   * @param vars     the list of variables in the result
   * @param types    the type of each variable
   * @param qa       the query-answer to build the results from
   * @param warnings the list of warnings generated while processing the query; may be null
   * @param sess     the session this is attached to
   */
  protected ItqlResults(String[] vars, Type[] types, Answer qa, String[] warnings, Session sess)
      throws OtmException {
    super(vars, types, warnings, sess.getSessionFactory());
    this.qa        = qa;
    this.sess      = sess;
    this.origTypes = types.clone();

    assert qa.getVariables().length == vars.length;
  }

  @Override
  public void beforeFirst() throws OtmException {
    super.beforeFirst();
    try {
      qa.beforeFirst();
    } catch (AnswerException ae) {
      throw new QueryException("Error processing answer", ae);
    }
  }

  @Override
  public void close() {
    qa.close();
  }

  @Override
  protected void loadRow() throws OtmException {
    for (int idx = 0; idx < getVariables().length; idx++)
      curRow[idx] = null;

    try {
      if (!qa.next())
        eor = true;
      else {
        for (int idx = 0; idx < getVariables().length; idx++) {
          if (origTypes[idx] == Type.UNKNOWN) {
            if (qa.isLiteral(idx))
              types[idx] = Type.LITERAL;
            else if (qa.isURI(idx))
              types[idx] = Type.URI;
            else if (qa.isBlankNode(idx))
              types[idx] = Type.BLANK_NODE;
            else if (qa.isSubQueryResults(idx))
              types[idx] = Type.SUBQ_RESULTS;
          }
        }
      }
    } catch (AnswerException ae) {
      throw new QueryException("Error parsing answer", ae);
    }
  }

  @Override
  public Object get(int idx, boolean eager) throws OtmException {
    if (eor)
      throw new QueryException("at end of results");

    if (curRow[idx] == null) {
      try {
        curRow[idx] = getResult(idx, getType(idx), eager);
      } catch (AnswerException ae) {
        throw new QueryException("Error parsing answer", ae);
      }
    }

    return curRow[idx];
  }

  /** 
   * Get a single result object. This handles LITERAL, URI, BLANK_NODE, and UNKNOWN only.
   * 
   * @param idx   which object to get
   * @param type  the object's type
   * @param eager true if the object should be fetched eagerly, false if lazily; only relevant
   *              for CLASS objects
   * @return the object
   * @throws OtmException 
   * @throws AnswerException 
   */
  protected Object getResult(int idx, Type type, boolean eager)
      throws OtmException, AnswerException {
    switch (type) {
      case LITERAL:
        String dt = qa.getLiteralDataType(idx);
        return new Literal(qa.getString(idx), qa.getLiteralLangTag(idx),
                           (dt != null) ? URI.create(dt) : null);

      case URI:
        return qa.getURI(idx);

      case BLANK_NODE:
        return qa.getBlankNode(idx);

      case UNKNOWN:
        if (qa.isLiteral(idx))
          types[idx] = Type.LITERAL;
        else if (qa.isURI(idx))
          types[idx] = Type.URI;
        else if (qa.isBlankNode(idx))
          types[idx] = Type.BLANK_NODE;
        else if (qa.isSubQueryResults(idx))
          types[idx] = Type.SUBQ_RESULTS;
        else
          throw new Error("unknown query-answer type encountered at index " + idx);
        return getResult(idx, types[idx], eager);

      default:
        throw new Error("unknown type " + type + " encountered");
    }
  }
}
