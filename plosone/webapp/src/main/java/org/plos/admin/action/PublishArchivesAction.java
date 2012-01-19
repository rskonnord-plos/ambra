/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.action;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;

import org.plos.ApplicationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.util.ServletContextAware;

public class PublishArchivesAction extends BaseAdminActionSupport implements ServletContextAware {
  private static final Log log = LogFactory.getLog(PublishArchivesAction.class);

  private String[] articlesToPublish;
  private String[] articlesInVirtualJournals;
  private String[] articlesToDelete;
  private ServletContext servletContext;

  /**
   * Deletes and publishes checked articles from the admin console.  Note that delete has priority
   * over publish.
   *
   */
  public String execute() throws RemoteException, ApplicationException {
    deleteArticles();
    publishArticles();
    return base();
  }

  /**
   * Publishes articles from the admin console.
   */
  public void publishArticles() {
    if (articlesToPublish == null)
      return;

    Map<String, Set<String>> vjMap = new HashMap<String, Set<String>>();
    if (articlesInVirtualJournals != null) {
      for (String articleInVirtualJournal : articlesInVirtualJournals) {
        // form builds checkbox value as "article" + "::" + "virtualJournal"
        String[] parts = articleInVirtualJournal.split("::");
        Set<String> vjList = vjMap.get(parts[0]);
        if (vjList == null)
          vjMap.put(parts[0], vjList = new HashSet<String>());
        vjList.add(parts[1]);
      }
    }

    List<String> msgs = getDocumentManagementService().publish(articlesToPublish, vjMap);
    for (String msg : msgs)
      addActionMessage(msg);
  }

  /**
   * Deletes the checked articles from the admin console.
   */
  public void deleteArticles() {
    if (articlesToDelete == null)
      return;

    List<String> msgs = getDocumentManagementService().delete(articlesToDelete, servletContext);
    for (String msg : msgs)
      addActionMessage(msg);
  }

  /**
   *
   * @param articles array of articles to publish
   */
  public void setArticlesToPublish(String[] articles) {
    articlesToPublish = articles;
  }

  /**
   *
   * @param articlesInVirtualJournals array of ${virtualJournal} + "::" + ${article} to publish.
   */
  public void setArticlesInVirtualJournals(String[] articlesInVirtualJournals) {
    this.articlesInVirtualJournals = articlesInVirtualJournals;
  }

  /**
   *
   * @param articles array of articles to delete
   */
  public void setArticlesToDelete(String[] articles) {
    articlesToDelete= articles;
  }

  /**
   * Sets the servlet context.  Needed in order to clear the image cache
   *
   * @param context SerlvetContext to set
   */
  public void setServletContext (ServletContext context) {
    this.servletContext = context;
  }
}
