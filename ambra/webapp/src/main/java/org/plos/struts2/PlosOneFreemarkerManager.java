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
import java.io.Reader;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.views.freemarker.FreemarkerManager;
import org.apache.struts2.views.freemarker.ScopesHashModel;

import com.opensymphony.xwork2.util.ValueStack;

import freemarker.cache.TemplateLoader;
import freemarker.cache.StatefulTemplateLoader;

/**
 * Custom Freemarker Manager to load up the configuration files for css, javascript, and titles of
 * pages
 *
 * @author Stephen Cheng
 */
public class PlosOneFreemarkerManager extends FreemarkerManager {
  private PlosOneFreemarkerConfig fmConfig;

  /**
   * Sets the custom configuration object via Spring injection
   *
   * @param fmConfig
   */
  public PlosOneFreemarkerManager(PlosOneFreemarkerConfig fmConfig) {
    this.fmConfig = fmConfig;
  }

  /**
   * Subclass from parent to add the freemarker configuratio object globally
   *
   * @see org.apache.struts2.views.freemarker.FreemarkerManager
   */
  protected void populateContext(ScopesHashModel model, ValueStack stack, Object action,
                                 HttpServletRequest request, HttpServletResponse response) {
    super.populateContext(model, stack, action, request, response);
    model.put("freemarker_config", fmConfig);
  }

  protected TemplateLoader getTemplateLoader(ServletContext context) {
    final TemplateLoader s = super.getTemplateLoader(context);
    return new StatefulTemplateLoader() {
      public void closeTemplateSource(Object source) throws IOException {
        s.closeTemplateSource(source);
      }

      public Object findTemplateSource(String name) throws IOException {
        Object r = s.findTemplateSource(name);
        if (r == null)
          r = s.findTemplateSource("struts/" + name);

        // FIXME: theme name and parent is hard coded
        // NOTE: The real fix is in struts. See WW-1961
        if (r == null)
          r = s.findTemplateSource(name.replace("ambra-theme", "simple"));

        return r;
      }

      public long getLastModified(Object source) {
        return s.getLastModified(source);
      }

      public Reader getReader(Object source, String encoding) throws IOException {
        return s.getReader(source, encoding);
      }

      public void resetState() {
        if (s instanceof StatefulTemplateLoader)
          ((StatefulTemplateLoader) s).resetState();
      }
    };
  }
}
