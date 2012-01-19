/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.plos.annotation.service.WebAnnotation;
import org.plos.article.service.Author;
import org.plos.article.service.CitationInfo;

/* ORIGINAL FREEMARKER CODE:
<#assign gt5 = false />
<#list citation.authors as author>
  <#if author_index gt 4>
    <#assign gt5 = true>
    <#break>
  </#if>
  <#assign gn = author.givenNames?word_list />
  <#assign allNames = []>
  <#list gn as n>
    <#if n?matches(".*\\p{Pd}\\p{Lu}.*")>
      <#assign names = n?split("\\p{Pd}",'r') />
      <#assign allNames = allNames + names />
    <#else>
      <#assign temp = [n]>
      <#assign allNames = allNames + temp>
    </#if>
  </#list>
  ${author.surname} <#if author.suffix?exists>${author.suffix}</#if> <#list allNames as n>${n[0]}</#list><#if author_has_next>,</#if>
</#list>
<#if gt5>et al.</#if>
(${citation.publicationDate?string("yyyy")}) ${citation.articleTitle}. ${citation.journalTitle} 
${citation.volume}(${citation.issue}): ${citation.startPage} doi:${citation.DOI}
*/

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
   * @param initializeGivenNames
   */
  private static void handleAuthors(CitationInfo ci, StringBuffer sb, boolean initializeGivenNames) {
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
          String[] givenNames = gns.split(" ");
          for(String gn :givenNames) {
            if(gn.matches(".*\\p{Pd}\\p{Lu}.*")) {
              String[] sarr = gn.split("\\p{Pd}");
              String fistGivenName  = sarr[0];
              if(initializeGivenNames) {
                sb.append(fistGivenName.charAt(0));
                sb.append('.');
              }
              else {
                sb.append(fistGivenName);
              }
            }
            else {
              if(initializeGivenNames) {
                sb.append(gn.charAt(0));
                sb.append('.');
              }
              else {
                sb.append(gn);
              }
              break;
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
   * @param foramt The desired citation string format.
   * @return String
   */
  public static String generateArticleCitationString(CitationInfo ci) {
    if(ci == null) return null;
    
    StringBuffer sb = new StringBuffer(1024);
    
    handleAuthors(ci, sb, true);
    
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
    sb.append("doi:");
    sb.append(ci.getDOI());
    
    return sb.toString();
  }

  /**
   * Assembles a String representing an annotation citatation based on a
   * prescribed format.
   * <p>
   * FORMAT:
   * <p>
   * {first five authors of the article}, et al. (<Year the annotation was
   * created>) Correction: {article title}. {journal abbreviated name}
   * {annotation URL}
   * 
   * @param ci The {@link CitationInfo} pertaining to the article.
   * @param wa The {@link WebAnnotation}.
   * @return A newly created article annotation citation String.
   * @see http://wiki.plos.org/pmwiki.php/Topaz/Corrections for the format
   *      specification
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
    sb.append("http://dx.plos.org");
    sb.append(StringUtils.replace(wa.getId(), "info:doi", ""));

    return sb.toString();
  }

}
