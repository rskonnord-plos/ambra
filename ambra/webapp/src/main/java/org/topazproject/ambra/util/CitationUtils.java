/* $HeadURL::                                                                            $
 * $Id::$
 *
 * Copyright (c) 2008 by Topaz, Inc.
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.topazproject.ambra.annotation.service.WebAnnotation;
import org.topazproject.ambra.article.service.Author;
import org.topazproject.ambra.article.service.CitationInfo;
import org.topazproject.ambra.configuration.ConfigurationStore;

/**
 * CitationUtils - General citation related utility methods.
 * @author jkirton
 */
public abstract class CitationUtils {

  private static final int MAX_AUTHORS_TO_DISPLAY = 5;

  /**
   * Date format used in citation strings.
   */
  private static final DateFormat dateFormat = new SimpleDateFormat("yyyy");

  /**
   * Appends to the given {@link StringBuffer} the article authors in a prescribed format.
   * @param ci CitationInfo
   * @param sb StringBuffer to which the authors String is appended
   * @param correction Is this for an article correction citation?
   */
  private static void handleAuthors(CitationInfo ci, StringBuffer sb, boolean correction) {
    // obtain a list of all author names
    Author[] authors = ci.getAuthors();
    if(authors != null) {
      int i = 0;
      for(Author a : authors) {

        sb.append(a.getSurname());
        sb.append(' ');

        if(a.getSuffix() != null) {
          sb.append(a.getSuffix());
          sb.append(' ');
        }

        String gns = a.getGivenNames();
        if(gns != null) {
          // for formal corrections, we want the initial of the last given name followed by a period (.)
          // whereas for article citations, we want each the initial of each given name concatenated with no periods
          String[] givenNames = gns.split(" ");
          int gnc = 0;
          for(String gn :givenNames) {
            if (gn.length() > 0 && ((correction && gnc++ == givenNames.length - 1) || !correction)) {
              if(gn.matches(".*\\p{Pd}\\p{Lu}.*")) {
                String[] sarr = gn.split("\\p{Pd}");
                sb.append(sarr[0].charAt(0));
              }
              else {
                sb.append(gn.charAt(0));
              }
              if(correction) sb.append('.');
            }
          }
        }

        if(i < authors.length - 1) sb.append(", ");

        if(++i == MAX_AUTHORS_TO_DISPLAY) {
          break;
        }

      }//authors

      if(authors.length > MAX_AUTHORS_TO_DISPLAY) {
        sb.append(" et al.");
      }
      sb.append(' ');
    }
  }

  /**
   * Generates the citation string.
   * @param ci The {@link CitationInfo}
   * @return String
   */
  public static String generateArticleCitationString(CitationInfo ci) {
    if(ci == null) return null;

    StringBuffer sb = new StringBuffer(1024);

    handleAuthors(ci, sb, false);

    // publication date
    synchronized(dateFormat) {
      sb.append(dateFormat.format(ci.getPublicationDate()));
    }
    sb.append(' ');

    // article title
    sb.append(ci.getArticleTitle());
    sb.append(". ");

    // journal title
    sb.append(ci.getJournalTitle());
    sb.append(" ");

    // volume
    sb.append(ci.getVolume());

    // issue
    sb.append('(');
    sb.append(ci.getIssue());
    sb.append(')');

    // start page
    sb.append(": ");
    sb.append(ci.getStartPage());
    sb.append(' ');

    // doi
    sb.append("doi:");  // XXX: Fixing #921 should get rid of this.
    sb.append(ci.getDOI());

    return sb.toString();
  }

  /**
   * Assembles a String representing an annotation citatation based on a prescribed format.
   * <p>
   * FORMAT:
   * <p>
   * {first five authors of the article}, et al. (<Year the annotation was created>) Correction:
   * {article title}. {journal abbreviated name} {annotation URL}
   *
   * @param ci
   *          The {@link CitationInfo} pertaining to the article.
   * @param wa
   *          The {@link WebAnnotation}.
   * @return A newly created article annotation citation String. <br>
   *         Refer to: <a href="http://wiki.plos.org/pmwiki.php/Topaz/Corrections"
   *         >http://wiki.plos.org/pmwiki.php/Topaz/Corrections</a> for the format specification.
   */
  public static String generateArticleCorrectionCitationString(CitationInfo ci, WebAnnotation wa) {
    assert ci != null;
    assert wa != null;

    StringBuffer sb = new StringBuffer(1024);

    // authors
    handleAuthors(ci, sb, true);

    // comment post date
    sb.append(" (");
    synchronized (dateFormat) {
      sb.append(dateFormat.format(wa.getCreatedAsDate()));
    }
    sb.append(") ");

    sb.append("Correction: ");

    // article title
    sb.append(ci.getArticleTitle());
    sb.append(". ");

    // journal title
    sb.append(ci.getJournalTitle());
    sb.append(": ");

    // annotation URI
    sb.append(ConfigurationStore.getInstance().getConfiguration().getString(
        "ambra.platform.doiUrlPrefix"));
    sb.append(StringUtils.replace(wa.getId(), ConfigurationStore.getInstance().getConfiguration()
                                                      .getString("ambra.aliases.doiPrefix"), ""));

    return sb.toString();
  }

}
