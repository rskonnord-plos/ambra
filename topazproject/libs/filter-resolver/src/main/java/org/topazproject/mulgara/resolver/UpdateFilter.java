/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.mulgara.resolver;

import java.net.URI;

/** 
 * This specifies the filter to be used by updates. The filter must have a public no-argument
 * constructor.
 *
 * <p>Fedora imposes various restrictions on the PID (such as the length and the allowed characters)
 * and on the contents of the RELS-EXT datastream (such as that the rdf:about attribute must contain
 * the URI <var>info:fedora/&lt;pid&gt;</var>). Update-filters are used to map the subject-URI's to
 * valid PID's and URI's for RELS-EXT.
 * 
 * @author Ronald Tschalär
 */
public interface UpdateFilter {
  /** 
   * Get the fedora PID under which to store the triples. This is run once for each
   * (subject, datastream) pair being updated and controls the fedora object in which the
   * triples are to be stored. It may do one of two things:
   * <ol>
   *   <li>return a fedora PID in the form &lt;namespace&gt;:&lt;id&gt;.
   *   <li>return null. In this case the triples will not be stored in fedora.
   * </ol>
   * 
   * @param subject    the subject node being updated
   * @param models     the list of models being queried
   * @param datastream the datastream being updated
   * @return the fedora PID to store the triples under, or null to ignore them
   */
  public String getFedoraPID(URI subject, String[] models, String datastream);

  /** 
   * Process a subject URI. This is run once for each (subject, datastream) pair being updated and
   * controls the rewriting of subject nodes in the triples as their put into fedora. It may do one
   * of two things:
   * <ol>
   *   <li>return the URI unmodified. In this case the triples are stored in fedora as is.
   *   <li>return a new URI. In this case the triples will be inserted with the subject-node
   *       changed to the new URI.
   * </ol>
   * 
   * @param subject    the subject node being updated
   * @param models     the list of models being queried
   * @param datastream the datastream being updated
   * @return the new subject URI
   */
  public URI processSubject(URI subject, String[] models, String datastream);
}
