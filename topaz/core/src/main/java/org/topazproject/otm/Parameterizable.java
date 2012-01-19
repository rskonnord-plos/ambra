/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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

package org.topazproject.otm;

import java.net.URI;
import java.util.Set;

/** 
 * This represents a parameterizable object.
 * 
 * @author Ronald Tschalär
 */
public interface Parameterizable<T extends Parameterizable<T>> {
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

  /**
   * A Wrapper to store params set using the setUri() call.
   */
  public static class UriParam {
     private URI uri;

     public UriParam(URI uri) {
       if (uri == null)
         throw new NullPointerException("URI param value can't be null");
       this.uri = uri;
     }

     public URI getUri() {
       return uri;
     }

     public String toString() {
       return uri.toString();
     }
  }
}
