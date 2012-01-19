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
import java.util.HashMap;
import java.util.Map;

import org.topazproject.otm.query.Results;

/** 
 * This implements the basic functionality of a parameterizable entity. The parameters are stored
 * in the {@link #paramValues paramValues} map, where the values are either a {@link URI URI}, a
 * {@link Results.Literal Literal}, or the specific object.
 * 
 * @author Ronald Tschalär
 */
public abstract class AbstractParameterizable<T> implements Parameterizable<T> {
  /** The parameter values that have been set. */
  protected final Map<String, Object> paramValues = new HashMap<String, Object>();

  private void checkParameterName(String name) throws OtmException {
    if (!getParameterNames().contains(name))
      throw new OtmException("'" + name + "' is not a valid parameter name - must be one of '" +
                             getParameterNames() + "'");
  }

  public T setParameter(String name, Object val) throws OtmException {
    checkParameterName(name);
    paramValues.put(name, val);
    return (T) this;
  }

  public T setUri(String name, URI val) throws OtmException {
    checkParameterName(name);
    paramValues.put(name, val);
    return (T) this;
  }

  public T setPlainLiteral(String name, String val, String lang) throws OtmException {
    checkParameterName(name);
    paramValues.put(name, new Results.Literal(val, lang, null));
    return (T) this;
  }

  public T setTypedLiteral(String name, String val, URI dataType) throws OtmException {
    checkParameterName(name);
    paramValues.put(name, new Results.Literal(val, null, dataType));
    return (T) this;
  }
}
