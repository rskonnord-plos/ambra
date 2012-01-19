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

import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.xacml.AbstractSimplePEP;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to stash thread-local variables between org.topazproject.ws:ws-search-server's
 * {@link org.topazproject.ws.search.FedoraGenericSearchServicePortSoapBindingImpl}
 * and classes in this package.
 *
 * @author Eric Brown
 * @version $Id$
 */
public class SearchContext {
  private static final Log log = LogFactory.getLog(SearchContext.class);
  
  private static class ContextData {
    ItqlHelper itql;
    AbstractSimplePEP pep;
    String userName;
  }
  
  private static ThreadLocal threadData = new ThreadLocal();

  public static void setContext(ItqlHelper itql, AbstractSimplePEP pep, String userName) {
    if (log.isDebugEnabled())
      log.debug("Set thread context for: " + userName + " " + itql + " " + pep);
    
    ContextData data = new ContextData();
    data.itql     = itql;
    data.pep      = pep;
    data.userName = userName;
    threadData.set(data);
  }

  static AbstractSimplePEP getPEP()        { return ((ContextData) threadData.get()).pep; }
  static ItqlHelper        getItqlHelper() { return ((ContextData) threadData.get()).itql; }
  static String            getUserName()   { return ((ContextData) threadData.get()).userName; }
}
