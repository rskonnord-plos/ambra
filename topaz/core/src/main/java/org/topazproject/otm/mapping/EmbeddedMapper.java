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
