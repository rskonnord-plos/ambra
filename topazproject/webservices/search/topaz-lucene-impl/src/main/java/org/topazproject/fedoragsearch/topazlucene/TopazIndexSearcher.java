/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 *
 * Modified from code part of Fedora. It's license reads:
 * License and Copyright: The contents of this file will be subject to the
 * same open source license as the Fedora Repository System at www.fedora.info
 * It is expected to be released with Fedora version 2.2.
 * Copyright 2006 by The Technical University of Denmark.
 * All rights reserved.
 */
package org.topazproject.fedoragsearch.topazlucene;

import java.io.IOException;

import org.apache.lucene.search.IndexSearcher;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

class TopazIndexSearcher extends IndexSearcher {
  private static final Log log = LogFactory.getLog(TopazIndexSearcher.class);

  public TopazIndexSearcher(String indexPath) throws IOException {
    super(indexPath);
    log.info("Created: " + this);
  }
  
  /**
   * Ensure that the underlying index is closed.
   */
  protected void finalize() throws Throwable {
    try {
      close();
      log.info("Closed: " + this);
    } finally {
      super.finalize();
    }
  }
}
