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
import java.util.List;

import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.query.ProjectionFunction;
import org.topazproject.otm.query.QueryInfo;

/** 
 * This processes the Itql results from an OQL query into an OTM Results object.
 * 
 * @author Ronald Tschalär
 */
class ItqlOQLResults extends ItqlResults {
  private final QueryInfo qi;

  /** 
   * Create a new oql-itql-query results object. 
   * 
   * @param qa       the query answer
   * @param qi       the query-info
   * @param warnings the list of warnings generated during the query processing, or null
   * @param sess     the session this is attached to
   * @throws OtmException 
   */
  public ItqlOQLResults(Answer qa, QueryInfo qi, String[] warnings, Session sess)
      throws OtmException {
    super(getVariables(qi), getTypes(qi), getFuncResult(qa, qi), warnings, sess);
    this.qi = qi;
  }

  private static String[] getVariables(QueryInfo qi) throws OtmException {
    List<String> vars = qi.getVars();

    int idx = 0;
    for (ProjectionFunction pf : qi.getFuncs()) {
      if (pf != null) {
        int old_size = vars.size();
        vars = pf.initVars(vars, idx);
        idx -= old_size - vars.size();
      }
      idx++;
    }

    return vars.toArray(new String[vars.size()]);
  }

  private static Type[] getTypes(QueryInfo qi) throws OtmException {
    List<Object> types = qi.getTypes();

    int idx = 0;
    for (ProjectionFunction pf : qi.getFuncs()) {
      if (pf != null) {
        int old_size = types.size();
        types = pf.initTypes(types, idx);
        idx -= old_size - types.size();
      }
      idx++;
    }

    Type[] res = new Type[types.size()];

    idx = 0;
    for (Object t : qi.getTypes()) {
      if (t == null)
        res[idx] = Type.UNKNOWN;
      else if (t instanceof QueryInfo)
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

  private static Answer getFuncResult(Answer qa, QueryInfo qi) throws OtmException {
    int idx = 0;
    for (ProjectionFunction pf : qi.getFuncs()) {
      if (pf != null) {
        int old_size = qa.getVariables().length;
        qa = pf.initItqlResult(qa, idx);
        idx -= old_size - qa.getVariables().length;
      }
      idx++;
    }

    return qa;
  }

  @Override
  protected Object getResult(int idx, Type type, boolean eager)
      throws OtmException, AnswerException {
    ProjectionFunction pf = qi.getFuncs().get(idx);
    if (pf != null) {
      Object res = pf.getItqlResult(qa, pos, idx, type, eager);
      if (res != null)
        return res;
    }

    switch (type) {
      case SUBQ_RESULTS:
        return new ItqlOQLResults(qa.getSubQueryResults(idx), (QueryInfo) qi.getTypes().get(idx),
                                  null, sess);

      case CLASS:
        ClassMetadata cm = (ClassMetadata) qi.getTypes().get(idx);
        return eager ? sess.get(cm, qa.getString(idx), false) :
                       sess.load(cm, qa.getString(idx));

      default:
        return super.getResult(idx, type, eager);
    }
  }
}
