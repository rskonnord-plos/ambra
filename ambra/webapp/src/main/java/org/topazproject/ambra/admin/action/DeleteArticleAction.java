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

package org.topazproject.ambra.admin.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DeleteArticleAction extends BaseAdminActionSupport {
  private static final Log log = LogFactory.getLog(DeleteArticleAction.class);
  private String article;

  public String execute() throws Exception {
    try {
      getDocumentManagementService().delete(article);
      addActionMessage("Successfully deleted article: " + article);

      try {
        getDocumentManagementService().revertIngestedQueue(article);
      } catch (Exception ioe) {
        log.warn("Error cleaning up spool directories for '" + article +
            "' - manual cleanup required", ioe);
      }
    } catch (Exception e) {
      addActionError("Failed to successfully delete article: " + article + ". <br>" + e);
      log.error("Failed to successfully delete article: " + article, e);
    }

    return base();
  }

  public void setArticle(String a) {
    article = a;
  }
}
