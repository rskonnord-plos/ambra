/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.xml.transform;

import java.io.IOException;

/** 
 * Retrieve the content of a URL.
 *
 * @author Ronald Tschalär
 * @version $Id$
 */
public interface URLRetriever {
  /** 
   * Retrieve the contents of a URL as a byte[]. 
   * 
   * @param url the url of the resource to retrieve
   * @param id  the id of the resource to retrieve
   * @return the contents, or null if not found
   * @throws IOException if an error occurred retrieving the contents (other than not-found)
   */
  public byte[] retrieve(String url, String id) throws IOException;
}
