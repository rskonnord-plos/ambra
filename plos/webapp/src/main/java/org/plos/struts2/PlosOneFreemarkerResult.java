/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.struts2;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.views.freemarker.FreemarkerResult;

import freemarker.template.SimpleHash;

/**
 * Custom Freemarker Result class so that we can pass the templateFile name into the template
 * in order to have a limited number of templates for the system.
 * 
 * @author Stephen Cheng
 *
 */
public class PlosOneFreemarkerResult extends FreemarkerResult {
  private String templateFile;
  private boolean noCache = false;
  /**
   * @return Returns the templateFile.
   */
  public String getTemplateFile() {
    return templateFile;
  }

  /**
   * @param templateFile The templateFile to set.
   */
  public void setTemplateFile(String templateFile) {
    this.templateFile = templateFile;
  }

  protected boolean preTemplateProcess(freemarker.template.Template template,
                                       freemarker.template.TemplateModel model) throws IOException{
    ((SimpleHash)model).put("templateFile", this.templateFile);
    if (noCache) {
      HttpServletResponse response = ServletActionContext.getResponse();
      // HTTP 1.1 browsers should defeat caching on this header
      response.setHeader("Cache-Control", "no-cache");
      // HTTP 1.0 browsers should defeat caching on this header
      response.setHeader("Pragma", "no-cache");
      // Last resort for those that ignore all of the above
      response.setHeader("Expires", "-1");
    }
    return super.preTemplateProcess(template, model);
  }

  /**
   * @return Returns the noCache.
   */
  public boolean getNoCache() {
    return noCache;
  }

  /**
   * @param noCache The noCache to set.
   */
  public void setNoCache(boolean noCache) {
    this.noCache = noCache;
  }
}
