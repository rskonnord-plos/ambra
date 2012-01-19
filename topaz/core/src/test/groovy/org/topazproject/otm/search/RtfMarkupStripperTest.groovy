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

package org.topazproject.otm.search;

/**
 * Rtf-tag-stripper unit tests.
 */
public class RtfMarkupStripperTest extends GroovyTestCase {
  void testBasic() {
    String rtf = """
      {\\rtf1\\ansi{\\fonttbl\\f0\\fswiss Helvetica;}\\f0\\pard
      This is some {\\b bold} text.\\par
      }
    """

    String exp = "      This is some bold text.\n      \n"

    assertEquals(exp, new RtfMarkupStripper().process(null, null, rtf))
  }
}
