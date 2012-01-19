/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.service;

import org.topazproject.authentication.CASProtectedService;

/**
 * Invalid proxy ticket exception.
 */
public class InvalidProxyTicketException extends RuntimeException {
  public InvalidProxyTicketException(final CASProtectedService.NoProxyTicketException exception) {
    super(exception);
  }
}
