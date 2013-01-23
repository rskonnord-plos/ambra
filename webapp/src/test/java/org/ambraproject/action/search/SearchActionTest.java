/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2007-2012 by Public Library of Science
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

package org.ambraproject.action.search;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.AmbraWebTest;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.testutils.EmbeddedSolrServerFactory;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SearchActionTest extends AmbraWebTest {

  @Autowired
  protected SearchAction action;

  @Autowired
  protected EmbeddedSolrServerFactory solrServerFactory;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  /**
   * To match expectations in {@link SearchAction#getHttpServletRequest()}
   */
  @BeforeMethod
  public void setUpRequest() {
    final String reqAttrKey = "org.springframework.web.context.request.RequestContextListener.REQUEST_ATTRIBUTES";
    ServletRequestAttributes mockAttr = new ServletRequestAttributes(ServletActionContext.getRequest());
    action.setRequest(Collections.singletonMap(reqAttrKey, mockAttr));
  }

  private void assertInvalidSimpleSearchQuery(String query) throws Exception {
    action.setQuery(query);
    String result = action.executeSimpleSearch();
    assertEquals(result, Action.INPUT, "Didn't halt with INPUT with query: " + query);
    final Map<String, List<String>> fieldErrors = action.getFieldErrors();
    assertEquals(fieldErrors.size(), 1, "Expected exactly one field error from invalid query");
    assertTrue(fieldErrors.containsKey("query"), "Expected field error to have key=\"query\" from invalid query");
    action.clearFieldErrors();
  }

  @Test
  public void testInvalidSimpleSearches() throws Exception {
    assertInvalidSimpleSearchQuery(null);
    assertInvalidSimpleSearchQuery("");
  }

}
