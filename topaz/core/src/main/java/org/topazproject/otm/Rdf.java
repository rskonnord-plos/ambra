/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
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
