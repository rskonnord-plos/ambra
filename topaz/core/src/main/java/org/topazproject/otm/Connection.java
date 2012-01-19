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
 * A connection handle to a triple store or blob store. Connections last for a single transaction
 * only; it is up the implementation to do pooling, if desired.
 *
 * @author Pradeep Krishnan
  */
public interface Connection {
  /** 
   * Signals that we're done with the connection.
   */
  void close();
}
