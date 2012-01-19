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
   * Signals that a model was created. 
   * 
   * @param filterModel the filter-resolver model that was created
   * @param realModel   the underlying "real" model
   * @throws ResolverException if something's wrong with the URI
   */
  public void modelCreated(URI filterModel, URI realModel) throws ResolverException;

  /** 
   * Signals that a model was removed. 
   * 
   * @param filterModel the filter-resolver model that was removed
   * @param realModel   the underlying "real" model
   * @throws ResolverException if something's wrong with the URI
   */
  public void modelRemoved(URI filterModel, URI realModel) throws ResolverException;

  /** 
   * Signals that a model was modified. 
   * 
   * @param filterModel the filter-resolver model that was modified
   * @param realModel   the underlying "real" model
   * @param stmts       the list of statements being inserted or deleted
   * @param occurs      if true the statements are being inserted; otherwise they're being deleted
   * @param resolverSession the resolver session
   * @throws ResolverException if some error occurred; this should only be thrown for
   *                           non-recoverable, fatal errors
   */
  public void modelModified(URI filterModel, URI realModel, Statements stmts, boolean occurs,
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
