/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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
package org.topazproject.otm.id;

import java.util.UUID;

import org.topazproject.otm.Session;
import org.topazproject.otm.ClassMetadata;

/**
 * Generate unique ids based on rfc 4122.
 *
 * Note that there are four sub-types of UUIDs defined by rfc 4122. Because we dont have
 * access to the mac-address via java, we use randomly generated UUIDs. It may be desireable
 * to use the mac-address form though. (There is some suggesting that Java 1.6 may have
 * access to this?)
 *
 * @see java.util.UUID
 * @author Eric Brown
 */
public class GUIDGenerator implements IdentifierGenerator {
  private String uriPrefix;

  public String generate(ClassMetadata cm, Session sess) {
    return uriPrefix + UUID.randomUUID().toString();
  }

  public void setUriPrefix(String uriPrefix) {
    this.uriPrefix = uriPrefix;
  }
}
