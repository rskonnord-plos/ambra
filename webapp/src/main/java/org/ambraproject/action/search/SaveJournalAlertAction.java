/*
 * Copyright (c) 2006-2013 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
package org.ambraproject.action.search;

import org.ambraproject.models.UserProfile;
import org.ambraproject.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
* Store a journal alert from a JSON request
*
* @author Joe Osowski
**/
public class SaveJournalAlertAction extends BaseSearchAction {
  private static final Logger log = LoggerFactory.getLogger(SaveJournalAlertAction.class);
  private UserService userService;
  private String category;

  /**
   * Save the user's search
   * @return
   */
  @Transactional(rollbackFor = { Throwable.class })
  @SuppressWarnings("unchecked")
  public String execute() {
    final UserProfile user = getCurrentUser();

    if (user == null) {
      log.info("User is null for saving search");
      addActionError("You must be logged in");

      return ERROR;
    }

    List<String> newCategories = userService.getJournalAlertSubjects(user.getID(), this.getCurrentJournal());

    if (newCategories.contains(this.category)) {
      //Have to keep message short to not break the UI
      addActionError("This journal alert already defined.");
      return INPUT;
    }

    //Add new category to the list of subjects the user is looking for
    newCategories.add(this.category);

    userService.setFilteredWeeklySearchAlert(user.getID(),
        newCategories.toArray(new String[newCategories.size()]),
        this.getCurrentJournal());

    return SUCCESS;
  }

  /**
   * Unsubscribe the user's search
   * @return
   */
  @Transactional(rollbackFor = { Throwable.class })
  @SuppressWarnings("unchecked")
  public String executeUnsubscribe() {
    final UserProfile user = getCurrentUser();

    if (user == null) {
      log.info("User is null for unsubscribe alert");
      addActionError("You must be logged in");

      return ERROR;
    }

    List<String> subjects = userService.getJournalAlertSubjects(user.getID(), this.getCurrentJournal());
    if (category != null && subjects.contains(category)) {
      subjects.remove(category);
      userService.setFilteredWeeklySearchAlert(user.getID(), subjects.toArray(new String[subjects.size()]), this.getCurrentJournal());
    }

    return SUCCESS;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  @Required
  public void setUserService(UserService userService) {
    this.userService = userService;
  }
}
