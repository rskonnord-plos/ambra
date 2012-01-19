/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.util.StrutsTypeConverter;

/** 
 * Split strings into a collection. 
 * 
 * @author Ronald Tschalär
 */
public class StringListTypeConverter extends StrutsTypeConverter {
  public Object convertFromString(Map context, String[] values, Class toClass) {
    Collection res = new ArrayList();

    for (String value : values) {
      for (String item : value.split("\\s*,\\s*"))
        res.add(item);
    }

    return res;
  }

  public String convertToString(Map context, Object o) {
    return StringUtils.join((Collection) o, ",");
  }
}
