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

import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

/**
 * Operates on the result set from an operation on the Lucene index.
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class ResultSet {
  private StringBuffer resultXml;

  public ResultSet() {
  }

  protected ResultSet(StringBuffer in) throws GenericSearchException {
    resultXml = in;
  }

  protected void close() throws GenericSearchException {
  }

  protected StringBuffer getResultXml() {
    return this.resultXml;
  }
}
