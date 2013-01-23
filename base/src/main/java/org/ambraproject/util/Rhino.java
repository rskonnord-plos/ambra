/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.util;

import org.w3c.dom.Node;

/**
 * Utility methods currently called by both admin and rhino.  When admin
 * is retired, we will likely move this code back into rhino, or refactor
 * it in some other way.
 */
public final class Rhino {

  /**
   * Helper method to get all the text of child nodes of a given node
   *
   * @param node - the node to use as base
   * @return - all nested text in the node
   */
  public static String getAllText(Node node) {

    String text = "";
    for (int i = 0; i < node.getChildNodes().getLength(); i++) {
      Node childNode = node.getChildNodes().item(i);
      if (Node.TEXT_NODE == childNode.getNodeType()) {
        text += childNode.getNodeValue();
      } else if (Node.ELEMENT_NODE == childNode.getNodeType()) {
        text += "<" + childNode.getNodeName() + ">";
        text += getAllText(childNode);
        text += "</" + childNode.getNodeName() + ">";
      }
    }
    return text.replaceAll("[\n\t]", "").trim();
  }
}
