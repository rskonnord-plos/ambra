/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.owl;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Id;

/**
 * A class in otm to represent the union we need to wrap ranges for an ObjectProperty.
 *
 * @author Eric Brown
 */
@Entity(model="metadata")
public class DomainUnion {
  /** The id of this domain union -- this should really be generated */
  @Id
  public URI id;
  /** The list of possible owl classes for this union */
  @Predicate(uri=Rdf.owl + "unionOf",storeAs=Predicate.StoreAs.rdfList)
  public List<OwlClass> domains = new ArrayList<OwlClass>();
}

