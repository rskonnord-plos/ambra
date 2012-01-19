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
 * The is the interface for classes wishing to handle the updates intercepted by the
 * {@link FilterResolver FilterResolver}.
 * 
 * @author Ronald Tschalär
 */
public interface FilterHandler {
  /** 
   * Signals that a graph was created. 
   * 
   * @param filterGraph the filter-resolver graph that was created
   * @param realGraph   the underlying "real" graph
   * @throws ResolverException if something's wrong with the URI
   */
  public void graphCreated(URI filterGraph, URI realGraph) throws ResolverException;

  /** 
   * Signals that a graph was removed. 
   * 
   * @param filterGraph the filter-resolver graph that was removed
   * @param realGraph   the underlying "real" graph
   * @throws ResolverException if something's wrong with the URI
   */
  public void graphRemoved(URI filterGraph, URI realGraph) throws ResolverException;

  /** 
   * Signals that a graph was modified. 
   * 
   * @param filterGraph the filter-resolver graph that was modified
   * @param realGraph   the underlying "real" graph
   * @param stmts       the list of statements being inserted or deleted
   * @param occurs      if true the statements are being inserted; otherwise they're being deleted
   * @param resolverSession the resolver session
   * @throws ResolverException if some error occurred; this should only be thrown for
   *                           non-recoverable, fatal errors
   */
  public void graphModified(URI filterGraph, URI realGraph, Statements stmts, boolean occurs,
                            ResolverSession resolverSession) throws ResolverException;

  /** 
   * Allows a handler to be notified of transaction events.
   * 
   * @return the xa-resource to associate with the filter
   */
  public XAResource getXAResource();

  /** 
   * Indicates the current transaction was abnormally aborted.
   */
  public void abort();

  /** 
   * Indicates the handler is being closed. Usually invoked on system exit.
   */
  public void close();
}
