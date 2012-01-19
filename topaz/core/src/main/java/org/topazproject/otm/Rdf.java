/* $HeadURL::                                                                            $
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
package org.topazproject.otm;

/**
 * Definitions of some standard uris.
 * @author Pradeep Krishnan
 */
public interface Rdf {
  /** help compose xsd literal ranges */
  public static final String xsd      = "http://www.w3.org/2001/XMLSchema#";

  /** help compose rdf defined URIs */
  public static final String rdf      = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  /** help compose mulgara defined URIs */
  public static final String mulgara  = "http://mulgara.org/mulgara#";
  public static final String tucana   = mulgara;

  /** help compose dc defined URIs */
  public static final String dc       = "http://purl.org/dc/elements/1.1/";

  /** help compose dc_terms defined URIs */
  public static final String dc_terms = "http://purl.org/dc/terms/";

  /** help compose topaz defined URIs */
  public static final String topaz    = "http://rdf.topazproject.org/RDF/";

  /** help compose fedora defined URIs */
  public static final String fedora   = "info:fedora/";

  /** help compose foaf defined URIs */
  public static final String foaf     = "http://xmlns.com/foaf/0.1/";
}
