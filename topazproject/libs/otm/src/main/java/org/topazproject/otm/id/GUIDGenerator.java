/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.id;

import java.util.UUID; // Requires Java 1.5

/**
 * Generate unique ids based on rfc 4122.
 *
 * Note that there are four sub-types of UUIDs defined by rfc 4122. Because we dont have
 * access to the mac-address via java, we use randomly generated UUIDs. It may be desireable
 * to use the mac-address form though. (There is some suggesting that Java 1.6 may have
 * access to this?)
 *
 * @see java.util.UUID
 * @author Eric Brown
 */
public class GUIDGenerator implements IdentifierGenerator {
  private String uriPrefix;

  public String generate() {
    return uriPrefix + UUID.randomUUID().toString();
  }

  public void setUriPrefix(String uriPrefix) {
    this.uriPrefix = uriPrefix;
  }
}
