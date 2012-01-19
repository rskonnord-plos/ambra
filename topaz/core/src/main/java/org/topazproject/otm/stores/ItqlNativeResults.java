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

import java.util.Arrays;

import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;

/** 
 * This processes the Itql results from a native query into a Results object.
 * 
 * @author Pradeep Krishnan
 */
class ItqlNativeResults extends ItqlResults {
  /** 
   * Create a new native-itql-query results object. 
   * 
   * @param qa   the query answer
   * @param sess the session this is attached to
   * @throws OtmException 
   */
  public ItqlNativeResults(Answer qa, Session sess) throws OtmException {
    super(qa.getVariables(), getTypes(qa.getVariables()), qa, null, sess);
  }

  private static Type[] getTypes(String[] variables) {
    Type[] types = new Type[variables.length];
    Arrays.fill(types, Type.UNKNOWN);
    return types;
  }

  @Override
  protected Object getResult(int idx, Type type, boolean eager)
      throws OtmException, AnswerException {
    switch (type) {
      case SUBQ_RESULTS:
        return new ItqlNativeResults(qa.getSubQueryResults(idx), sess);

      default:
        return super.getResult(idx, type, eager);
    }
  }
}
