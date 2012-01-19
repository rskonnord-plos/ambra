/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.query;

import java.util.ArrayList;
import java.util.List;

import antlr.LLkParser;
import antlr.ParserSharedInputState;
import antlr.RecognitionException;
import antlr.TokenBuffer;
import antlr.TokenStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * This holds some common stuff for Oql parsers, such as collecting error and warning
 * messages.
 * 
 * @author Ronald Tschalär
 */
abstract class OqlParser extends LLkParser implements ErrorCollector {
  private static final Log log = LogFactory.getLog(OqlParser.class);

  private final List<String> errs = new ArrayList<String>();
  private final List<String> wrns = new ArrayList<String>();

  /** 
   * Create a new parser instance. 
   * 
   * @param state the parser state to read from
   * @param k     how far to look ahead
   */
  protected OqlParser(ParserSharedInputState state, int k) {
    super(state, k);
  }

  /** 
   * Create a new parser instance. 
   * 
   * @param tokenBuf the token buffer to read from
   * @param k        how far to look ahead
   */
  protected OqlParser(TokenBuffer tokenBuf, int k) {
    super(tokenBuf, k);
  }

  /** 
   * Create a new parser instance. 
   * 
   * @param lexer the lexer to read from
   * @param k     how far to look ahead
   */
  protected OqlParser(TokenStream lexer, int k) {
    super(lexer, k);
  }

  @Override
  public void reportError(RecognitionException ex) {
    log.debug("parse exception", ex);
    errs.add(ex.toString());
  }

  @Override
  public void reportError(String err) {
    errs.add(err);
  }

  @Override
  public void reportWarning(String wrn) {
    wrns.add(wrn);
  }

  public List<String> getErrors() {
    return errs;
  }

  public List<String> getWarnings() {
    return wrns;
  }

  public String getErrors(String join) {
    return StringUtils.join(errs, (join != null ? join : NL));
  }

  public String getWarnings(String join) {
    return StringUtils.join(wrns, (join != null ? join : NL));
  }
}
