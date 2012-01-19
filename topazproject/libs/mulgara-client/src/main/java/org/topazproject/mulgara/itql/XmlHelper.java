/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.mulgara.itql;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Text;

/** 
 * Some XML parsing helpers.
 * 
 * @author Ronald Tschalär
 */
class XmlHelper {
  private XmlHelper() {
  }

  /** 
   * Helper to parse an XML string into a DOM.
   * 
   * @param xml the xml string
   * @return the document
   * @throws AnswerException if an error occured parsing the xml
   */
  public static Document parseXml(String xml) throws AnswerException {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setIgnoringComments(true);
    builderFactory.setCoalescing(true);

    DocumentBuilder builder;
    try {
      builder = builderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException pce) {
      throw new RuntimeException(pce);  // can this happen?
    }

    try {
      return builder.parse(new InputSource(new StringReader(xml)));
    } catch (IOException ioe) {
      throw new Error(ioe);     // can't happen
    } catch (SAXException se) {
      throw new AnswerException("Unexpected response: '" + xml + "'", se);
    }
  }

  /** 
   * Get all immediate element children with the given name. This differs from {@link
   * org.w3c.dom.Element#getElementsByTagName getElementsByTagName} in that this only returns
   * direct children, not all descendents.
   * 
   * @param parent    the parent node
   * @param childName the name of the children elements to get; may be null or "*" to indicate all
   *                  element children
   * @return the list of children; may be empty
   */
  public static NodeList getChildren(Element parent, String childName) {
    final NodeList children = parent.getChildNodes();
    final String   filter   = (childName != null && !childName.equals("*")) ? childName : null;

    return new NodeList() {
      private List elems;

      {
        elems = new ArrayList();
        for (int idx = 0; idx < children.getLength(); idx++) {
          Node n = children.item(idx);
          if (n.getNodeType() == Node.ELEMENT_NODE &&
              (filter == null || n.getNodeName().equals(filter)))
            elems.add(n);
        }
      }

      public Node item(int index) {
        return (Node) elems.get(index);
      }

      public int getLength() {
        return elems.size();
      }
    };
  }

  /** 
   * Get a single child element with the given name. If more than one child with the given name
   * exists, a warning is logged.
   * 
   * @param parent the parent node
   * @param name   the name of the child element to get; may be null or "*" to indicate any
   *               element child
   * @param log    where to write the warning if more than one child element was found; may be
   *               null to disable the warning
   * @return the child, or null if none was found
   */
  public static Element getOnlyChild(Element parent, String name, Log log) {
    NodeList children = getChildren(parent, name);
    if (children.getLength() == 0)
      return null;
    if (children.getLength() > 1 && log != null)
      log.warn("Expected exactly one child named '" + name + "' of '" + parent.getTagName() +
               "' but got " + children.getLength());
    return (Element) children.item(0);
  }

  /** 
   * Get the first child element with the given name.  This differs from {@link
   * org.w3c.dom.Node#getFirstChild Node.getFirstChild} in that this only returns elements.
   * 
   * @param parent the parent node for which to get the first child
   * @param name   the name of the child element to get; may be null or "*" to indicate the
   *               first child element of any name
   * @return the child, or null if none was found
   */
  public static Element getFirstChild(Element parent, String name) {
    final String filter = (name != null && !name.equals("*")) ? name : null;

    for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n.getNodeType() == Node.ELEMENT_NODE &&
          (filter == null || n.getNodeName().equals(filter)))
        return (Element) n;
    }

    return null;
  }

  /** 
   * Get the next sibling element with the given name.  This differs from {@link
   * org.w3c.dom.Node#getNextSibling Node.getNextSibling} in that this only returns elements.
   * 
   * @param node   the current node for which to get the next sibling
   * @param name   the name of the next sibling element to get; may be null or "*" to indicate the
   *               next sibling of any name
   * @return the sibling, or null if none was found
   */
  public static Element getNextSibling(Element node, String name) {
    final String filter = (name != null && !name.equals("*")) ? name : null;

    for (Node n = node.getNextSibling(); n != null; n = n.getNextSibling()) {
      if (n.getNodeType() == Node.ELEMENT_NODE &&
          (filter == null || n.getNodeName().equals(filter)))
        return (Element) n;
    }

    return null;
  }

  /** 
   * Get the text from the given node. This assumes the node contains a single child that is a text
   * node.
   * 
   * @param node the node from which to extract the text
   * @return the raw text, or the empty string if the element has no children
   * @throws IllegalArgumentException if the first child is not a text node
   */
  public static String getText(Node node) throws IllegalArgumentException {
    Node text = node.getFirstChild();
    if (text == null)
      return "";
    if (!(text instanceof Text))
      throw new IllegalArgumentException("Expected text, but found node '" +
                                         text.getNodeName() + "'");
    return ((Text) text).getData();
  }
}
