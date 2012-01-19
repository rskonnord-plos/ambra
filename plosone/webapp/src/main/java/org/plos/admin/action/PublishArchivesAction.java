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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.action.BaseActionSupport;
import org.plos.admin.service.DocumentManagementService;

import com.opensymphony.webwork.interceptor.ParameterAware;

public class PublishArchivesAction extends BaseAdminActionSupport {
  
  private static final Log log = LogFactory.getLog(PublishArchivesAction.class);
  private String[] articlesToPublish;
  private String[] articlesToDelete;
  
  public void setArticlesToPublish(String[] articles) {
    articlesToPublish = articles;
  }
  
  public void setArticlesToDelete(String[] articles) {
    articlesToDelete= articles;
  }
  
  public String execute() throws RemoteException, ApplicationException  {
    deleteArticles();
    publishArticles();
    return base();
  }
  
  public void publishArticles () throws RemoteException, ApplicationException  {
    if (articlesToPublish != null){
      for (String article : articlesToPublish) {
        try {
          getDocumentManagementService().publish(article);
          if (log.isDebugEnabled()) {
            log.debug("published article: " + article);
          }
          addActionMessage("Published: " + article);
        } catch (Exception e) {
          addActionMessage("Error publishing: " + article + " - " + e.toString());
          log.warn ("Could not publish article: " + article, e);
        }
      }
    }
  }
  
  public void deleteArticles() throws RemoteException, ApplicationException  {
    if (articlesToDelete != null) {
      for (String article : articlesToDelete) {
        try {
          getDocumentManagementService().delete(article);
          if (log.isDebugEnabled()) {
            log.debug("deleted article: " + article);
          }
          addActionMessage("Deleted: " + article);
        } catch (Exception e) {
          addActionMessage("Error deleting: " + article + " - " + e.toString());
          log.warn ("Could not delete article: " + article, e);        
        }
      } 
    }
  }
}
