/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.criterion;

import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * A parameter that can be used in place of a 'value' in Criterions.  Parameters are resolved
 * at the query execution time from a value map that is set up on {@link
 * org.topazproject.otm.Criteria Criteria}.
 *
 * @author Pradeep Krishnan
 *
 * @see org.topazproject.otm.Parameterizable
 */
@Entity(type = Criterion.RDF_TYPE + "/Parameter", model = Criterion.MODEL)
@UriPrefix(Criterion.NS)
public class Parameter {
  private String parameterName;

  /**
   * The id field used for persistence. Ignored otherwise.
   */
  @Id
  @GeneratedValue(uriPrefix = Criterion.RDF_TYPE + "/Parameter/Id/")
  public URI parameterId;

  /**
   * Creates a new Parameter object.
   */
  public Parameter() {
  }

  /**
   * Creates a new Parameter object.
   *
   * @param name The name of the parameter
   */
  public Parameter(String name) {
    this.parameterName = name;
  }

  /**
   * Get parameterName.
   *
   * @return parameterName as String.
   */
  public String getParameterName() {
    return parameterName;
  }

  /**
   * Set parameterName.
   *
   * @param parameterName the value to set.
   */
  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  public String toString() {
    return "Parameter[" + parameterName + "]";
  }
}
