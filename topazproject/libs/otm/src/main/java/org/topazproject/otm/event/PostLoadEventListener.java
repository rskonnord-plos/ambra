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
 * Called after loading from the datastore
 * 
 * @author Pradeep Krishnan
 */
public interface PostLoadEventListener {
  /**
   * Do any post-load processing. 
   *
   * @param session the session that is reporting this event
   * @param object the object for which the event is being generated
   */
  public void onPostLoad(Session session, Object object);
}
