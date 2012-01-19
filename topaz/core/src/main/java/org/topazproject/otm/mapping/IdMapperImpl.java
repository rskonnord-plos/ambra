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
import org.topazproject.otm.id.IdentifierGenerator;
import org.topazproject.otm.metadata.IdDefinition;

/**
 * An implementation of Mapper for Id fields.
 *
 * @author Pradeep krishnan
 */
public class IdMapperImpl extends AbstractMapper implements IdMapper {
  private final IdDefinition def;

  /**
   * Creates a new IdMapperImpl object.
   *
   * @param def     the property definition
   * @param binders the binders
   */
  public IdMapperImpl(IdDefinition def, Map<EntityMode, Binder> binders) {
    super(binders);
    this.def = def;
  }

  /*
   * inherited javadoc
   */
  public IdentifierGenerator getGenerator() {
    return getDefinition().getGenerator();
  }

  /*
   * inherited javadoc
   */
  public IdDefinition getDefinition() {
    return def;
  }
}
