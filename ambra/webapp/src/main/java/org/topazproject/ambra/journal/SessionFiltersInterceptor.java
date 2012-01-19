/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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

package org.topazproject.ambra.journal;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts2.StrutsStatics;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.springframework.beans.factory.annotation.Required;

import org.topazproject.otm.Filter;
import org.topazproject.otm.Session;

/**
 * A webwork interceptor that sets up the set of filters to be used on the current otm session.
 * This is the glue between the virtual-journal-context and the virtual-journal definitions.
 *
 * @author Ronald Tschalär
 */
public class SessionFiltersInterceptor extends AbstractInterceptor {
  private static Log log = LogFactory.getLog(SessionFiltersInterceptor.class);

  private Session        session;
  private JournalService journalService;

  public String intercept(ActionInvocation invocation) throws Exception {
    setupFilters(invocation);
    return invocation.invoke();
  }

  private void setupFilters(ActionInvocation invocation) throws ServletException {
    HttpServletRequest request =
        (HttpServletRequest) invocation.getInvocationContext().get(StrutsStatics.HTTP_REQUEST);

    String jName = journalService.getCurrentJournalName();
    if (jName == null)
      return;

    Set<String> jf = journalService.getFilters(jName);
    if (jf == null)
      //throw new ServletException("No journal found with name '" + jName + "'");
      log.error("No journal found with name '" + jName + "'");

    // XXX: guard against jf == null, we don't have any journals yet, allow development to continue
    for (String fName : (jf != null ? jf : new HashSet<String>(0))) {
      Filter f = session.enableFilter(fName);
      if (f == null)            // shouldn't happen
        throw new ServletException("Filter '" + fName +
                                   "' has not been registered with the session-factory");

      // XXX: set parameters
    }
  }

  /**
   * Set the OTM session. Called by spring's bean wiring. 
   * 
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Set the journal service. Called by spring's bean wiring. 
   * 
   * @param journalService the journal service
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }
}
