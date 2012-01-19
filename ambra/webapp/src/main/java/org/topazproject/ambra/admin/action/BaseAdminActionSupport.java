/* $$HeadURL::                                                                            $$
 * $$Id$$
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

package org.topazproject.ambra.admin.action;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.admin.service.DocumentManagementService;
import org.topazproject.ambra.admin.service.FlagManagementService;
import org.topazproject.ambra.admin.service.FlaggedCommentRecord;

@SuppressWarnings("serial")
public class BaseAdminActionSupport  extends BaseActionSupport {
  private static final Log log = LogFactory.getLog(BaseAdminActionSupport.class);

  private Collection<String> uploadableFiles;
  private Collection<String> publishableFiles;
  private Collection<FlaggedCommentRecord> flaggedComments;

  private DocumentManagementService documentManagementService;
  private FlagManagementService flagManagementService;

  protected String base() {
    // catch all Exceptions to keep Admin console active (vs. Site Error)
    try {
      uploadableFiles = documentManagementService.getUploadableFiles();
      publishableFiles = documentManagementService.getPublishableFiles();
      flaggedComments = flagManagementService.getFlaggedComments();
    } catch (Exception e) {
      log.error("Admin console Exception", e);
      addActionError("Exception: " + e);
      return ERROR;
    }
    return SUCCESS;
  }

  public void setDocumentManagementService(DocumentManagementService documentManagementService) {
    this.documentManagementService = documentManagementService;
  }

  protected DocumentManagementService getDocumentManagementService() {
    return documentManagementService;
  }

  public Collection<String> getUploadableFiles() {
    return uploadableFiles;
  }

  public Collection<String> getPublishableFiles() {
    return publishableFiles;
  }

  public Collection<FlaggedCommentRecord> getFlaggedComments() {
    return flaggedComments;
  }

  public void setFlagManagementService(FlagManagementService flagManagementService) {
    this.flagManagementService = flagManagementService;
  }
}
