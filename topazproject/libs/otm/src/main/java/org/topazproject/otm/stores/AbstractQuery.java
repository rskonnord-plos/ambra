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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.query.ErrorCollector;
import org.topazproject.otm.query.FieldTranslator;
import org.topazproject.otm.query.QueryException;
import org.topazproject.otm.query.QueryLexer;
import org.topazproject.otm.query.QueryParser;

import antlr.collections.AST;

/** 
 * Common code for query handlers.
 * 
 * @author Ronald Tschalär
 */
abstract class AbstractQuery {
  private final Log          log;
  private final List<String> warnings = new ArrayList<String>();

  /** 
   * Create a new abstract query instance. 
   * 
   * @param log  the logger to user
   */
  protected AbstractQuery(Log log) {
    this.log = log;
  }

  /**
   * Do the non query-language specific parsing.
   *
   * @param sess   the current session
   * @param query  the query to parse
   * @return the AST after field translation
   * @throws OtmException if an error occurred
   */
  protected AST parseQuery(Session sess, String query) throws OtmException {
    ErrorCollector curParser = null;

    try {
      QueryLexer  lexer  = new QueryLexer(new StringReader(query));

      QueryParser parser = new QueryParser(lexer);
      curParser = parser;
      parser.query();
      checkMessages(parser.getErrors(), parser.getWarnings());

      FieldTranslator ft = new FieldTranslator(sess);
      curParser = ft;
      ft.query(parser.getAST());
      checkMessages(ft.getErrors(), ft.getWarnings());

      return ft.getAST();
    } catch (Exception e) {
      if (curParser != null && curParser.getErrors().size() > 0) {
        // exceptions are usually the result of aborted parsing due to errors
        log.debug("error parsing query: " + curParser.getErrors(null), e);
        throw new QueryException("error parsing query '" + query + "'", curParser.getErrors());
      } else
        throw new QueryException("error parsing query '" + query + "'", e);
    }
  }

  /** 
   * Check the errors and warnings. If there were any errors, generate an exception; else if there
   * were any warnings add them to the list.
   * 
   * @param errors   the list of parse errors
   * @param warnings the list of parse warnings
   * @throws OtmException if <var>errors</var> is non empty
   */
  protected void checkMessages(List<String> errors, List<String> warnings) throws OtmException {
    if (errors != null && errors.size() > 0)
      throw new QueryException("Error parsing query", errors);
    else if (warnings != null)
      this.warnings.addAll(warnings);
  }

  /** 
   * Get all warnings emitted while parsing query. 
   * 
   * @return the warnings; may be empty
   */
  public List<String> getWarnings() {
    return warnings;
  }
}
