/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.serializer;

/**
 * A serializer for xsd:boolean type.
 *
 * @author Pradeep Krishnan
 */
public class XsdBooleanSerializer implements Serializer<Boolean> {
  /*
   * inherited javadoc
   */
  public String serialize(Boolean o) throws Exception {
    return o.toString();
  }

  /*
   * inherited javadoc
   */
  public Boolean deserialize(String o, Class c) throws Exception {
    if ("1".equals(o) || "true".equals(o))
      return Boolean.TRUE;

    if ("0".equals(o) || "false".equals(o))
      return Boolean.FALSE;

    throw new IllegalArgumentException("invalid xsd:boolean '" + o + "'");
  }
}
