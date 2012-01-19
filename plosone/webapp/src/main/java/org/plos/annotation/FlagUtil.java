/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation;

import org.plos.ApplicationException;
import org.plos.util.TextUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;



/**
 * Util functions to be used for Flag related tasks like created and extracting flag attributes.
 */
public class FlagUtil {
  private static final String FLAG_NODE = "flag";
  private static final String REASON_CODE = "reasonCode";
  private static final String COMMENT_NODE = "comment";
  private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

  /**
   * Return the comment from an xml string
   * @param xmlDocument xmlDocument
   * @return the comment
   * @throws ApplicationException ApplicationException
   */
  public static String getComment(final String xmlDocument) throws ApplicationException {
    try {
      final Element root = getRootNode(xmlDocument);
      final Node reasonCode = root.getElementsByTagName(COMMENT_NODE).item(0);
      return reasonCode.getTextContent();
    } catch (Exception ex) {
      throw new ApplicationException(ex);
    }
  }

  /**
   * Return the reasonCode from an xml string
   * @param xmlDocument xmlDocument
   * @return the rason code
   * @throws ApplicationException ApplicationException
   */
  public static String getReasonCode(final String xmlDocument) throws ApplicationException {
    try {
      final Element root = getRootNode(xmlDocument);
      return root.getAttribute(REASON_CODE);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  private static Element getRootNode(final String xmlDocument) throws SAXException, IOException, ParserConfigurationException {
    final Document doc = documentBuilderFactory.newDocumentBuilder()
                            .parse(new ByteArrayInputStream(xmlDocument.getBytes("UTF-8")));
    return doc.getDocumentElement();
  }

  /**
   * Create the body as XML string for the flag comment given a reasonCode and a commentText
   * @param reasonCode reasonCode
   * @param commentText commentText
   * @return the flag body
   * @throws Exception Exception
   */
  public static String createFlagBody(final String reasonCode, final String commentText) throws Exception {
    final Document doc = documentBuilderFactory.newDocumentBuilder().newDocument();
    final Element rootElement = doc.createElement(FLAG_NODE);
    doc.appendChild(rootElement);

    rootElement.setAttribute(REASON_CODE, reasonCode);
    final Element commentElement = doc.createElement(COMMENT_NODE);
    commentElement.setTextContent(commentText);
    rootElement.appendChild(commentElement);

    return TextUtils.getAsXMLString(doc);
  }
}
