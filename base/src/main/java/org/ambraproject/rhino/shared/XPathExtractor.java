/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2014 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.rhino.shared;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathException;

/**
 * Interface for a thin wrapper around {@link javax.xml.xpath.XPath}.
 * <p/>
 * This interface is only here in order to share some code between ambra and rhino
 * (specifically {@link org.ambraproject.rhino.shared.AuthorsXmlExtractor}).  The
 * two projects set up their xpath instances in slightly different ways, hence the
 * need for this interface.  You shouldn't ever need this interface for anything else.
 */
public interface XPathExtractor {

  /**
   * Retrieves a single node from the given node using an xpath expression.
   *
   * @param node node to search
   * @param xpath xpath expression
   * @return single Node found, or null if it wasn't
   * @throws XPathException
   */
  Node selectNode(Node node, String xpath) throws XPathException;

  /**
   * Retrieves nodes from the given node using an xpath expression.
   *
   * @param node node to search
   * @param xpath xpath expression
   * @return list of Nodes found
   * @throws XPathException
   */
  NodeList selectNodes(Node node, String xpath) throws XPathException;

  /**
   * Retrieves a single node as a string from the given node using an xpath expression.
   *
   * @param node node to search
   * @param xpath xpath expression
   * @return string representation of the Node found, or null if it wasn't
   * @throws XPathException
   */
  String selectString(Node node, String xpath) throws XPathException;
}
