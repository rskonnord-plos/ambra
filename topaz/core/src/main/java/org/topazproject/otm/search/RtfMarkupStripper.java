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

import java.io.StringReader;

import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.metadata.SearchableDefinition;

/**
 * A pre-processor that strips out all rtf markup.
 *
 * @author Ronald Tschalär
 */
public class RtfMarkupStripper implements PreProcessor {
  public String process(Object o, SearchableDefinition def, String value) throws OtmException {
    try {
      RTFEditorKit kit = new RTFEditorKit();
      Document doc = kit.createDefaultDocument();
      kit.read(new StringReader(value), doc, 0);
      return doc.getText(0, doc.getLength());
    } catch (Exception e) {
      throw new OtmException("Error parsing document", e);
    }
  }
}
