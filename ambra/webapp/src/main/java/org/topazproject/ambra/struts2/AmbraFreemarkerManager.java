/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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

package org.topazproject.ambra.struts2;

import java.io.IOException;
import java.io.Reader;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.views.freemarker.FreemarkerManager;
import org.apache.struts2.views.freemarker.ScopesHashModel;
import org.topazproject.ambra.util.AuthorNameAbbreviationDirective;
import org.topazproject.ambra.util.ArticleFormattingDirective;
import org.topazproject.ambra.util.SimpleTextDirective;

import com.opensymphony.xwork2.util.ValueStack;

import freemarker.cache.TemplateLoader;
import freemarker.cache.StatefulTemplateLoader;
import freemarker.template.TemplateException;
import freemarker.template.Configuration;

/**
 * Custom Freemarker Manager to load up the configuration files for css, javascript, and titles of
 * pages
 *
 * @author Stephen Cheng
 */
public class AmbraFreemarkerManager extends FreemarkerManager {
  private AmbraFreemarkerConfig fmConfig;

  /**
   * Sets the custom configuration object via Spring injection
   *
   * @param fmConfig Freemarker configuration
   */
  public AmbraFreemarkerManager(AmbraFreemarkerConfig fmConfig) {
    this.fmConfig = fmConfig;
  }

  /**
   * Subclass from parent to add the freemarker configuratio object globally
   *
   * @see org.apache.struts2.views.freemarker.FreemarkerManager
   */
  @Override
  protected void populateContext(ScopesHashModel model, ValueStack stack, Object action,
                                 HttpServletRequest request, HttpServletResponse response) {
    super.populateContext(model, stack, action, request, response);
    model.put("freemarker_config", fmConfig);
  }

  @Override
  protected TemplateLoader getTemplateLoader(ServletContext context) {
    final TemplateLoader s = super.getTemplateLoader(context);

    return new StatefulTemplateLoader() {
      public void closeTemplateSource(Object source) throws IOException {
        s.closeTemplateSource(source);
      }

      public Object findTemplateSource(String name) throws IOException {

        // requests are in form journals/<journal_name>/<package>/template.ftl

        Object templateSource;
        String templateName = name;

        // First: look in journal-specific override
        if (name.startsWith("journals")) {
          templateSource = s.findTemplateSource(name);
          if (templateSource != null)
            return templateSource;

          // Second: look in journal-specific folders the way they are packed in jars
          templateSource = s.findTemplateSource("struts/" + name);
          if (templateSource != null)
            return templateSource;


          templateName = AmbraFreemarkerConfig.trimJournalFromTemplatePath(name);
        }

        // Third: look in plos default folders
        templateSource = s.findTemplateSource("journals/plosJournals/" + templateName);
        if (templateSource != null)
          return templateSource;

        // Fourth: look in the ambra default folders
        templateSource = s.findTemplateSource(templateName);
        if (templateSource != null)
          return templateSource;

        // Fifth: look at the path for templates (does not start with "journals")
        templateSource = s.findTemplateSource(name);
        if (templateSource != null)
          return templateSource;

        /*
         * Sixth: try struts default theme
         * FIXME: theme inheritance is hard coded
         * NOTE: The real fix is in struts. See WW-1832
         */
        if (name.indexOf("ambra-theme") >= 0)
          return s.findTemplateSource(name.replace("ambra-theme", "simple"));
        else
          return null;

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

  /**
   * Attaches custom Freemarker directives as shared variables.
   *
   * @param servletContext Servlet context.
   * @return Freemarker configuration.
   * @throws TemplateException
   */
  @Override
  protected Configuration createConfiguration(ServletContext servletContext) throws TemplateException {
    Configuration configuration = super.createConfiguration(servletContext);
    configuration.setSharedVariable("abbreviation", new AuthorNameAbbreviationDirective());
    configuration.setSharedVariable("articleFormat", new ArticleFormattingDirective());
    configuration.setSharedVariable("simpleText", new SimpleTextDirective());
    return configuration;
  }
}
