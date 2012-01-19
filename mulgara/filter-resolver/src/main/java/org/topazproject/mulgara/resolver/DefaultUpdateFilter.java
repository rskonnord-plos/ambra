/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.mulgara.resolver;

import java.net.URI;

import org.apache.log4j.Logger;
import fedora.common.Constants;

/** 
 * This is the default filter to be used by updates if none was specified.
 * 
 * @author Ronald Tschalär
 */
public class DefaultUpdateFilter implements UpdateFilter {
  private static final Logger logger = Logger.getLogger(DefaultUpdateFilter.class);

  /** 
   * If the URI is a fedora URI ({@link fedora.common.Constants.FEDORA#uri FEDORA.uri}) then
   * return the URI minus the fedora prefix; else return null.
   */
  public String getFedoraPID(URI subject, String[] models, String datastream) {
    String subj = subject.toString();
    if (subj.startsWith(Constants.FEDORA.uri))
      return subj.substring(Constants.FEDORA.uri.length());

    // don't throw an exception, because that will trigger a retry, and that is pointless...
    logger.error("Invalid subject-uri '" + subj + "' - must start with '" + Constants.FEDORA.uri +
                   "'");
    return null;
  }

  /** 
   * Return <var>subject</var>.
   */
  public URI processSubject(URI subject, String[] models, String datastream) {
    return subject;
  }
}
