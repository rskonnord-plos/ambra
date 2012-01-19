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
import org.topazproject.otm.FetchType;
import org.topazproject.otm.metadata.VarDefinition;

/**
 * An implementation of Mapper for view projection fields.
 *
 * @author Pradeep krishnan
 */
public class VarMapperImpl extends AbstractMapper implements VarMapper {
  private final VarDefinition def;

  /**
   * Creates a new VarMapperImpl object.
   *
   * @param def     the property definition
   * @param binders the list of binders
   */
  public VarMapperImpl(VarDefinition def, Map<EntityMode, Binder> binders) {
    super(binders);
    this.def = def;
  }

  /*
   * inherited javadoc
   */
  public String getProjectionVar() {
    return getDefinition().getProjectionVar();
  }

  /*
   * inherited javadoc
   */
  public FetchType getFetchType() {
    return getDefinition().getFetchType();
  }

  /*
   * inherited javadoc
   */
  public String getAssociatedEntity() {
    return getDefinition().getAssociatedEntity();
  }

  /*
   * inherited javadoc
   */
  public boolean isAssociation() {
    return getDefinition().getAssociatedEntity() != null;
  }

  /*
   * inherited javadoc
   */
  public VarDefinition getDefinition() {
    return def;
  }
}
