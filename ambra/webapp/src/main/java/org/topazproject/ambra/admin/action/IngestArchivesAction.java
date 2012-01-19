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

import java.io.File;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.article.service.DuplicateArticleIdException;
import org.topazproject.ambra.article.service.SearchUtil;
import org.topazproject.ambra.models.Article;
import org.topazproject.otm.Session;
import org.springframework.beans.factory.annotation.Required;

public class IngestArchivesAction extends BaseAdminActionSupport {
  private static final Log log = LogFactory.getLog(IngestArchivesAction.class);
  private String[] filesToIngest;
  private boolean  force = false;
  private Session session;

  public void setFilesToIngest(String[] files) {
    filesToIngest = files;
  }

  public void setForce(boolean flag) {
    force = flag;
  }

  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  public String execute() throws RemoteException, ApplicationException {
    if (filesToIngest != null) {
      for (String filename : filesToIngest) {
        filename = filename.trim();
        try {
          File file = new File(getDocumentManagementService().getDocumentDirectory(), filename);
          Article article = getDocumentManagementService().ingest(file, force);
          String id = article.getId().toString();
          addActionMessage("Ingested: " + filename);

          // FIXME: hack until ingest can directly put into search
          try {
            SearchUtil.index(id);
          } catch (Exception e) {
            addActionError("Error updating search index for '" + id + "': " + getMessages(e));
          }
          session.evict(article);  // purely for performance. Subsequent flush()es will be faster.
          getDocumentManagementService().generateIngestedData(file, article);
        } catch (DuplicateArticleIdException de) {
          addActionError("Error ingesting: " + filename + " - " + getMessages(de));
          log.error("Error ingesting article: " + filename , de);
        } catch (Exception e) {
          addActionError("Error ingesting: " + filename + " - " + getMessages(e));
          log.error("Error ingesting article: " + filename, e);
        }
      }
    }
    return base();
  }

  private static String getMessages(Throwable t) {
    StringBuilder msg = new StringBuilder();
    while (t != null) {
      msg.append(t.toString());
      t = t.getCause();
      if (t != null)
        msg.append("<br/>\n");
    }
    return msg.toString();
  }
}
