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

/** 
 * Partial implementation of an Answer that manages the list of variables. 
 * 
 * @author Ronald Tschalär
 */
public abstract class AbstractAnswer implements Answer {
  /** The result message; should be assigned in the constructor and not changed after that. */
  protected String   message;
  /** The result variables; should be assigned in the constructor and not changed after that. */
  protected String[] variables;

  public String getMessage() {
    return message;
  }

  public String[] getVariables() {
    return variables;
  }

  public int indexOf(String var) {
    if (variables == null)
      return -1;

    for (int idx = 0; idx < variables.length; idx++) {
      if (variables[idx].equals(var))
        return idx;
    }

    return -1;
  }

  public String getString(String  var) throws AnswerException {
    return getString(indexOf(var));
  }

  public URI getURI(String var) throws AnswerException {
    return getURI(indexOf(var));
  }

  public String getBlankNode(String var) throws AnswerException {
    return getBlankNode(indexOf(var));
  }

  public Answer getSubQueryResults(String var) throws AnswerException {
    return getSubQueryResults(indexOf(var));
  }
}
