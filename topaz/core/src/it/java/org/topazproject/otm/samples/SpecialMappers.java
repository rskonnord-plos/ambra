/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.samples;

import java.util.List;
import java.util.ArrayList;

import org.topazproject.otm.CollectionType;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.annotations.Predicate;

@UriPrefix(Rdf.topaz)
@Entity(model = "ri", type=Rdf.topaz + "SpecialMappers")
public class SpecialMappers {
  @Id
  public String id;
  @Predicate(collectionType = CollectionType.RDFLIST)
  public List<String> list = new ArrayList<String>();
  @Predicate(collectionType = CollectionType.RDFBAG)
  public List<String> bag = new ArrayList<String>();
  @Predicate(collectionType = CollectionType.RDFALT)
  public List<String> alt = new ArrayList<String>();
  @Predicate(collectionType = CollectionType.RDFSEQ)
  public List<String> seq = new ArrayList<String>();

  @Predicate(collectionType = CollectionType.RDFLIST)
  public List<SpecialMappers> assocList = new ArrayList<SpecialMappers>();

  @Predicate(collectionType = CollectionType.RDFSEQ)
  public List<SpecialMappers> assocSeq = new ArrayList<SpecialMappers>();

  public String name;

  public SpecialMappers() {
  }

  public SpecialMappers(String id) {
    this.id = id;
    this.name = id;
  }
}
