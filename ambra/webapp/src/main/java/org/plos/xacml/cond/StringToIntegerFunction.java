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

package org.plos.xacml.cond;

import java.util.List;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.cond.FunctionBase;

/**
 * A XACML function that converts a string to an integer.
 *
 * @author Ronald Tschalär
 */
public class StringToIntegerFunction extends FunctionBase {
  /**
   * Standard identifier for the string-normalize-space function.
   */
  public static final String NAME_STRING_TO_INTEGER = FUNCTION_NS + "string-to-integer";

  /**
   * Creates a new <code>StringToIntegerFunction</code> object.
   */
  public StringToIntegerFunction() {
    super(NAME_STRING_TO_INTEGER, 2, StringAttribute.identifier, false, 1,
          IntegerAttribute.identifier, false);
  }

  /**
   * Evaluate the function, using the specified parameters.
   *
   * @param inputs  a <code>List</code> of <code>Evaluatable</code> objects representing the
   *                arguments passed to the function
   * @param context an <code>EvaluationCtx</code> so that the <code>Evaluatable</code> objects can
   *                be evaluated
   * @return an <code>EvaluationResult</code> representing the function's result
   */
  public EvaluationResult evaluate(List inputs, EvaluationCtx context) {
    // Evaluate the arguments
    AttributeValue[] argValues = new AttributeValue[inputs.size()];
    EvaluationResult result = evalArgs(inputs, context, argValues);

    if (result != null)
      return result;

    String s = ((StringAttribute) argValues[0]).getValue();
    return new EvaluationResult(IntegerAttribute.getInstance(s));
  }
}
