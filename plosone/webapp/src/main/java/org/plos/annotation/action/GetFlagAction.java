/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.action;

import org.plos.annotation.service.Flag;

public class GetFlagAction extends BaseGetAnnotationAction {
  public Flag getFlag() {
    return new Flag(getAnnotation());
  }

  public void setFlagId(final String flagId) {
    setAnnotationId(flagId);
  }
}
