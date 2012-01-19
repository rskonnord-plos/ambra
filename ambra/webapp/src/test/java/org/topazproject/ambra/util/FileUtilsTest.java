/* $HeadURL::                                                                            $
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

package org.topazproject.ambra.util;

import junit.framework.TestCase;

import java.net.URISyntaxException;

import org.topazproject.ambra.util.FileUtils;

public class FileUtilsTest extends TestCase {
  public void testFileNameExtraction() throws URISyntaxException {
    assertEquals("TransformerFactory.html", FileUtils.getFileName("http://java.sun.com/j2se/1.4.2/docs/api/javax/xml/transform/TransformerFactory.html"));
    assertEquals("TransformerFactory.txt", FileUtils.getFileName("/1.4.2/docs/api/javax/xml/transform/TransformerFactory.txt"));
    assertEquals("TransformerFactory.txt", FileUtils.getFileName("C:\\1.4.2\\docs\\api\\javax\\xml\\transform\\TransformerFactory.txt"));
  }

  public void testFileExtForMimeType() throws Exception {
    assertEquals("tiff", FileUtils.getDefaultFileExtByMimeType("image/tiff"));
    assertEquals("html", FileUtils.getDefaultFileExtByMimeType("text/html"));
    assertEquals("xml", FileUtils.getDefaultFileExtByMimeType("text/xml"));
    assertEquals("ps", FileUtils.getDefaultFileExtByMimeType("application/postscript"));
  }
}
