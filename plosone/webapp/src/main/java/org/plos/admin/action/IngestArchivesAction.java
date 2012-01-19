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

import java.io.File;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.admin.service.ImageResizeException;

import org.plos.article.util.DuplicateArticleIdException;

public class IngestArchivesAction extends BaseAdminActionSupport {

  private static final Log log = LogFactory.getLog(IngestArchivesAction.class);
  private String[] filesToIngest;

  public void setFilesToIngest(String[] files) {
    filesToIngest = files;
  }

  public String execute() throws RemoteException, ApplicationException {
    if (filesToIngest != null) {
      String articleURI = null;
      for (String filename : filesToIngest) {
        articleURI = null;
        filename = filename.trim();
        try {
          articleURI = getDocumentManagementService().ingest(new File(
              getDocumentManagementService().getDocumentDirectory(),
              filename));
          addActionMessage("Ingested: " + filename);
        } catch (DuplicateArticleIdException de) {
          addActionMessage("Error ingesting: " + filename + " - " + de.toString());
          log.info("Error ingesting article: " + filename , de);
        } catch (ImageResizeException ire) {
          addActionMessage("Error ingesting: " + filename + " - " + ire.getCause().toString());
          log.error("Error ingesting articles: " + filename, ire);
          articleURI = ire.getArticleURI().toString();
          log.debug("trying to delete: " + articleURI);
          try {
            getDocumentManagementService().delete(articleURI);
          } catch (Exception deleteException) {
            log.error("Could not delete article: " + articleURI, deleteException);
          }
        } catch (Exception e) {
          addActionMessage("Error ingesting: " + filename + " - " + e.toString());
          log.error("Error ingesting article: " + filename, e);
        }
      }
    }
    return base();
  }
}
