/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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

  StringCompareImpl getOpposite() { return this; }
}
