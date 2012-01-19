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
 * A triple store connection handle.
 *
 * @author Pradeep Krishnan
  */
public interface Connection {
  /**
   * Begin a transaction on this connection.
   */
  public void beginTransaction() throws OtmException;

  /**
   * Commit the current transaction and end it.
   */
  public void commit() throws OtmException;

  /**
   * Rollback the current transaction and end it.
   */
  public void rollback() throws OtmException;
}
