/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.struts2;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.struts2.ServletActionContext;

/**
 * Struts interceptor that sets a session attribute if the request originates
 * from an "internal" IP.  The idea here is to allow certain functionality only
 * to users within the organization.
 */
public class InternalIpInterceptor extends AbstractInterceptor {

  /**
   * Key for the session attribute that will be set only if the request
   * comes from an internal IP.
   */
  public static final String INTERNAL_SESSION_KEY = "ambraproject_isInternalIp";

  @Override
  public String intercept(final ActionInvocation actionInvocation) throws Exception {
    String ip = ServletActionContext.getRequest().getRemoteAddr();
    boolean internal = false;

    // TODO: this is really bare-bones right now.  Expand this to include the external
    // IPs of offices if necessary.  Also, refactor this so that it plays nice with
    // non-PLOS installations.  (We can get away with this for now since our developers
    // are all in the SF office, accessing SF servers.)
    if (ip.startsWith("10.135.") || "127.0.0.1".equals(ip)) {
      internal = true;
    }
    actionInvocation.getInvocationContext().getSession().put(INTERNAL_SESSION_KEY, internal);
    return actionInvocation.invoke();
  }
}
