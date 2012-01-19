/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm;

import java.net.URI;
import java.util.Set;

/** 
 * This represents a parameterizable object.
 * 
 * @author Ronald Tschalär
 */
public interface Parameterizable<T> {
  /** 
   * Return the list of parameter names. 
   * 
   * @return the set of names; will be empty if there are no parameters
   */
  Set<String> getParameterNames() throws OtmException;

  /** 
   * Set the given parameter's value. The value will be converted using the appropriate serializer
   * for parameter's type; if the parameter's type is not known it will be serialized by calling
   * <code>toString()</code> on the value.
   * 
   * @param name the name of the parameter
   * @param val  the parameter's value
   * @return this (useful for method chaining)
   * @throws OtmException if <var>name</var> is not a valid parameter name
   */
  T setParameter(String name, Object val) throws OtmException;

  /** 
   * Set the given parameter's value as a URI.
   * 
   * @param name the name of the parameter
   * @param val  the parameter's value
   * @return this (useful for method chaining)
   * @throws OtmException if <var>name</var> is not a valid parameter name
   */
  T setUri(String name, URI val) throws OtmException;

  /** 
   * Set the given parameter's value as a plain literal.
   * 
   * @param name the name of the parameter
   * @param val  the parameter's literal value
   * @param lang if not null, the language tag to add
   * @return this (useful for method chaining)
   * @throws OtmException if <var>name</var> is not a valid parameter name
   */
  T setPlainLiteral(String name, String val, String lang) throws OtmException;

  /** 
   * Set the given parameter's value as a datatyped literal.
   * 
   * @param name     the name of the parameter
   * @param val      the parameter's literal value
   * @param dataType the literal's datatype
   * @return this (useful for method chaining)
   * @throws OtmException if <var>name</var> is not a valid parameter name
   */
  T setTypedLiteral(String name, String val, URI dataType) throws OtmException;
}
