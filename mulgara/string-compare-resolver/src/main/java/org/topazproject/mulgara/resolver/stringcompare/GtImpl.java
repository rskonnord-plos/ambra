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
 * Implement gt comparison
 *
 * @author Eric Brown
 */
class GtImpl extends StringCompareImpl {
  String getOp() { return "gt"; }

  boolean test(SPObject spo, String comp) {
    return spo.getLexicalForm().compareTo(comp) > 0;
  }

  String  lowValue(String comp) { return comp; }
  boolean incLowValue() { return false; }
}
