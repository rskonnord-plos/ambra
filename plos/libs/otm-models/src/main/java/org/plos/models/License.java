/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/libs/otm-models/src/main/#$
 * $Id: PLoS.java 3138 2007-07-10 19:10:40Z amit $
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

import java.io.Serializable;
import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;

/**
 * Signature for licenses to be used
 *
 * @author Amit Kapoor
 */
@Entity(type = PLoS.creativeCommons + "License", model = "ri")
public abstract class License implements Serializable {
  @Id
  private URI   id;

  /**
   * Creates a new license object.
   */
  public License(URI id) {
    this.id = id;
  }

  /**
   * Get id.
   *
   * @return id as URI.
   */
  public URI getId() {
    return id;
  }

  /**
   * Set id.
   *
   * @param id the value to set.
   */
  public void setId(URI id) {
    this.id = id;
  }
}
