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
   * @param binders the binders
   */
  public BlobMapperImpl(BlobDefinition def, Map<EntityMode, Binder> binders) {
    super(binders);
    this.def = def;
  }

  /*
   * inherited javadoc
   */
  public BlobDefinition getDefinition() {
    return def;
  }
}
