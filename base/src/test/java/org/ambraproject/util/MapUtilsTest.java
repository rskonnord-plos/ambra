/* Copyright (c) 2006-2013 by Public Library of Science

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
import java.util.Map;
import java.util.TreeMap;
import static org.testng.Assert.assertEquals;

public class MapUtilsTest {
  private static Logger log = LoggerFactory.getLogger(MapUtilsTest.class);

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


  @Test(dataProvider = "makeMap")
  public void testCreateMap(List<String> before, TreeMap expected) {
    for(String string : before) {
      log.debug(string);
    }

    Map result = MapUtils.createMapFromStringList(before);

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

  private void assertEqualRecursive(Map result, Map expected) {
    assertEquals(result, expected);

    for(Object key : result.keySet()) {
      assertEqualRecursive((Map)result.get(key), (Map)expected.get(key));
    }
  }

  private void printMap(Map map, int depth) {
    String spacer = StringUtils.repeat("-", depth);

    for(Object key : map.keySet()) {
      log.debug("{}Key: {}, Size: {}", new Object[] { spacer, key, ((TreeMap)map.get(key)).size() });

      if(((TreeMap)map.get(key)).size() > 0) {
        printMap((TreeMap)map.get(key), depth + 1);
      }
    }
  }
}
