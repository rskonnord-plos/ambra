/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Provides some useful text manipulation functions.
 */
public class TextUtils {
  public static final String HTTP_PREFIX = "http://";
  private static final Pattern maliciousContentPattern = Pattern.compile("[<>\"\'%;()&+]");
  private static final Pattern lineBreakPattern = Pattern.compile("\\p{Zl}|\r\n|\n|\u0085|\\p{Zp}");
  
  /**
   * Takes in a String and returns it with all line separators replaced by <br/> tags suitable
   * for display as HTML.
   * 
   * @param input
   * @return String with line separators replaced with <br/>
   */
  public static String makeHtmlLineBreaks (final String input) {
    if (StringUtils.isBlank(input)) {
      return input;
    }
    return lineBreakPattern.matcher(input).replaceAll("<br/>");
  }
  
  /**
   * Linkify any possible web links excepting email addresses and enclosed with <p> tags
   * @param text text
   * @return hyperlinked text
   */
  public static String hyperlinkEnclosedWithPTags(final String text) {
    final StringBuilder retStr = new StringBuilder("<p>");
    retStr.append(hyperlink(text));
    retStr.append("</p>");
    return (retStr.toString());
  }

  /**
   * Linkify any possible web links excepting email addresses
   * @param text text
   * @return hyperlinked text
   */
  public static String hyperlink(final String text) {
    if (StringUtils.isBlank(text)) {
      return text;
//      return "";
    } else {
      return com.opensymphony.util.TextUtils.linkURL(text);
    }
  }

  /**
   * Return the escaped html. Useful when you want to make any dangerous scripts safe to render.
   * @param bodyContent bodyContent
   * @return escaped html text
   */
  public static String escapeHtml(final String bodyContent) {
    return makeHtmlLineBreaks(StringEscapeUtils.escapeHtml(bodyContent));
  }

  /**
   * @param bodyContent bodyContent
   * @return Return escaped and hyperlinked text
   */
  public static String escapeAndHyperlink(final String bodyContent) {
    return hyperlinkEnclosedWithPTags(escapeHtml(bodyContent));
  }

  
  /**
   * Transforms an org.w3c.dom.Document into a String
   * 
   * @param node Document to transform
   * @return String representation of node
   * @throws TransformerException TransformerException
   */
  public static String getAsXMLString(final Node node) throws TransformerException {
    final Transformer tf = TransformerFactory.newInstance().newTransformer();
//    tf.setOutputProperty("indent", "yes");
    final StringWriter stringWriter = new StringWriter();
    tf.transform(new DOMSource(node), new StreamResult(stringWriter));
    return stringWriter.toString();
  }

  /**
   * @return whether the url is a valid address
   */
  public static boolean verifyUrl(final String url) {
    final String lowercaseUrl = url.toLowerCase();
    try {
      new URL(lowercaseUrl);
      //False if it only matches the protocol names.
      if (ArrayUtils.contains(new String[]{HTTP_PREFIX, "ftp://", "https://"}, url)) {
        return false;
      }
    } catch (MalformedURLException e) {
      return false;
    }
    return true;
//    return com.opensymphony.util.TextUtils.verifyUrl(url);
  }

  public static String makeValidUrl(final String url) throws Exception {
    String finalUrl = url;
    if (!verifyUrl(finalUrl)) {
      finalUrl = HTTP_PREFIX + finalUrl;
      if (!verifyUrl(finalUrl)) {
        throw new Exception("Invalid url:" + url);
      }
    }
    return finalUrl;
  }

  /**
   * Check if the input text is potentially malicious. For more details read;
   * http://www.dwheeler.com/secure-programs/Secure-Programs-HOWTO/cross-site-malicious-content.html
   * @param text text
   * @return boolean
   */
  public static boolean isPotentiallyMalicious(final String text) {
    return maliciousContentPattern.matcher(text).find();
  }
}
