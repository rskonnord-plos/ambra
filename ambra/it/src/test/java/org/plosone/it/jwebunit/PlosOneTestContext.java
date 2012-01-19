/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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

package org.plosone.it.jwebunit;

import java.util.HashMap;
import java.util.Map;

import com.gargoylesoftware.htmlunit.BrowserVersion;

import net.sourceforge.jwebunit.util.TestContext;

/**
 * Test context for plosone tests.
 *
 * @author Pradeep Krishnan
  */
public class PlosOneTestContext extends TestContext {
  private final BrowserVersion bv;
  private final Map<String, String> httpHeaders = new HashMap();

  /**
   * Creates a new PlosOneTestContext object.
   *
   * @param bv the browser to emulate
   * @param httpHeaders additional http headers to set
   */
  public PlosOneTestContext(BrowserVersion bv, Map<String, String> httpHeaders) {
    this.bv = bv;
    if (httpHeaders != null)
      this.httpHeaders.putAll(httpHeaders);
  }

  /**
   * Gets the browser in use
   *
   * @return bv as BrowserVersion.
   */
  public BrowserVersion getBrowser() {
    return bv;
  }

  /**
   * Gets the additional http headers
   *
   * @return any additional headers that need to be set
   */
  public Map getRequestHeaders() {
    Map m  = super.getRequestHeaders();
    if (m == null)
      m = httpHeaders;
    else
      m.putAll(httpHeaders);
    return m;
  }

}
