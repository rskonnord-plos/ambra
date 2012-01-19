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

import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.AnswerSet;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.query.QueryException;
import org.topazproject.otm.query.Results;

/** 
 * This processes the Itql results from a native query into a Results object.
 * 
 * @author Pradeep Krishnan
 */
class ItqlNativeResults extends Results {
  private final AnswerSet.QueryAnswerSet qas;

  private ItqlNativeResults(AnswerSet.QueryAnswerSet qas) throws OtmException {
    super(qas.getVariables(), null);
    this.qas  = qas;
  }

  public ItqlNativeResults(String a) throws OtmException {
    this(getQAS(a));
  }

  private static AnswerSet.QueryAnswerSet getQAS(String a) throws OtmException {
    try {
      AnswerSet ans = new AnswerSet(a);

      // check if we got something useful
      ans.beforeFirst();
      if (!ans.next())
        return null;
      if (!ans.isQueryResult())
        throw new QueryException("query failed: " + ans.getMessage());

      // looks like we're ok
      return ans.getQueryResults();
    } catch (AnswerException ae) {
      throw new QueryException("Error parsing answer", ae);
    }
  }


  @Override
  public void beforeFirst() throws OtmException {
    super.beforeFirst();
    qas.beforeFirst();
  }

  @Override
  protected void loadRow() throws OtmException {
    if (!qas.next()) {
      eor = true;
      return;
    }

    curRow = new Object[qas.getVariables().length];
    for (int idx = 0; idx < curRow.length; idx++) {
      try {
        curRow[idx] = getResult(idx);
      } catch (AnswerException ae) {
        throw new QueryException("Error parsing answer", ae);
      }
    }
  }

  private Object getResult(int idx) throws OtmException, AnswerException {

    if (qas.isLiteral(idx)) {
      types[idx] = Type.LITERAL;

    // FIXME: need lang from AnswerSet
      return new Literal(qas.getString(idx), qas.getLiteralDataType(idx), null);
    }

    if (qas.isURI(idx)) {
      types[idx] = Type.URI;
      return qas.getURI(idx);
    }

    if (qas.isSubQueryResults(idx)) {
      types[idx] = Type.SUBQ_RESULTS;
      return new ItqlNativeResults(qas.getSubQueryResults(idx));
    }

    types[idx] = Type.UNKNOWN;
    return null;
  }
}
