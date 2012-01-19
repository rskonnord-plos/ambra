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

import java.util.List;


/**
 * Policy Finder module that loads policy files from a thread-context classloader. This version
 * supports look up of policied by id reference.
 *
 * @author Pradeep Krishnan
 */
public class ResourceReferencePolicyModule extends AbstractPolicyModule {
  /*
   * inherited javadoc
   */
  public ResourceReferencePolicyModule() {
    super();
  }

  /*
   * inherited javadoc
   */
  public ResourceReferencePolicyModule(File schemaFile) {
    super(schemaFile);
  }

  /*
   * inherited javadoc
   */
  public ResourceReferencePolicyModule(List resources) {
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
   * inherited javadoc
   */
  public InputStream getPolicyResourceAsStream(String resource) throws IOException {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    InputStream in = cl.getResourceAsStream(resource);

    if (in == null)
      throw new IOException(resource + " not found in classpath");

    return in;
  }
}
