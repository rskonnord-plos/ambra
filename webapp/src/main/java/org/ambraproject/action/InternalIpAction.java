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

package org.ambraproject.action;

import com.opensymphony.xwork2.ActionContext;
import org.ambraproject.struts2.InternalIpInterceptor;

/**
 * Abstract superclass for actions that should only be accessed by
 * users internal to the organization.
 *
 * @see org.ambraproject.struts2.InternalIpInterceptor
 */
public abstract class InternalIpAction extends BaseActionSupport {

  /**
   * @return true if InternalIpInterceptor has flagged this session as coming
   *     from an internal user
   */
  public boolean checkAccess() {
    Boolean internal = (Boolean) ActionContext.getContext().getSession().get(
        InternalIpInterceptor.INTERNAL_SESSION_KEY);
    if (internal == null || !internal) {
      return false;
    } else {
      return true;
    }
  }
}
