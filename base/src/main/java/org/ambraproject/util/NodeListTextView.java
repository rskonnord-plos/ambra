/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2012 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

package org.ambraproject.util;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.AbstractList;

/**
 * An immutable view of a node list as a list of strings.
 */
public class NodeListTextView extends AbstractList<String> {

  private final NodeList nodes;

  /**
   * Wrap a node list to represent it as a list of strings.
   * <p/>
   * This takes constant time and memory, and retains a pointer to the {@code NodeList} object. If that object must
   * become eligible for garbage-collection, use {@code new ArrayList<String>(new NodeListTextView(nodes)}.
   *
   * @param nodes the nodes to wrap
   * @throws NullPointerException if {@code nodes == null}
   */
  public NodeListTextView(NodeList nodes) {
    if (nodes == null) {
      throw new NullPointerException();
    }
    this.nodes = nodes;
  }

  /**
   * Extract text from a node.
   * <p/>
   * This method defines how this instance converts nodes into text. This class just calls {@link
   * Node#getTextContent()}, but subclasses may get such values as the text content of a child or an attribute.
   *
   * @param node a node
   * @return the nodes' text
   */
  protected String extractText(Node node) {
    return node.getTextContent();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String get(int index) {
    Node node = nodes.item(index);
    if (node == null) {
      throw new IndexOutOfBoundsException();
    }
    return extractText(node);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size() {
    return nodes.getLength();
  }

}
