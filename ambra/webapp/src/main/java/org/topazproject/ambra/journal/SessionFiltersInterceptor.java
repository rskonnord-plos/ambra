/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static Logger log = LoggerFactory.getLogger(SessionFiltersInterceptor.class);

  // FIXME: This is not thread safe. Interceptors are singletons and must be stateless.
  // SessionFactory can change while interceptor is executing. See ticket #1302
  private Session        session;
  private JournalService journalService;

  public String intercept(ActionInvocation invocation) throws Exception {
    setupFilters();
    return invocation.invoke();
  }

  private void setupFilters() throws ServletException {

    String jName = journalService.getCurrentJournalName();
    if (jName == null)
      return;

    Set<String> jf = journalService.getFilters(jName);
    if (jf == null)
      log.error("No journal found with name '" + jName + "'");

    // Guard against jf == null, we don't have any journals yet, allow development to continue
    for (String fName : (jf != null ? jf : new HashSet<String>(0))) {
      Filter f = session.enableFilter(fName);
      if (f == null)            // shouldn't happen
        throw new ServletException("Filter '" + fName +
            "' has not been registered with the session-factory");

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
