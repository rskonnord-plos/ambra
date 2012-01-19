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

import java.net.URL;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Retrieve the URL over the network.
 *
 * @author Ronald Tschalär
 * @version $Id$
 */
public class NetworkURLRetriever implements URLRetriever {
  private static final Log log = LogFactory.getLog(NetworkURLRetriever.class);

  /**
   * Retrieve the specified url from the network and return it.
   *
   * @param url the address of the content to retrieve.
   * @return a byte array containing the retrieved content.
   * @throws IOException if there was a problem fetching the content.
   */
  public byte[] retrieve(String url) throws IOException {
    if (log.isDebugEnabled())
      log.debug("Network retriever ('" + url + "')");

    return IOUtils.toByteArray(new URL(url).openStream());
  }
}
