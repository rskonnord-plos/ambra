/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2011 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.models;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Alex Kudlick 11/10/11
 */
public class CategoryTest extends BaseHibernateTest {

  @Test
  public void testCategory() {
    Category category = new Category();
    category.setPath("/main category/some/stuff/in between/sub category");

    Long id = (Long) hibernateTemplate.save(category);
    assertNotNull(id, "session generated null id");
    category = (Category) hibernateTemplate.get(Category.class, id);

    assertEquals(category.getMainCategory(), "main category", "incorrect main category");
    assertEquals(category.getSubCategory(), "sub category", "incorrect sub category");
    assertEquals(category.getPath(),
        "/main category/some/stuff/in between/sub category", "incorrect path");

    category = new Category();
    category.setPath("/main category/sub category");
    assertEquals(category.getMainCategory(), "main category", "incorrect main category");
    assertEquals(category.getSubCategory(), "sub category", "incorrect sub category");
  }

  @Test(expectedExceptions = {DataIntegrityViolationException.class})
  public void testUniqueConstraint() {
    String main = "main";
    String sub = "sub";
    Category category1 = new Category();
    category1.setPath("/main/sub");

    Category category2 = new Category();
    category2.setPath("/main/sub");

    hibernateTemplate.save(category1);
    hibernateTemplate.save(category2);

  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testSetPath_null() {
    Category category = new Category();
    category.setPath(null);
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testSetPath_empty() {
    Category category = new Category();
    category.setPath("");
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testSetPath_noSlash() {
    Category category = new Category();
    category.setPath("foo");

    category = new Category();
    category.setPath("foo/bar");
  }
}
