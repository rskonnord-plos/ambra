/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
package org.ambraproject.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;


public class TextUtilsTest {
  private static Logger log = LoggerFactory.getLogger(TextUtilsTest.class);

  @DataProvider(name = "brokenUrls")
  public String[][] createBrokenData() {
    return new String[][]{
        {"http://"},
        {"ftp://"},
        {"..."},
        {"\\"},
        {"http://google.com\\"},
        {"http://www.google.com\\"},
        {"google.com\\"},
        {"--"},
        {"httpss:\\..."},
        {"ftps://www.google.com"},
        {"asdasdasd"},
        {"123123"},
        {"http://www.yahoo.com:asas"},
        {"http://www.   yahoo.com:asas"},
    };
  }

  @DataProvider(name = "correctUrls")
  public String[][] createCorrectData() {
    return new String[][]{
        {"http://www.yahoo.com"},
        {"http://www.yahoo.com:9090"},
        {"http://www.yahoo.com/"},
        {"https://www.yahoo.com/"},
        {"ftp://www.yahoo.com/"},
        {"http://www.google.com//something#somewhere"},
        {"ftp://..."},
    };
  }

  @DataProvider(name = "makeUrls")
  public String[][] createMakeData() {
    return new String[][]{
        {"www.google.com", "http://www.google.com"},
        {"http://www.google.com", "http://www.google.com"},
        {"ftp://www.google.com", "ftp://www.google.com"},
        {"https://www.google.com", "https://www.google.com"},
    };
  }

  @DataProvider(name = "makeMap")
  public Object[][] createMap() {
    return new Object[][]{
      {
        new ArrayList() {{
          add("/f");
          add("/a/b/c");
          add("/a/b/c/d");
          add("/g");
          add("/a/c/e");
          add("/a/b/c/d/e");
          add("/e");
          add("/z");
          add("/1/2/3");
          add("/x/y");
        }},
        new TreeMap() {{
          put("a",
          new TreeMap() {{
            put("b",
            new TreeMap() {{
              put("c",
              new TreeMap() {{
                put("d",
                new TreeMap() {{
                  put("e",
                  new TreeMap());
                }});
              }});
            }});

            put("c",
              new TreeMap() {{
                put("e",
                new TreeMap());
              }});
          }});

          put("e", new TreeMap());
          put("g", new TreeMap());
          put("f", new TreeMap());
          put("z", new TreeMap());
          put("1",
            new TreeMap() {{
              put("2",
                new TreeMap() {{
                  put("3", new TreeMap());
                }});
            }});
          put("x",
            new TreeMap() {{
              put("y", new TreeMap());
            }});
        }}
      }
    };
  }

  @DataProvider(name = "malicious")
  public String[][] createMaliciousData() {
    return new String[][]{
        {"<"},
        {"something<script"},
        {"something>"},
        {"someth&ing"},
        {"someth%ing"},
        {">something"},
        {"s%omething"},
        {"somet)hing"},
        {"(something"},
        {"someth'ing+"},
        {"somethin\"g"}
    };
  }

  @DataProvider(name = "hashStrings")
  public String[][] strings2Hash () {
    return new String[][] {
      { "1" },
      { "{\"query\":\"\",\"unformattedQuery\":\"everything:testing\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":\"\",\"filterJournals\":[\"PLoSOne\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}" },
      { "Nulla diam lectus, venenatis non adipiscing in, dictum lacinia lorem. Sed a lorem justo, molestie sagittis lectus. In ante nunc, tristique at venenatis sed, iaculis ut ipsum. Donec suscipit hendrerit ultrices. Vivamus volutpat consectetur blandit. Curabitur commodo malesuada pretium. Vestibulum aliquet lacinia consequat. Morbi porttitor orci eget neque pellentesque volutpat. Curabitur laoreet diam vel nunc congue sagittis sit amet nec urna. Aliquam erat volutpat. Vestibulum viverra augue a tortor convallis posuere. Vestibulum in felis vel libero tincidunt vulputate quis nec sem. Cras imperdiet molestie diam nec hendrerit. Integer accumsan volutpat leo, sit amet molestie leo condimentum vitae. Nam arcu leo, luctus in semper nec, tempus at tellus. Vivamus tempus lectus at augue eleifend eu ullamcorper orci molestie. " },
      { "bleh" },
      { "TESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTEST"},
      { "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" },
      { "fffffffffffffffffffffffffffffffffffffffffff fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" }
    };
  }

  @DataProvider(name = "nonMalicious")
  public String[][] createNonMaliciousData() {
    return new String[][]{
        {"something."}
    };
  }

  @DataProvider(name = "hyperlinks")
  public String[][] createHyperlinks() {
    return new String[][]{
        {"Ïwww.google.com", "Ï<a href=\"http://www.google.com\">www.google.com</a>"},
        {"Ï", "Ï"}
    };
  }

