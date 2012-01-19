/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2008 by Topaz, Inc.
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

import java.io.Writer;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import org.ccil.cowan.tagsoup.Parser;

/**
 * A pre-processor that just strips all tags and replaces each with a space. In addition, the
 * &lt;script&gt; element is skipped altogether.
 *
 * @author Ronald Tschalär
 */
public class HtmlTagStripper extends XmlTagStripper {
  /**
   * Creates a new html parser.
   */
  protected XMLReader newParser() {
    return new Parser();
  }

  /**
   * Creates a new {@link TagStripper} that excludes the content of &lt;script&gt; tags too.
   */
  protected ContentHandler newContentHandler(Writer out) throws Exception {
    return new TagStripper(out) {
      protected boolean include(String ns, String localName, String qName) {
        return !localName.equalsIgnoreCase("script");
      }
    };
  }
}
