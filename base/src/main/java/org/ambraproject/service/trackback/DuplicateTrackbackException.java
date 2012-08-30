/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
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

/**
 * Exception indicating that a trackback for an article with the given blog url already exists
 *
 * @author Alex Kudlick 4/4/12
 */
public class DuplicateTrackbackException extends Exception {

  public DuplicateTrackbackException(String articleDoi, String url) {
    super("A trackback for article " + articleDoi + " and url " + url + " already exists");
  }
}
