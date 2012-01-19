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

package org.topazproject.mulgara.resolver;

import java.net.URI;
import javax.transaction.xa.XAResource;

import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/** 
 * Superclass for filter handlers. All methods are no-ops.
 * 
 * @author Ronald Tschalär
 */
public abstract class AbstractFilterHandler implements FilterHandler {
  public void graphCreated(URI filterGraph, URI realGraph) throws ResolverException {
  }

  public void graphRemoved(URI filterGraph, URI realGraph) throws ResolverException {
  }

  public void graphModified(URI filterGraph, URI realGraph, Statements stmts, boolean occurs,
                            ResolverSession resolverSession) throws ResolverException {
  }

  public XAResource getXAResource() {
    return null;
  }

  public void abort() {
  }

  public void close() {
  }
}
