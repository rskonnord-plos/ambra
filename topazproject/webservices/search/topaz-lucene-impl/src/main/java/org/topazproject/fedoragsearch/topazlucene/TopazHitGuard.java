/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.fedoragsearch.topazlucene;

import java.security.Guard;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;

import org.apache.lucene.search.Hit;
import org.apache.lucene.document.Document;

import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.xacml.AbstractSimplePEP;
import org.topazproject.ws.article.Article;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Allow access to items the user has access to, but not ones that the PEP would disallow
 * for the user.
 *
 * @author Eric Brown
 * @version $Id$
 */
public class TopazHitGuard implements Guard {
  private static final Log log = LogFactory.getLog(TopazHitGuard.class);
  public void checkGuard(Object object) throws SecurityException {
    Hit hit = (Hit) object;
    Document doc;
    try {
      doc = hit.getDocument();
    } catch (IOException ioe) {
      throw (SecurityException)
          new SecurityException("Unable to access document: " + hit).initCause(ioe);
    }

    ItqlHelper itql = SearchContext.getItqlHelper();
    /* TODO: Check state & tombstone & ... throw SecurityException if it shouldn't be available
     * (This may not be necessary as these things shouldn't be in the index anyway and/or
     *  it should be xacml -- see below -- that should block them anyway.)
     *
     * That said, in the future, we may have a class of articles that the user wants to know
     * exists but be flagged as protected. We'd have to throw a different type of exception
     * and handle that in the XML we return when searching.
     */

    String pid = doc.get("PID");
    String uri = "info:fedora/" + pid;
    AbstractSimplePEP pep = SearchContext.getPEP();
    try {
      pep.checkAccess(Article.Permissions.READ_META_DATA, new URI(uri));

      if (log.isDebugEnabled())
        log.debug("Returning unguarded uri '" + uri + "'");
    } catch (URISyntaxException us) {
      throw (SecurityException)
        new SecurityException("Unable to create URI '" + uri + "'").initCause(us);
    }
  }
}
