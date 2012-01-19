/* $HeadURL::                                                                            $
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
package org.plos.user.action;

import org.springframework.transaction.annotation.Transactional;

import org.plos.user.PlosOneUser;
import static org.plos.Constants.PLOS_ONE_USER_KEY;
import org.plos.Constants;

import java.util.Map;

/**
 * User Profile Action that is called by the member user to update their profile
 * (distinct from the one that might be called by admin to edit a user profile)
 */
public class MemberUserProfileAction extends UserProfileAction {
  /**
   * Save the user and save the PlosOneUser into Session
   * @return webwork status code
   * @throws Exception
   */
  @Override
  @Transactional(rollbackFor = { Throwable.class })
  public String executeSaveUser() throws Exception {
    final String statusCode = super.executeSaveUser();

    if (SUCCESS.equals(statusCode)) {
      final Map<String, Object> sessionMap = getSessionMap();
      sessionMap.put(PLOS_ONE_USER_KEY, super.getSavedPlosOneUser());
    }

    return statusCode;
  }

  @Override
  protected PlosOneUser getPlosOneUserToUse() {
    final Map<String, Object> sessionMap = getSessionMap();
    return (PlosOneUser) sessionMap.get(PLOS_ONE_USER_KEY);
  }

  @Override
  protected String getUserIdToFetchEmailAddressFor() {
    final Map<String, Object> sessionMap = getSessionMap();
    return (String) sessionMap.get(Constants.SINGLE_SIGNON_USER_KEY);
  }
}
