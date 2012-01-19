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
