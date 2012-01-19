/* $HeadURL::                                                                            $
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
 */
package org.topazproject.ambra.xacml.finder;

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
   * inherited javadoc
   */
  public URLPolicyModule() {
    super();
  }

  /*
   * inherited javadoc
   */
  public URLPolicyModule(File schemaFile) {
    super(schemaFile);
  }

  /*
   * inherited javadoc
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
   * inherited javadoc
   */
  public InputStream getPolicyResourceAsStream(String resource) throws IOException {
    return (new URL(resource)).openStream();
  }
}
