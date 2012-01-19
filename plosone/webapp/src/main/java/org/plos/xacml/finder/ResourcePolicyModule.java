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

import java.util.List;


/**
 * Policy Finder module that loads policy files from a thread-context classloader. This version
 * supports Target matching; (ie. searches thru all policy files to find a matching policy)
 *
 * @author Pradeep Krishnan
 */
public class ResourcePolicyModule extends AbstractPolicyModule {
  /*
   * @see org.topazproject.xacml.finder.AbstractPolicyFinderModule
   */
  public ResourcePolicyModule() {
    super();
  }

  /*
   * @see org.topazproject.xacml.finder.AbstractPolicyFinderModule
   */
  public ResourcePolicyModule(File schemaFile) {
    super(schemaFile);
  }

  /*
   * @see org.topazproject.xacml.finder.AbstractPolicyFinderModule
   */
  public ResourcePolicyModule(List resources) {
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
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    InputStream in = cl.getResourceAsStream(resource);

    if (in == null)
      throw new IOException(resource + " not found in classpath");

    return in;
  }
}
