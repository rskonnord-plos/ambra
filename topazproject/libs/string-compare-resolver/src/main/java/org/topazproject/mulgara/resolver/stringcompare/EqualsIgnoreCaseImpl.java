/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.mulgara.resolver.stringcompare;

import org.mulgara.store.stringpool.SPObject;

/**
 * Implement equalsIgnoreCase operation
 *
 * @author Eric Brown
 */
class EqualsIgnoreCaseImpl extends StringCompareImpl {
  String getOp() { return "equalsIgnoreCase"; }

  boolean test(SPObject spo, String comp) {
    return spo.getLexicalForm().equalsIgnoreCase(comp);
  }

  boolean doFilter() { return true; }

  String  lowValue (String comp) { return comp.toUpperCase(); }
  String  highValue(String comp) { return comp.toLowerCase(); }
}
