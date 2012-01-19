/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.admin.action;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;

/**
 * Index all Articles.
 * 
 * @author jsuttor
 */
public class IndexArticlesAction extends BaseAdminActionSupport {

  private static final Log log = LogFactory.getLog(IndexArticlesAction.class);
  
  @Override
  public String execute() throws Exception {
    
    Map<String, String> results = new HashMap();
    try {
      results = getDocumentManagementService().indexArticles();
    } catch (ApplicationException ae) {
      final String errorMsg = "Exception during indexing";
      addActionError(errorMsg + ": " + ae);
      log.error(errorMsg, ae);
    }
    
    // pass results on to console
    addActionMessage("indexed " + results.size() + " Articles in current Journal");
    for (String key : results.keySet()) {
      addActionMessage(key + " : " + results.get(key));
    }
        
    return SUCCESS;
  }
}
