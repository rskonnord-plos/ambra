/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.event;

import org.topazproject.otm.Session;

/**
 * Called before inserting into the datastore
 * 
 * @author Pradeep Krishnan
 */
public interface PreInsertEventListener {
  /**
   * Do any pre-insert processing.
   *
   * @param session the session that is reporting this event
   * @param object the object for which the event is being generated
   */
  public void onPreInsert(Session session, Object object);
}
