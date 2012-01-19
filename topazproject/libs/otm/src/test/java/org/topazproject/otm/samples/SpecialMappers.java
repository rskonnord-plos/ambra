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

import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.annotations.Predicate;

@UriPrefix(Rdf.topaz)
@Entity(model = "ri")
public class SpecialMappers {
  @Id
  public String id;
  @Predicate(storeAs=Predicate.StoreAs.rdfList)
  public List<String> list = new ArrayList<String>();
  @Predicate(storeAs=Predicate.StoreAs.rdfBag)
  public List<String> bag = new ArrayList<String>();
  @Predicate(storeAs=Predicate.StoreAs.rdfAlt)
  public List<String> alt = new ArrayList<String>();
  @Predicate(storeAs=Predicate.StoreAs.rdfSeq)
  public List<String> seq = new ArrayList<String>();

  public SpecialMappers() {
  }

  public SpecialMappers(String id) {
    this.id = id;
  }
}
