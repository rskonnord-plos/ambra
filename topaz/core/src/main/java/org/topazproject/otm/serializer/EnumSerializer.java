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
 * A serializer for enum that uses name() to serialize and valueOf() to de-serialize.
 *
 * @author Pradeep Krishnan
 */
public class EnumSerializer implements Serializer<Enum> {
  /*
   * inherited javadoc
   */
  public String serialize(Enum o) throws Exception {
    return (o == null) ? null : o.name();
  }

  /*
   * inherited javadoc
   */
  public Enum deserialize(String o, Class c) throws Exception {
    return Enum.valueOf(c, o);
  }
}
