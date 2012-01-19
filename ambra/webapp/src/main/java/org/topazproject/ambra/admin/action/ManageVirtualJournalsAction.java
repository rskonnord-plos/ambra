/* $$HeadURL::                                                                            $$
 * $$Id$$
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

package org.topazproject.ambra.admin.action;

import java.net.URI;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.admin.service.AdminService;
import org.topazproject.ambra.admin.service.AdminService.JournalInfo;
import org.topazproject.ambra.models.Volume;

/**
 * Volumes are associated with some journals and hubs. A volume is an aggregation of
 * of issues. Issue are aggregations of articles.
 *
 */
@SuppressWarnings("serial")
public class ManageVirtualJournalsAction extends BaseAdminActionSupport {

  // Past in as parameters
  private String   command;
  private String   journalToModify;
  private URI      curIssueURI;
  private URI      volumeURI;
  private String[] volsToDelete;
  private String   displayName;

  //Used by template
  private List<Volume> volumes;
  private JournalInfo  journalInfo;

  // Necessary services
  private AdminService adminService;

  private static final Log log = LogFactory.getLog(ManageVirtualJournalsAction.class);

 /**
  * Enumeration used to dispatch commands within the action.
  */
  public enum MVJ_COMMANDS {
    UPDATE_ISSUE,
    CREATE_VOLUME,
    REMOVE_VOLUMES,
    INVALID;

    /**
     * Convert a string specifying a command to its
     * enumerated equivalent.
     *
     * @param command  string value to convert.
     * @return        enumerated equivalent
     */
    public static MVJ_COMMANDS toCommand(String command) {
      MVJ_COMMANDS a;
      try {
        a = valueOf(command);
      } catch (Exception e) {
        // It's ok just return invalid.
        a = INVALID;
      }
      return a;
    }
  }

  /**
   * Manage Journals.  Display Journals and processes all add/deletes.
   */
  @Override
  @Transactional(rollbackFor = { Throwable.class })
  public String execute() throws Exception  {

    switch( MVJ_COMMANDS.toCommand(command)) {
      case UPDATE_ISSUE: {
        try {
          if (curIssueURI != null) {
            // TODO:: Check to see if it actually exit
            adminService.setJrnlIssueURI(curIssueURI);
            addActionMessage("Current Issue (URI) set to: " + curIssueURI);
          } else {
            addActionMessage("Invalid Current Issue (URI) ");
          }
        } catch (Exception e) {
          addActionMessage("Current Issue not updated due to the following error.");
          addActionMessage(e.getMessage());
        }
        break;
      }
      case CREATE_VOLUME: {
        try {
          if (volumeURI != null) {
            // Create and add to journal
            Volume v = adminService.createVolume(volumeURI, displayName, "" );
            if (v != null) {
              addActionMessage("Created Volume: " + v.getId());
            } else {
              addActionMessage("Duplicate Volume URI: " + volumeURI);
            }
          } else {
            //Somebody failed to be valid report it.
            if (volumeURI == null) {
              addActionMessage("Invalid Volume URI" );
            }
          }
        } catch (Exception e) {
          addActionMessage("Volume not created due to the following error.");
          addActionMessage(e.getMessage());
        }
        break;
      }
      case REMOVE_VOLUMES: {
        try {
          if (volsToDelete.length > 0) {
              // volsToDelete was supplied by the system so they should be correct
              addActionMessage("Remvoing the Following Volume URIs:");
              for(String vol : volsToDelete) {
                adminService.deleteVolume(URI.create(vol));
                addActionMessage("Volume: " + vol );
              }
          }
        } catch (Exception e){
          addActionMessage("Volume remove failed due to the following error.");
          addActionMessage(e.getMessage());
        }
        break;
       }
       case INVALID:
         break;
    }

    volumes = adminService.getVolumes();
    journalInfo = adminService.createJournalInfo();
    return SUCCESS;
  }

  /**
   * Gets a list of Volume objects associated with the journal.
   *
   * @return list of Volume objects associated with the journals.
   */
  public List<Volume> getVolumes() {
    return volumes;
  }

  /**
   * Gets the JournalInfo value object for access in the view.
   *
   * @return Current virtual Journal value object.
   */
  public JournalInfo getJournal() {
    return journalInfo;
  }

  /**
   * Set Journal to modify.
   *
   * @param journalToModify Journal to modify.
   */
  public void setJournalToModify(String journalToModify) {
    this.journalToModify = journalToModify.trim();
  }

  /**
   * Set volume URI.
   *
   * @param vol the volume URI.
   */
  public void setVolumeURI(String vol) {
    try {
      this.volumeURI =  URI.create(vol.trim());
    } catch (Exception e) {
      this.volumeURI = null;
    }
  }

  /**
   * Get current issue.
   *
   * @return current issue.
   */
  public String getCurIssue() {
    return curIssueURI.toString();
  }

  /**
   * Set current issue.
   *
   * @param currentIssueURI the current issue for this journal.
   */
  public void setCurrentIssueURI(String currentIssueURI) {
    try {
      this.curIssueURI = URI.create(currentIssueURI.trim());
    } catch (Exception e) {
      this.curIssueURI = null;
    }
  }

   /**
   * Set display name for a voulume.
   *
   * @param dsplyName the display of the volume.
   */
  public void setDisplayName(String dsplyName) {
    this.displayName = dsplyName.trim();
  }

  /**
   * Set volumes to delete.
   *
   * @param vols .
   */
  public void setVolsToDelete(String[] vols) {
    this.volsToDelete = vols;
  }

  /**
   * Sets the command to execute.
   *
   * @param  command the command to execute for this action.
   */
  @Required
  public void setCommand(String command) {
    this.command = command;                                                                 
  }

  /**
   * Sets the AdminService.
   *
   * @param  adminService The adminService to set.
   */
  @Required
  public void setAdminService(AdminService adminService) {
    this.adminService = adminService;
  }
}
