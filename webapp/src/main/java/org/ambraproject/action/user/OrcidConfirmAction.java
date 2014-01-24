/*
* Copyright (c) 2006-2014 by Public Library of Science
*
* http://plos.org
* http://ambraproject.org
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.ambraproject.action.user;

import org.ambraproject.models.UserProfile;
import org.ambraproject.service.user.DuplicateOrcidException;
import org.ambraproject.views.OrcidAuthorization;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import static org.ambraproject.Constants.AMBRA_USER_KEY;

/**
 * Confirm and save the user's orcid information
 */
public class OrcidConfirmAction extends EditUserAction {
  private static final Logger log = LoggerFactory.getLogger(OrcidConfirmAction.class);
  private String code;

  /**
   * If access has been denied, orcid will return these values
   */
  private String error;
  private String error_description;

  @Override
  public String execute() throws Exception {
    //Call parent execute to grab all page variables
    super.execute();

    //If error is set, user denied us access to their profile data
    //Just pass through, display error in ftl

    if(error == null) {
      Map<String, Object> session = ServletActionContext.getContext().getSession();
      UserProfile user = (UserProfile)session.get(AMBRA_USER_KEY);

      if(user == null) {
        //User not authenticated
        //Some how the user got here with out this?  URL Hacking?
        return LOGIN;
      } else {
        //on bad config an exception will be thrown, just let it pass through
        OrcidAuthorization orcidAuthorization = orcidService.authorizeUser(this.code);

        if(orcidAuthorization == null) {
          //Handle user access denied and site down handled the same way
          error = "System error";
          error_description = "There was a system error when authenticating your ORCiD";
        } else {
          try {
            this.userService.saveUserOrcid(user.getID(), orcidAuthorization);
            this.orcid = orcidAuthorization.getOrcid();
          } catch(DuplicateOrcidException ex) {
            log.error(ex.getMessage(), ex);
            error = "System error";
            error_description = ex.getMessage();
          }
          log.debug("User authenticated via ORCiD {}", this.orcid);
        }
      }
    }

    return SUCCESS;
  }

  /**
   * Here to let freemarker know that this is the confirmed OrcidConfim action and to display a notification
   * That the account has been confirmed (or not)
   *
   * @return true
   */
  public boolean getOrcidConfirm() {
    return true;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getError_description() {
    return error_description;
  }

  public void setError_description(String error_description) {
    this.error_description = error_description;
  }
}
