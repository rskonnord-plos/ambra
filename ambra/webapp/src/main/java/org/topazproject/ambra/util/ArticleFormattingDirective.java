/* $HeadURL$
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

package org.topazproject.ambra.util;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;

import java.util.Map;
import java.io.IOException;
import java.io.Writer;

/**
 * Freemarker directive for converting XML formatting into HTML formatting.
 * <br/>
 * For example &lt;italic&gt;foo&lt;/italic&gt; is converted to &lt;i&gt;foo&lt;/i&gt;
 * <br/>
 * Conversion table:
 * <table border="1">
 *  <tr>
 *    <th>XML</th>
 *    <th>HTML</th>
 *  </tr>
 *  <tr>
 *    <td>&lt;italic&gt;</td><td>&lt;i&gt;</td>
 *  </tr>
 *  <tr>
 *    <td>&lt;bold&gt;</td><td>&lt;b&gt;</td>
 *  </tr>
 *  <tr>
 *    <td>&lt;monospace&gt;</td><td>&lt;span class="monospace"&gt;</td>
 *  </tr>
 *  <tr>
 *    <td>&lt;overline&gt;</td><td>&lt;span class="overline"&gt;</td>
 *  </tr>
 *  <tr>
 *    <td>&lt;sc&gt;</td><td>&lt;small&gt;</td>
 *  </tr>
 *  <tr>
 *    <td>&lt;strike&gt;</td><td>&lt;s&gt;</td>
 *  </tr>
 *  <tr>
 *    <td>&lt;underline&gt;</td><td>&lt;u&gt;</td>
 *  </tr>
 *  <tr>
 *    <td>&lt;named-content xmlns:xlink="http://www.w3.org/1999/xlink"
 * content-type="genus-species" xlink:type="simple"&gt;</td>
 *    <td>&lt;i&gt;</td>
 *  </tr>
 * </table>
 */
public class ArticleFormattingDirective implements TemplateDirectiveModel {
  public void execute(Environment environment, Map params, TemplateModel[] loopVars,
                      TemplateDirectiveBody body)
      throws TemplateException, IOException {

    if (!params.isEmpty()) {
      throw new TemplateModelException(
          "ArticleFormattingDirective doesn't allow parameters.");
    }

    if (loopVars.length != 0) {
      throw new TemplateModelException(
          "ArticleFormattingDirective doesn't allow loop variables.");
    }

    if (body != null) {
      body.render(new AmbraTextWriter(environment.getOut()));
    }
  }

  /**
   * A {@link java.io.Writer} that transforms the author name as character stream
   */
  private static class AmbraTextWriter extends Writer {
    private final Writer out;

    AmbraTextWriter(Writer out) {
      this.out = out;
    }

    public void write(char[] chars, int off, int len) throws IOException {
      out.write(format(new String(chars, off, len)));
    }

    public void flush() throws IOException {
      out.flush();
    }

    public void close() throws IOException {
      out.close();
    }
  }

  /**
   * Static mathod that does conversion. Can be used in Java code.
   * @param str input string.
   * @return converted string.
   */
  public static String format(String str) {
    if (str == null)
      return null;

    String result = str;

    result = result.replaceAll("<italic>", "<i>");
    result = result.replaceAll("</italic>", "</i>");

    result = result.replaceAll("&lt;italic&gt;", "<i>");
    result = result.replaceAll("&lt;/italic&gt;", "</i>");

    result = result.replaceAll("<named-content xmlns:xlink= \"http://www.w3.org/1999/xlink\" " +
        "content-type=\"genus-species\" xlink:type=\"simple\">", "<i>");
    result = result.replaceAll("</named-content>", "</i>");

    result = result.replaceAll("<bold>", "<b>");
    result = result.replaceAll("</bold>", "</b>");

    result = result.replaceAll("<monospace>", "<span class=\"monospace\">");
    result = result.replaceAll("</monospace>", "</span>");

    result = result.replaceAll("<overline>", "<span class=\"overline\">");
    result = result.replaceAll("</overline>", "</span>");

    result = result.replaceAll("<sc>", "<small>");
    result = result.replaceAll("</sc>", "</small>");

    result = result.replaceAll("<strike>", "<s>");
    result = result.replaceAll("</strike>", "</s>");

    result = result.replaceAll("<underline>", "<u>");
    result = result.replaceAll("</underline>", "</u>");

    return result;
  }
}
