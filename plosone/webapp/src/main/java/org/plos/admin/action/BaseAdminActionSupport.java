/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.action;

import java.rmi.RemoteException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.admin.service.DocumentManagementService;
import org.plos.admin.service.FlagManagementService;
import org.plos.ApplicationException;

public class BaseAdminActionSupport  extends BaseActionSupport {
  
  private static final Log log = LogFactory.getLog(BaseAdminActionSupport.class);
  
  private Collection uploadableFiles;
  private Collection publishableFiles;
  private Collection flaggedComments;
  
  private DocumentManagementService documentManagementService;
  private FlagManagementService flagManagementService;
  
  protected String base() throws RemoteException, ApplicationException {
    uploadableFiles = documentManagementService.getUploadableFiles();
    publishableFiles = documentManagementService.getPublishableFiles();
    flaggedComments = flagManagementService.getFlaggedComments();
    return SUCCESS;
  }	
  
  public void setDocumentManagementService(DocumentManagementService documentManagementService) {
    this.documentManagementService = documentManagementService;
  }
  
  protected DocumentManagementService getDocumentManagementService() {
    return documentManagementService;
  }
  
  public Collection getUploadableFiles() {
    return uploadableFiles;
  }
  
  public Collection getPublishableFiles() {
    return publishableFiles;
  }	
  
  public Collection getFlaggedComments() {
    return flaggedComments;
  }
  
  public void setFlagManagementService(FlagManagementService flagManagementService) {
    this.flagManagementService = flagManagementService;
  }
}
