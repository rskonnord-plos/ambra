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

import java.util.Map;

import org.topazproject.otm.EntityMode;
import org.topazproject.otm.metadata.BlobDefinition;

/**
 * An implementation of Mapper for Blob fields.
 *
 * @author Pradeep krishnan
 */
public class BlobMapperImpl extends AbstractMapper implements BlobMapper {
  private final BlobDefinition def;

  /**
   * Creates a new BlobMapperImpl object.
   *
   * @param def     the property definition
   * @param propertyBinders the binders
   */
  public BlobMapperImpl(BlobDefinition def, Map<EntityMode, PropertyBinder> propertyBinders) {
    super(propertyBinders);
    this.def = def;
  }

  /*
   * inherited javadoc
   */
  public BlobDefinition getDefinition() {
    return def;
  }
}
