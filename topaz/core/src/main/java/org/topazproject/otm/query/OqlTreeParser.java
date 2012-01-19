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

import antlr.RecognitionException;
import antlr.TreeParser;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * This holds some common stuff for Oql tree parsers, such as collecting error and warning
 * messages and setting up the default AST node class.
 * 
 * @author Ronald Tschalär
 */
abstract class OqlTreeParser extends TreeParser implements ErrorCollector {
  private static final Log log = LogFactory.getLog(OqlTreeParser.class);

  private final List<String> errs = new ArrayList<String>();
  private final List<String> wrns = new ArrayList<String>();

  /** 
   * Create a new tree-parser instance. 
   */
  protected OqlTreeParser() {
    astFactory.setASTNodeClass(OqlAST.class);
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
