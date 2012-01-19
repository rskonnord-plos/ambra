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
package org.topazproject.otm.criterion;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.FetchType;
import org.topazproject.otm.annotations.Predicate;

/**
 * A base class for all binary operations involving a field and its value. The value could be a
 * {@link org.topazproject.otm.criterion.Parameter Parameter} in which case the value is resolved
 * during the query generation time. Note that to support persisted Criterions, a serialized value
 * must be set. This is because without knowing the Serializer used by the field, there is no way
 * for us to Serialize/Deserialize a value. A serializer is only available during query
 * generation. So during the query generation,  if a field value is supplied that will be
 * serialized and used instead of the already serialized value.
 *
 * @author Pradeep Krishnan
 *
 * @see #getValue
 */
public abstract class AbstractBinaryCriterion extends AbstractUnaryCriterion {
  private static final Log log             = LogFactory.getLog(AbstractBinaryCriterion.class);
  private String           serializedValue;
  private transient Object value;
  private Parameter        parameter;

  /**
   * Creates a new AbstractBinaryCriterion object.
   */
  public AbstractBinaryCriterion() {
  }

  /**
   * Creates a new AbstractBinaryCriterion object.
   *
   * @param name field/predicate name
   * @param value field/predicate value or a {@link org.topazproject.otm.criterion.Parameter}
   */
  public AbstractBinaryCriterion(String name, Object value) {
    super(name);
    setValue(value);
  }

  /**
   * Get serializedValue.
   *
   * @return serializedValue as String.
   */
  public String getSerializedValue() {
    return serializedValue;
  }

  /**
   * Set serializedValue.
   *
   * @param serializedValue the value to set.
   */
  @Predicate
  public void setSerializedValue(String serializedValue) {
    this.serializedValue = serializedValue;
  }

  /**
   * Gets the value to use in operations. Falls back to serializedValue if not set.
   *
   * @return value as Object.
   */
  public Object getValue() {
    return (value != null) ? value : serializedValue;
  }

  /**
   * Set value. The value could be a {@link org.topazproject.otm.criterion.Parameter
   * Parameter}  in which case one of the set methods from {@link
   * org.topazproject.otm.Parameterizable Parameterizable} must be called on the {@link
   * org.topazproject.otm.Criteria Criteria} to which  this Criterion belongs to.
   *
   * @param value the value to set.
   */
  public void setValue(Object value) {
    this.value = value;

    if (value instanceof Parameter)
      this.parameter = (Parameter) value;
    else
      this.parameter = null;
  }

  /*
   * inherited javadoc
   */
  public Set<String> getParamNames() {
    if (parameter == null)
      return Collections.emptySet();

    return Collections.singleton(parameter.getParameterName());
  }

  /**
   * Get parameter.
   *
   * @return parameter as Parameter.
   */
  public Parameter getParameter() {
    return parameter;
  }

  /**
   * Set parameter.
   *
   * @param parameter the value to set.
   */
  @Predicate(fetch=FetchType.eager)
  public void setParameter(Parameter parameter) {
    this.parameter   = parameter;
    this.value       = parameter;
  }

  public String toString() {
    return getClass().getName().replace("org.topazproject.otm.criterion.", "").
           replace("Criterion", "") + "[" + getFieldName() + ", " +  getValue() + "]";
  }

}
