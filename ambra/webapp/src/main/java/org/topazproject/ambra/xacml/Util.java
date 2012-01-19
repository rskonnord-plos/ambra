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
package org.topazproject.ambra.xacml;

import java.net.URI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.Status;

/**
 * XACML related Utility functions.
 *
 * @author Pradeep Krishnan
 */
public class Util {
  /**
   * Resource URI that must be present in all xacml requests.
   */
  public static final URI RESOURCE_ID = URI.create(EvaluationCtx.RESOURCE_ID);

  /**
   * The Action URI that must be present in all xacml requests.
   */
  public static final URI ACTION_ID = URI.create("urn:oasis:names:tc:xacml:1.0:action:action-id");

  // Standard subject-id from spec
  public static final URI SUBJECT_ID =
    URI.create("urn:oasis:names:tc:xacml:1.0:subject:subject-id");

  /**
   * Creates a set of action attributes for use in a XACML request.
   *
   * @param action The value of {@link #ACTION_ID} attribute
   *
   * @return the singleton set containing the action-id attribute
   */
  public static Set createActionAttrs(String action) {
    AttributeValue value = new StringAttribute(action);
    Attribute      attr = new Attribute(ACTION_ID, null, null, value);

    return Collections.singleton(attr);
  }

  /**
   * Creates an EvaluationResult for an attribute finder processing error.
   *
   * @param message the error message
   * @param type the type-URI of the attribute
   * @param id the id of the attribute
   *
   * @return Returns the EvaluationResult with the processing error status code
   */
  public static EvaluationResult processingError(String message, URI type, URI id) {
    Status status = new Status(Collections.singletonList(Status.STATUS_PROCESSING_ERROR),
                               message + "\nattribute-id: " + id + ", attribute-type: " + type);

    return new EvaluationResult(status);
  }

  /**
   * Converts an Object to an AttributeValue. This can be improved to make use of a java type to
   * XACML type conversion mapping. Currently it depends on the <code>toString</code> method
   * matching the <code>getInstance</code> method of AttributeProxy. Should work mostly for
   * string, integer and boolean types.
   *
   * @param type the xacml attribute type identifier
   * @param o the object to be converted
   *
   * @return Returns the AttributeValue or null
   *
   * @throws UnknownIdentifierException propagated from AtttributeFactory
   * @throws ParsingException propagated from AttributeFactory
   */
  public static AttributeValue toAttributeValue(URI type, Object o)
                                         throws UnknownIdentifierException, ParsingException {
    if (o == null)
      return null;

    if (o instanceof AttributeValue)
      return (AttributeValue) o;

    if (o instanceof Collection)
      return toAttributeValue(type, (Collection) o);

    if (o.getClass().isArray())
      return toAttributeValue(type, (Object[]) o);

    return AttributeFactory.getInstance().createValue(type, o.toString());
  }

  /**
   * Converts a Collection of objects to a BagAttributeValue.
   *
   * @param type the xacml attribute type identifier
   * @param o the collection to be converted
   *
   * @return Returns a bag of AttributeValues
   *
   * @throws UnknownIdentifierException propagated from AtttributeFactory
   * @throws ParsingException propagated from AttributeFactory
   */
  public static BagAttribute toAttributeValue(URI type, Collection o)
                                       throws UnknownIdentifierException, ParsingException {
    Collection c  = new ArrayList(o.size());
    Iterator   it = o.iterator();

    while (it.hasNext()) {
      c.add(toAttributeValue(type, it.next()));
    }

    return new BagAttribute(type, c);
  }

  /**
   * Converts an array of objects to a BagAttributeValue.
   *
   * @param type the xacml attribute type identifier
   * @param o the collection to be converted
   *
   * @return Returns a bag of AttributeValues
   *
   * @throws UnknownIdentifierException propagated from AtttributeFactory
   * @throws ParsingException propagated from AttributeFactory
   */
  public static BagAttribute toAttributeValue(URI type, Object[] o)
                                       throws UnknownIdentifierException, ParsingException {
    Collection c = new ArrayList(o.length);

    for (int i = 0; i < o.length; i++)
      c.add(toAttributeValue(type, o[i]));

    return new BagAttribute(type, c);
  }

}