  @DataProvider(name = "escapeHyperlinks")
  public String[][] createEscapeHyperlinks() {
    return new String[][]{
        {"Ïwww.google.com", "<p>&Iuml;<a href=\"http://www.google.com\">www.google.com</a></p>"},
        {"Ï", "<p>&Iuml;</p>"}
    };
  }

  @DataProvider(name = "tagsToBeStripped")
  public String[][] createTagsToBeStripped() {
    return new String[][]{
        {"Test string with no tags.", "Test string with no tags."}, // no tags
        {"<i><a href=\"http://www.google.com\">who?</a></i>", "who?"}, // nested tags, attributes
        {"www.google.com</a></p>", "www.google.com"}, // unpaired tags
        {"2>1 and 3 > 2 and 4> 3 and 4 >5", "2>1 and 3 > 2 and 4> 3 and 4 >5"}, // not tags
        {"1<2 and 2 < 3 and 3< 4 and 4< 5", "1<2 and 2 < 3 and 3< 4 and 4< 5"}, // not tags
        {"2>1 and 2<3", "2>1 and 2<3"}, // not tags
        {"1<2 and <p> and <p/>3>2", "1<2 and  and 3>2"}, // brackets and tags
        {"<i/> and 2>1<i>", " and 2>1"}, // brackets and tags
        {"<p></p>", ""} // nothing but tags
    };
  }

  @Test
  public void testUniqueHash() {
    String result = TextUtils.createHash("TESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTEST");
    String result1 = TextUtils.createHash("TESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTEST1");

    String result2 = TextUtils.createHash("tESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTEST");
    String result3 = TextUtils.createHash("TESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTEST");

    assertNotSame(result, result1);
    assertNotSame(result2, result3);
  }

  @Test(dataProvider = "hashStrings")
  public void testHashLength(String hash) {
    String result = TextUtils.createHash(hash);

    assertEquals(result.length(), 27);
  }

  @Test(dataProvider = "brokenUrls")
  public void testValidatesBrokenUrl(String url) {
    assertFalse(TextUtils.verifyUrl(url));
  }

  @Test(dataProvider = "correctUrls")
  public void testValidatesCorrectUrl(String url) {
    assertTrue(TextUtils.verifyUrl(url));
  }

  @Test(dataProvider = "makeUrls")
  public void testMakeUrl(String url, String expected) throws Exception {
    assertEquals(TextUtils.makeValidUrl(url), expected);
  }

  @Test(dataProvider = "malicious")
  public void testMaliciousContent(String data) {
    assertTrue(TextUtils.isPotentiallyMalicious(data));
  }

  @Test(dataProvider = "nonMalicious")
  public void testNonMaliciousContent(String data) {
    assertFalse(TextUtils.isPotentiallyMalicious(data));
  }

  @Test(dataProvider = "hyperlinks")
  public void testHyperLink(String hyperlink, String expected) {
    assertEquals(TextUtils.hyperlink(hyperlink), expected);
  }

  @Test(dataProvider = "escapeHyperlinks")
  public void testEscapeAndHyperlink(String hyperlink, String expected) {
    assertEquals(TextUtils.escapeAndHyperlink(hyperlink), expected);
  }

  @Test(dataProvider = "tagsToBeStripped")
  public void testSimpleStripAllTags(String before, String after) {
    assertEquals(TextUtils.simpleStripAllTags(before), after);
  }

  @Test(dataProvider = "makeMap")
  public void testCreateMap(List<String> before, TreeMap expected) {
    for(String string : before) {
      log.debug(string);
    }

    TreeMap result = TextUtils.createMapFromStringList(before);

    if(log.isDebugEnabled()) {
      log.debug("Result Map:");
      printMap(result, 0);
    }

    if(log.isDebugEnabled()) {
      log.debug("Expected Map:");
      printMap(expected, 0);
    }

    //Compare both ways to get around testNG bug
    assertEqualRecursive(result, expected);
    assertEqualRecursive(expected, result);
  }

  private void assertEqualRecursive(TreeMap result, TreeMap expected) {
    assertEquals(result, expected);

    for(Object key : result.keySet()) {
      assertEqualRecursive((TreeMap)result.get(key), (TreeMap)expected.get(key));
    }
  }

  private void printMap(TreeMap map, int depth) {
    String spacer = StringUtils.repeat("-", depth);

    for(Object key : map.keySet()) {
      log.debug("{}Key: {}, Size: {}", new Object[] { spacer, key, ((TreeMap)map.get(key)).size() });

      if(((TreeMap)map.get(key)).size() > 0) {
        printMap((TreeMap)map.get(key), depth + 1);
      }
    }
  }
}
