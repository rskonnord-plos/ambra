/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.xacml.finder;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import java.net.URL;

import java.util.List;


/**
 * Policy Finder module that loads policy files from a URL. This version
 * supports Target matching; (ie. searches thru all policy files to find a matching policy)
 *
 * @author Pradeep Krishnan
 */
public class URLPolicyModule extends AbstractPolicyModule {
  /*
   * @see org.topazproject.xacml.finder.AbstractPolicyFinderModule
   */
  public URLPolicyModule() {
    super();
  }

  /*
   * @see org.topazproject.xacml.finder.AbstractPolicyFinderModule
   */
  public URLPolicyModule(File schemaFile) {
    super(schemaFile);
  }

  /*
   * @see org.topazproject.xacml.finder.AbstractPolicyFinderModule
   */
  public URLPolicyModule(List resources) {
    super(resources);
  }

  /**
   * Indicates whether this module supports finding policies based on a request (target matching).
   * Since this module does support finding policies based on requests, it returns true.
   *
   * @return true if finding policies based on requests is supported
   */
  public boolean isRequestSupported() {
    return true;
  }

  /*
   * @see org.topazproject.xacml.finder.AbstratcPolicyFinderModule#getPolicyResourceAsStream
   */
  public InputStream getPolicyResourceAsStream(String resource) throws IOException {
    return (new URL(resource)).openStream();
  }
}
