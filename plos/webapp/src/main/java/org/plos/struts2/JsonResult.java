/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.struts2;

import javax.servlet.http.HttpServletResponse;

import com.googlecode.jsonplugin.JSONResult;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

import org.apache.struts2.StrutsStatics;

/**
 * @author stevec
 *
 */
public class JsonResult extends JSONResult {
  private boolean noCache;
  
  /**
   * 
   */
  public JsonResult() {
    super();
  }
  
  public void execute(ActionInvocation invocation) throws Exception {
    ActionContext actionContext = invocation.getInvocationContext();
    HttpServletResponse response = (HttpServletResponse) actionContext
        .get(StrutsStatics.HTTP_RESPONSE);

    if (noCache) {
      // HTTP 1.1 browsers should defeat caching on this header
      response.setHeader("Cache-Control", "no-cache");
      // HTTP 1.0 browsers should defeat caching on this header
      response.setHeader("Pragma", "no-cache");
      // Last resort for those that ignore all of the above
      response.setHeader("Expires", "0");
    }
    super.execute(invocation);
  }

  /**
   * @param noCache The noCache to set.
   */
  public void setNoCache(boolean noCache) {
    this.noCache = noCache;
  }

}
