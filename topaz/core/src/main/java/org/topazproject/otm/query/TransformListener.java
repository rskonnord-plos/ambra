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

import antlr.RecognitionException;

/** 
 * A listener for OQL transform events.
 *
 * @author Ronald Tschalär
 */
public interface TransformListener {
  void deref(OqlAST reg, OqlAST[] nodes) throws RecognitionException;
}
