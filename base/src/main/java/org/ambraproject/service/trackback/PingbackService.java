/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2007-2012 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

package org.ambraproject.service.trackback;

import org.apache.xmlrpc.XmlRpcException;

import java.net.URI;

public interface PingbackService extends LinkbackService {

  /**
   * Persist a record of an incoming pingback notification that an external page (such as a blog article) has linked to
   * a local journal article.
   * <p/>
   * Implementing methods should return without throwing an exception if and only if the pingback was valid and recorded
   * successfully. In this case, we will respond to the pinging server with a string indicating success. Else, the
   * pinging server will receive the error code corresponding to the exception. Exception objects thrown from this
   * method should always be created with {@link PingbackFault#getException} to provide a meaningful error code. Any
   * uncaught {@link RuntimeException}s will cause the pinging server to receive an error code of 0 ("Generic fault").
   *
   * @param sourceUri    the external page where (the pingback says) the inbound link exists
   * @param targetUri    an absolute URI of the target of the inbound link (will be {@code http://}, not {@code info:})
   * @param pbServerHost the hostname of the address to which the pingback was sent
   * @return the ID of the newly created pingback record
   * @throws XmlRpcException if the pingback is invalid
   */
  public abstract Long createPingback(URI sourceUri, URI targetUri, String pbServerHost) throws XmlRpcException;

}
