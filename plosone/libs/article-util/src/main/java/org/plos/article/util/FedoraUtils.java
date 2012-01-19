/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.util;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Some utilities for dealing with Fedora.
 *
 * @author Ronald Tschalär
 */
class FedoraUtil {
  private static final Log log = LogFactory.getLog(FedoraUtil.class);

  /**
   * Not meant to be instantiated.
   */
  private FedoraUtil() {
  }

  /**
   * See if the given exception indicates that the fedora object does not exist.
   *
   * @param re the RemoteException to analyze
   */
  public static boolean isNoSuchObjectException(RemoteException re) {
    return re.getMessage().startsWith("fedora.server.errors.ObjectNotInLowlevelStorageException");
  }

  /**
   * See if the given exception indicates that the fedora object already exists, and if so generate
   * a DuplicateArticleIdException; otherwise just rethrow the original exception. This method never
   * returns normally.
   *
   * @param re  the RemoteException to analyze
   * @param id  the id to put in the DuplicateArticleIdException
   * @throws DuplicateArticleIdException if <var>re</var> indicates that the fedora object already
   *         exists
   * @throws RemoteException for all other exceptions; this is just <var>re</var>
   */
  public static void detectDuplicateArticleIdException(RemoteException re, String id)
      throws DuplicateArticleIdException, RemoteException {
    if (re.getMessage().startsWith("fedora.server.errors.ObjectExistsException")) {
      if (log.isDebugEnabled())
        log.debug("tried to create duplicate object", re);
      throw new DuplicateArticleIdException(id);
    }

    throw re;
  }
}
