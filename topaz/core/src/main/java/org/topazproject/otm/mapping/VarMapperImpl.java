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
