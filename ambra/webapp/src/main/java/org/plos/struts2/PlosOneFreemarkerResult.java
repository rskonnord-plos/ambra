/* $HeadURL::                                                                            $
 * $Id$
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
