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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class CategoryUtilsTest {
  private static Logger log = LoggerFactory.getLogger(CategoryUtilsTest.class);

  private CategoryView buildComplicatedExpectedTree() {

    // If you're having trouble debugging this I strongly suggest you draw out
    // the tree/DAG on paper!

    CategoryView root = new CategoryView("ROOT", 0);
    CategoryView a = new CategoryView("a", 0);
    CategoryView b = new CategoryView("b", 0);
    CategoryView c = new CategoryView("c", 6);
    CategoryView d = new CategoryView("d", 2);
    CategoryView e = new CategoryView("e", 4);

    root.addChild(a);
    root.addChild(e);
    root.addChild(new CategoryView("f", 5));
    root.addChild(new CategoryView("g", 8));
    root.addChild(new CategoryView("z", 17));
    a.addChild(b);
    b.addChild(c);
    a.addChild(c);
    c.addChild(d);
    d.addChild(e);
    c.addChild(e);

    root.addChild(new CategoryView("1", 0) {{
      addChild(new CategoryView("2", 0) {{
        addChild(new CategoryView("3", 7));
      }});
    }});
    root.addChild(new CategoryView("x", 0) {{
      addChild(new CategoryView("y", 31));
    }});

    return root;
  }

  @DataProvider(name = "makeMap")
  public Object[][] createMap() {
    final CategoryView c = new CategoryView("c", 1);
    return new Object[][]{

      // Simple case that's a plain tree (no nodes have multiple parents).
      {
        new ArrayList() {{
          add(new Pair<String, Long>("/a", 1L));
          add(new Pair<String, Long>("/b", 2L));
          add(new Pair<String, Long>("/b/d", 4L));
          add(new Pair<String, Long>("/c", 3L));
        }},
        new CategoryView("ROOT", 0) {{
          addChild(new CategoryView("a", 1));
          addChild(new CategoryView("b", 2) {{
            addChild(new CategoryView("d", 4));
          }});
          addChild(new CategoryView("c", 3));
        }}
      },

      // Simple case where there is one node with two parents.
      {
        new ArrayList() {{
          add(new Pair<String, Long>("/a", 3L));
          add(new Pair<String, Long>("/a/b", 2L));
          add(new Pair<String, Long>("/a/b/c", 1L));
          add(new Pair<String, Long>("/a/c", 1L));
        }},
        new CategoryView("ROOT", 0) {{
          addChild(new CategoryView("a", 3) {{
            addChild(new CategoryView("b", 2) {{
              addChild(c);
            }});
            addChild(c);
          }});
        }}
      },

      // Complicated case.
      {
        new ArrayList() {{
          add(new Pair<String, Long>("/f", 5L));
          add(new Pair<String, Long>("/a/b/c", 6L));
          add(new Pair<String, Long>("/a/b/c/d", 2L));
          add(new Pair<String, Long>("/g", 8L));
          add(new Pair<String, Long>("/a/c/e", 4L));
          add(new Pair<String, Long>("/a/b/c/d/e", 4L));
          add(new Pair<String, Long>("/e", 4L));
          add(new Pair<String, Long>("/z", 17L));
          add(new Pair<String, Long>("/1/2/3", 7L));
          add(new Pair<String, Long>("/x/y", 31L));
        }},
        buildComplicatedExpectedTree()
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
  public void testCreateMap(List<Pair<String, Long>> before, CategoryView expected) {
    for(Pair<String, Long> subject : before) {
      log.debug(subject.getFirst());
    }

    CategoryView result = CategoryUtils.createMapFromStringList(before);

    if(log.isDebugEnabled()) {
      log.debug("Expected Map:");
      printMap(expected, 0);
    }

    if(log.isDebugEnabled()) {
      log.debug("\nResult Map:");
      printMap(result, 0);
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
    assertEquals(result, expected);
    assertEquals(result.getName(), expected.getName());
    assertEquals(result.getCount(), expected.getCount(), "Node " + expected.getName());

    for(String key : result.getChildren().keySet()) {
      assertEqualRecursive(result.getChild(key), expected.getChild(key));
    }
  }

  private void printMap(CategoryView view, int depth) {
    String spacer = StringUtils.repeat("-", depth);
    log.debug("{}Key: {}, Value: {}, Children: {}",
        new Object[] { spacer, view.getName(), view.getCount(), view.getChildren().size() });
    for (CategoryView child : view.getChildren().values()) {
      printMap(child, depth + 1);
    }
  }
}
