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

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;

/**
 * The general contract between a class that generates unique identifiers and the Session.
 * It is not intended that this interface ever be exposed to the application. It is intended
 * that users implement this interface to provide custom identifier generation strategies.
 *
 * @see org.hibernate.id.IdentifierGenerator
 * @author Eric Brown
 */
public interface IdentifierGenerator {
  /**
   * Generate a new identifier.
   *
   * @param cm the ClassMetadata of the class for which the id is being generated
   * @param sess the current session
   *
   * @return a new identifier
   */
  String generate(ClassMetadata cm, Session sess) throws OtmException;

  /**
   * Set the uri-prefix to use for the generated ids
   *
   * Example: http://rdf.topazproject.org/MyClass/ids#
   *
   * @param uriPrefix the uri prefix to use for id generation.
   */
  void setUriPrefix(String uriPrefix);
}
