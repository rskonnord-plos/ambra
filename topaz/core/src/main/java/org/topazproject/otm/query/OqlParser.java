/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
