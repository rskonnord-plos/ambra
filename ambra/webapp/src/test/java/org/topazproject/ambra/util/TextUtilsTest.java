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

import org.topazproject.ambra.util.TextUtils;

import junit.framework.TestCase;

public class TextUtilsTest extends TestCase {
  public void testValidatesUrl() {
    assertFalse(TextUtils.verifyUrl("http://"));
    assertFalse(TextUtils.verifyUrl("ftp://"));
    assertFalse(TextUtils.verifyUrl("..."));
    assertFalse(TextUtils.verifyUrl("\\"));
    assertFalse(TextUtils.verifyUrl("http://google.com\\"));
    assertFalse(TextUtils.verifyUrl("http://www.google.com\\"));
    assertFalse(TextUtils.verifyUrl("google.com\\"));
    assertFalse(TextUtils.verifyUrl("--"));
    assertFalse(TextUtils.verifyUrl("httpss:\\..."));
    assertFalse(TextUtils.verifyUrl("ftps://www.google.com"));
    assertFalse(TextUtils.verifyUrl("asdasdasd"));
    assertFalse(TextUtils.verifyUrl("123123"));
    assertFalse(TextUtils.verifyUrl("http://www.yahoo.com:asas"));
    assertFalse(TextUtils.verifyUrl("http://www.   yahoo.com:asas"));

    assertTrue(TextUtils.verifyUrl("http://www.yahoo.com"));
    assertTrue(TextUtils.verifyUrl("http://www.yahoo.com:9090"));
    assertTrue(TextUtils.verifyUrl("http://www.yahoo.com/"));
    assertTrue(TextUtils.verifyUrl("https://www.yahoo.com/"));
    assertTrue(TextUtils.verifyUrl("ftp://www.yahoo.com/"));
    assertTrue(TextUtils.verifyUrl("http://www.google.com//something#somewhere"));
    assertTrue(TextUtils.verifyUrl("ftp://..."));
  }

  public void testMakeUrl() throws Exception {
    assertEquals("http://www.google.com", TextUtils.makeValidUrl("www.google.com"));
    assertEquals("http://www.google.com", TextUtils.makeValidUrl("http://www.google.com"));
    assertEquals("ftp://www.google.com", TextUtils.makeValidUrl("ftp://www.google.com"));
    assertEquals("https://www.google.com", TextUtils.makeValidUrl("https://www.google.com"));
  }

  public void testMaliciousContent() {
    assertTrue(TextUtils.isPotentiallyMalicious("<"));
    assertTrue(TextUtils.isPotentiallyMalicious("something<script"));
    assertTrue(TextUtils.isPotentiallyMalicious("something>"));
    assertTrue(TextUtils.isPotentiallyMalicious("someth&ing"));
    assertTrue(TextUtils.isPotentiallyMalicious("someth%ing"));
    assertTrue(TextUtils.isPotentiallyMalicious(">something"));
    assertTrue(TextUtils.isPotentiallyMalicious("s%omething"));
    assertTrue(TextUtils.isPotentiallyMalicious("somet)hing"));
    assertTrue(TextUtils.isPotentiallyMalicious("(something"));
    assertTrue(TextUtils.isPotentiallyMalicious("someth'ing+"));
    assertTrue(TextUtils.isPotentiallyMalicious("somethin\"g"));
    assertFalse(TextUtils.isPotentiallyMalicious("something."));
  }

  public void testHyperLink() {
    assertEquals("Ï<a href=\"http://www.google.com\">www.google.com</a>" , TextUtils.hyperlink("Ïwww.google.com"));
    assertEquals("Ï" , TextUtils.hyperlink("Ï"));
  }

  public void testEscapeAndHyperlink() {
    assertEquals("<p>&Iuml;<a href=\"http://www.google.com\">www.google.com</a></p>" , TextUtils.escapeAndHyperlink("Ïwww.google.com"));
    assertEquals("<p>&Iuml;</p>" , TextUtils.escapeAndHyperlink("Ï"));
  }
}
