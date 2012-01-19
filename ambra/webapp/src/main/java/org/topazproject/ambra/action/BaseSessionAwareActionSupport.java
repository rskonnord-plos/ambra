/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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

package org.topazproject.ambra.action;

import org.apache.struts2.interceptor.SessionAware;

import static org.topazproject.ambra.Constants.AMBRA_USER_KEY;

import org.topazproject.ambra.user.AmbraUser;

import java.util.Map;

/**
 * Struts2 action support class that is session aware.
 *
 * @author Dragisa krsmanovic
 */

public class BaseSessionAwareActionSupport extends BaseActionSupport implements SessionAware {
  protected Map session;

  public void setSession(Map map) {
    session = map;
  }

  /**
   * Get currently logged in user
   *
   * @return Logged in user object
   */
  protected AmbraUser getCurrentUser() {
    return (AmbraUser) session.get(AMBRA_USER_KEY);
  }
}
