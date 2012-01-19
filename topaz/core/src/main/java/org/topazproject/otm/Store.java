/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm;

/**
 * An abstraction to represent triple or blob stores.
 *
 * @author Pradeep Krishnan
 */
public interface Store {
  /**
   * Opens a connection to the store.
   *
   * @param session  the current session
   * @param readOnly true if the connection should be read-only
   *
   * @return the connection
   *
   * @throws OtmException on an error
   */
  public Connection openConnection(Session session, boolean readOnly) throws OtmException;
}
