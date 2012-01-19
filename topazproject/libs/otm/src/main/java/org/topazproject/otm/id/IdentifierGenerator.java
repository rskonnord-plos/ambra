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

import org.topazproject.otm.OtmException;

/**
 * The general contract between a class that generates unique identifiers and the Session.
 * It is not intended that this interface ever be exposed to the application. It is intended
 * that users implement this interface to provide custom identifier generation strategies.
 *
 * @see org.hibernate.id.IdentifierGenerator
 * @author Eric Brown
 */
public interface IdentifierGenerator {
  /**
   * Generate a new identifier.
   *
   * @return a new identifier
   */
  String generate() throws OtmException;

  /**
   * Set the uri-prefix to use for the generated ids
   *
   * Example: http://rdf.topazproject.org/MyClass/ids#
   *
   * @param uriPrefix the uri prefix to use for id generation.
   */
  void setUriPrefix(String uriPrefix);
}
