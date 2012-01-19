/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.mapping;

import org.topazproject.otm.id.IdentifierGenerator;

/**
 * A mapper for Id fields.
 *
 * @author Pradeep krishnan
 */
public interface IdMapper extends Mapper {
  /**
   * Get the generator for this field
   *
   * @return the generator to use for this field (or null if there isn't one)
   */
  public IdentifierGenerator getGenerator();
}
