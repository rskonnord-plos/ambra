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

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;

/**
 * Mapper for a property that embeds another entity.
 *
 * @author Pradeep Krishnan
 */
public interface EmbeddedMapper extends Mapper {
  /**
   * Gets the embedded entity.
   *
   * @return the embedded entity
   */
  public ClassMetadata getEmbeddedClass();

  /**
   * Promote an embedded field mapper up to the same level as this so that it can be added to
   * collections that contain mappers at the same level as this.
   *
   * @param mapper the property that needs to be promoted
   *
   * @return the promoted mapper
   *
   * @throws OtmException on an error
   */
  public Mapper promote(Mapper mapper) throws OtmException;
}
