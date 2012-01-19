/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
