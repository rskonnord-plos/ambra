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
