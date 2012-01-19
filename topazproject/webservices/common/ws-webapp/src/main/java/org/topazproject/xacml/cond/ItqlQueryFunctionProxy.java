/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.xacml.cond;

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
