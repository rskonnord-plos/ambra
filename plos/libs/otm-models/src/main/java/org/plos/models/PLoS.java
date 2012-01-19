/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

/**
 * Definitions of some standard uris.
 *
 * @author Amit Kapoor
 */
public interface PLoS {
  /** PLoS namespace */
  public static final String plos               = "http://rdf.plos.org/RDF/";
  /** Creative Commons namespace */
  public static final String creativeCommons    = "http://web.resource.org/cc/";
  /** Bibtex namespace */
  public static final String bibtex             = "http://purl.org/net/nknouf/ns/bibtex#";
  /** Prism namespace */
  public static final String prism              = "http://prismstandard.org/namespaces/1.2/basic/";

  /** Base name space for article types for PLoS */
  public static final String PLOS_ArticleType = plos + "articleType/";
  /** Base name for PLoS predicates of temporal types */
  public static final String PLoS_Temporal = plos + "temporal#";
  /** Base name for PLoS predicates for citations */
  public static final String PLoS_Citation = plos + "citation/";

  /** Base name for PLoS Citation types */
  public static final String PLoS_CitationTypes = PLoS_Citation + "type#";
}
