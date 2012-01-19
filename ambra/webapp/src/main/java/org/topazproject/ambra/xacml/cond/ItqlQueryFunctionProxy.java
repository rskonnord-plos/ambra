/* $HeadURL::                                                                            $
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
package org.topazproject.ambra.xacml.cond;

import java.net.URI;

import org.w3c.dom.Node;

import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.cond.Function;
import com.sun.xacml.cond.FunctionProxy;

/**
 * This function proxy allows the creation of ItqlQueryFunctions whose return-value type is
 * specified in the xacml policy. The return-type is specified using the xml node attribute
 * <code>ReturnType</code> and will appear on the <code>Apply</code> node in policy.
 *
 * @author Pradeep Krishnan
 */
public class ItqlQueryFunctionProxy implements FunctionProxy {
  /**
   * Default Constructor
   */
  public ItqlQueryFunctionProxy() {
  }

  /**
   * Create a new instance of the itql query function using the data found in the DOM node
   * provided. This is called when the factory is asked to create one of these functions.
   *
   * @param root DOM node of the apply tag containing this function
   * @param xpathVersion ignored since itql query function does not use xpath
   *
   * @return Returns a <code>ItqlQueryFunction</code> instance
   *
   * @throws Exception if the DOM data was incorrect
   * @throws UnknownIdentifierException if the <code>ReturnType</code> is not supported
   */
  public Function getInstance(Node root, String xpathVersion)
                       throws Exception {
    String type    = root.getAttributes().getNamedItem("ReturnType").getNodeValue();
    URI    typeUri = new URI(type);

    if (!AttributeFactory.getInstance().getSupportedDatatypes().contains(type))
      throw new UnknownIdentifierException("Attributes of type " + type + " aren't supported.");

    return new ItqlQueryFunction(typeUri);
  }
}
