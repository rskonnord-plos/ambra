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

import org.ambraproject.views.CategoryView;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class CategoryUtilsTest {
  private static Logger log = LoggerFactory.getLogger(CategoryUtilsTest.class);

  @DataProvider(name = "makeMap")
  public Object[][] createMap() {
    return new Object[][]{
      {
        new ArrayList() {{
          add(new CategoryCount("/f", 5));
          add(new CategoryCount("/a/b/c", 6));
          add(new CategoryCount("/a/b/c/d", 2));
          add(new CategoryCount("/g", 8));
          add(new CategoryCount("/a/c/e", 4));
          add(new CategoryCount("/a/b/c/d/e", 4));
          add(new CategoryCount("/e", 4));
          add(new CategoryCount("/z", 17));
          add(new CategoryCount("/1/2/3", 7));
          add(new CategoryCount("/x/y", 31));
        }},
        new CategoryView("ROOT", 0) {{
          addChild(new CategoryView("a", 0) {{
            addChild(new CategoryView("b", 0) {{
              addChild(new CategoryView("c", 6) {{
                addChild(new CategoryView("d", 2) {{
                  addChild(new CategoryView("e", 4));
                }});
              }});
            }});
            addChild(new CategoryView("c", 0) {{
              addChild(new CategoryView("e", 4));
            }});
          }});
          addChild(new CategoryView("e", 4));
          addChild(new CategoryView("g", 8));
          addChild(new CategoryView("f", 5));
          addChild(new CategoryView("z", 17));
          addChild(new CategoryView("1", 0) {{
            addChild(new CategoryView("2", 0) {{
              addChild(new CategoryView("3", 7));
            }});
          }});
          addChild(new CategoryView("x", 0) {{
            addChild(new CategoryView("y", 31));
          }});
        }}
      }
    };
  }


  @DataProvider(name = "filterMap")
  public Object[][] filterMap() {
    return new Object[][]{
      {
        "a",
        new CategoryView("ROOT") {{
          addChild(new CategoryView("a"));
          addChild(new CategoryView("d") {{
            addChild(new CategoryView("e") {{
              addChild(new CategoryView("f") {{
                addChild(new CategoryView("a"));
              }});
            }});
          }});

          addChild(new CategoryView("d") {{
            addChild(new CategoryView("e") {{
              addChild(new CategoryView("f") {{
                addChild(new CategoryView("a"));
              }});
            }});
          }});

          addChild(new CategoryView("b") {{
            addChild(new CategoryView("a"));
          }});
        }},
        new CategoryView("ROOT") {{
          addChild(new CategoryView("a") {{
            addChild(new CategoryView("b") {{
              addChild(new CategoryView("c"));
            }});
            addChild(new CategoryView("c") {{
              addChild(new CategoryView("e"));
            }});
          }});
          addChild(new CategoryView("d") {{
            addChild(new CategoryView("e") {{
              addChild(new CategoryView("f") {{
                addChild(new CategoryView("a"));
              }});
            }});
          }});
          addChild(new CategoryView("g") {{
            addChild(new CategoryView("f"));
          }});
          addChild(new CategoryView("a") {{
            addChild(new CategoryView("x"));
          }});
          addChild(new CategoryView("b") {{
            addChild(new CategoryView("a"));
          }});
        }}
      }
    };
  }

  @DataProvider(name = "findCategory")
  public Object[][] findCategory() {
    return new Object[][]{
      {
        "F",
        "f",
        new CategoryView("ROOT") {{
          addChild(new CategoryView("a"));
          addChild(new CategoryView("d") {{
            addChild(new CategoryView("e") {{
              addChild(new CategoryView("f") {{
                addChild(new CategoryView("a"));
              }});
            }});
          }});

          addChild(new CategoryView("d") {{
            addChild(new CategoryView("e") {{
              addChild(new CategoryView("f") {{
                addChild(new CategoryView("a"));
              }});
            }});
          }});

          addChild(new CategoryView("b") {{
            addChild(new CategoryView("a"));
          }});
        }}
      }
    };
  }

  @Test(dataProvider = "makeMap")
  public void testCreateMap(List<CategoryCount> before, CategoryView expected) {
    for(CategoryCount subject : before) {
      log.debug(subject.getCategory());
    }

    CategoryView result = CategoryUtils.createMapFromStringList(before);

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

  @Test(dataProvider = "filterMap")
  @SuppressWarnings("unchecked")
  public void testFilterMap(String filter, CategoryView expected, CategoryView source) {
    CategoryView result = CategoryUtils.filterMap(source, new String[]{filter});

    log.debug("Source");
    printMap(source, 0);
    log.debug("Result");
    printMap(result, 0);

    //Compare both ways to get around testNG bug
    assertEqualRecursive(result, expected);
    assertEqualRecursive(expected, result);
  }

  @Test(dataProvider = "findCategory")
  @SuppressWarnings("unchecked")
  public void testFindCategory(String filter, String expected, CategoryView source) {
    CategoryView result = CategoryUtils.findCategory(source, filter);

    //Compare both ways to get around testNG bug
    assertEquals(result.getName(), expected);
  }

  private void assertEqualRecursive(CategoryView result, CategoryView expected) {
    assertEquals(result.getName(), expected.getName());
    assertEquals(result.getCount(), expected.getCount());

    for(String key : result.getChildren().keySet()) {
      assertEqualRecursive(result.getChild(key), expected.getChild(key));
    }
  }

  private void printMap(CategoryView view, int depth) {
    String spacer = StringUtils.repeat("-", depth);

    for(String key : view.getChildren().keySet()) {
      log.debug("{}Key: {}, Size: {}", new Object[] { spacer, key, view.getChild(key).getChildren().size() });

      if(view.getChild(key).getChildren().size() > 0) {
        printMap(view.getChild(key), depth + 1);
      }
    }
  }
}
