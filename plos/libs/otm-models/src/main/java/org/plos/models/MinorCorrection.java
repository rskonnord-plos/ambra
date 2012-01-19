/* $HeadURL::$
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.models;

import org.topazproject.otm.annotations.Entity;

@Entity(type = MinorCorrection.RDF_TYPE)
public class MinorCorrection extends Correction  implements ArticleAnnotation {
  private static final long serialVersionUID = -5374711498322357045L;
  
  public static final String RDF_TYPE = Annotea.TOPAZ_TYPE_NS + "MinorCorrection";
  public String getType() {
    return RDF_TYPE;
  }

  /**
   * Human friendly string for display and debugging.
   *
   * @return String for human consumption.
   */
  public String toString() {
    return "MinorCorrection: {"
            + "type: " + getType()
            + ", " + super.toString() + "}";
  }
}
