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
package org.topazproject.ambra.user.action;

import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.Constants;
import org.topazproject.ambra.user.AmbraUser;

import static org.topazproject.ambra.Constants.AMBRA_USER_KEY;

/**
 * User Profile Action that is called by the member user to update their profile
 * (distinct from the one that might be called by admin to edit a user profile)
 */
public class MemberUserProfileAction extends UserProfileAction {
  /**
   * Save the user and save the AmbraUser into Session
   * @return webwork status code
   * @throws Exception
   */
  @Override
  @Transactional(rollbackFor = { Throwable.class })
  public String executeSaveUser() throws Exception {
    final String statusCode = super.executeSaveUser();

    if (SUCCESS.equals(statusCode)) {
      session.put(AMBRA_USER_KEY, super.getSavedAmbraUser());
    }

    return statusCode;
  }

  @Override
  protected AmbraUser getAmbraUserToUse() {
    return getCurrentUser();
  }

  @Override
  protected String getUserIdToFetchEmailAddressFor() {
    return (String) session.get(Constants.SINGLE_SIGNON_USER_KEY);
  }
}
