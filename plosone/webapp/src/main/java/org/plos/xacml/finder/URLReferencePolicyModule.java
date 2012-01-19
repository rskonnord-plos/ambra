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
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.List;

/**
 * Policy Finder module that loads policy files from a URL. This version supports look up of
 * policied by id reference.
 *
 * @author Pradeep Krishnan
 */
public class URLReferencePolicyModule extends AbstractPolicyModule {
  /*
   * @see org.topazproject.xacml.finder.AbstractPolicyFinderModule
   */
  public URLReferencePolicyModule() {
    super();
  }

  /*
   * @see org.topazproject.xacml.finder.AbstractPolicyFinderModule
   */
  public URLReferencePolicyModule(File schemaFile) {
    super(schemaFile);
  }

  /*
   * @see org.topazproject.xacml.finder.AbstractPolicyFinderModule
   */
  public URLReferencePolicyModule(List resources) {
    super(resources);
  }

  /**
   * Returns true if the module supports finding policies based on an id reference (in a
   * PolicySet).
   *
   * @return true since idReference retrieval is supported
   */
  public boolean isIdReferenceSupported() {
    return true;
  }

  /*
   * @see org.topazproject.xacml.finder.AbstratcPolicyFinderModule#getPolicyResourceAsStream
   */
  public InputStream getPolicyResourceAsStream(String resource)
                                        throws IOException {
    return (new URL(resource)).openStream();
  }
}
