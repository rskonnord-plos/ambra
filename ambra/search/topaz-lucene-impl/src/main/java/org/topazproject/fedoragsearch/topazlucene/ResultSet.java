/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
