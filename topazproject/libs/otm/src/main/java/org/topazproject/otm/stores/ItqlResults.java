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
import org.topazproject.otm.Session;
import org.topazproject.otm.query.QueryException;
import org.topazproject.otm.query.QueryInfo;
import org.topazproject.otm.query.Results;

/** 
 * This processes the Itql results into an OTM Results object.
 * 
 * @author Ronald Tschalär
 */
class ItqlResults extends Results {
  private final AnswerSet.QueryAnswerSet qas;
  private final QueryInfo                qi;
  private final Session                  sess;

  private ItqlResults(AnswerSet.QueryAnswerSet qas, QueryInfo qi, String[] warnings, Session sess)
      throws OtmException {
    super(getVariables(qi), getTypes(qi), warnings);
    this.qas  = qas;
    this.qi   = qi;
    this.sess = sess;

    assert qas.getVariables().length == getVariables().length;
  }

  public ItqlResults(String a, QueryInfo qi, String[] warnings, Session sess) throws OtmException {
    this(getQAS(a), qi, warnings, sess);
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

  private static String[] getVariables(QueryInfo qi) {
    return qi.getVars().toArray(new String[0]);
  }

  private static Type[] getTypes(QueryInfo qi) {
    Type[] res = new Type[qi.getTypes().size()];

    int idx = 0;
    for (Object t : qi.getTypes()) {
      if (t instanceof QueryInfo)
        res[idx] = Type.SUBQ_RESULTS;
      else if (t.equals(URI.class))
        res[idx] = Type.URI;
      else if (t.equals(String.class))
        res[idx] = Type.LITERAL;
      else
        res[idx] = Type.CLASS;
      idx++;
    }

    return res;
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

    curRow = new Object[qi.getVars().size()];
    for (int idx = 0; idx < curRow.length; idx++) {
      try {
        curRow[idx] = getResult(idx);
      } catch (AnswerException ae) {
        throw new QueryException("Error parsing answer", ae);
      }
    }
  }

  private Object getResult(int idx) throws OtmException, AnswerException {
    switch (getType(idx)) {
      case LITERAL:
        // FIXME: need lang and datatype from AnswerSet
        return new Literal(qas.getString(idx), null, null);

      case URI:
        return qas.getURI(idx);

      case SUBQ_RESULTS:
        return new ItqlResults(qas.getSubQueryResults(idx), (QueryInfo) qi.getTypes().get(idx),
                               null, sess);

      case CLASS:
        return sess.get((Class) qi.getTypes().get(idx), qas.getString(idx));

      default:
        throw new Error("unknown type " + getType(idx) + " encountered");
    }
  }
}
