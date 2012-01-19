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

import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.CollectionType;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
  */
@UriPrefix(Rdf.topaz)
@Entity(graph = "ri", types = {Rdf.topaz + "SpecialMappers"})
public class SpecialMappers {
  private String               id;
  private List<String>         list      = new ArrayList<String>();
  private List<String>         bag       = new ArrayList<String>();
  private List<String>         alt       = new ArrayList<String>();
  private List<String>         seq       = new ArrayList<String>();
  private List<SpecialMappers> assocList = new ArrayList<SpecialMappers>();
  private List<SpecialMappers> assocSeq  = new ArrayList<SpecialMappers>();
  private String               name;

  /**
   * Creates a new SpecialMappers object.
   */
  public SpecialMappers() {
  }

  /**
   * Creates a new SpecialMappers object.
   *
   * @param id DOCUMENT ME!
   */
  public SpecialMappers(String id) {
    this.id                              = id;
    this.name                            = id;
  }

  /**
   * Get id.
   *
   * @return id as String.
   */
  public String getId() {
    return id;
  }

  /**
   * Set id.
   *
   * @param id the value to set.
   */
  @Id
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get list.
   *
   * @return list as a List of Strings.
   */
  public List<String> getList() {
    return list;
  }

  /**
   * Set lits.
   *
   * @param list the value to set.
   */
  @Predicate(collectionType = CollectionType.RDFLIST)
  public void setList(List<String> list) {
    this.list = list;
  }

  /**
   * Get bag.
   *
   * @return bag as a List of Strings.
   */
  public List<String> getBag() {
    return bag;
  }

  /**
   * Set bag.
   *
   * @param bag the value to set.
   */
  @Predicate(collectionType = CollectionType.RDFBAG)
  public void setBag(List<String> bag) {
    this.bag = bag;
  }

  /**
   * Get alt.
   *
   * @return alt as a List of Strings.
   */
  public List<String> getAlt() {
    return alt;
  }

  /**
   * Set alt.
   *
   * @param alt the value to set.
   */
  @Predicate(collectionType = CollectionType.RDFALT)
  public void setAlt(List<String> alt) {
    this.alt = alt;
  }

  /**
   * Get seq.
   *
   * @return seq as a List of Strings.
   */
  public List<String> getSeq() {
    return seq;
  }

  /**
   * Set seq.
   *
   * @param seq the value to set.
   */
  @Predicate(collectionType = CollectionType.RDFSEQ)
  public void setSeq(List<String> seq) {
    this.seq = seq;
  }

  /**
   * Get assocList.
   *
   * @return assocList as a List of SpecialMappers.
   */
  public List<SpecialMappers> getAssocList() {
    return assocList;
  }

  /**
   * Set assocList.
   *
   * @param assocList the value to set.
   */
  @Predicate(collectionType = CollectionType.RDFLIST)
  public void setAssocList(List<SpecialMappers> assocList) {
    this.assocList = assocList;
  }

  /**
   * Get assocSeq.
   *
   * @return assocSeq as a List of SpecialMappers.
   */
  public List<SpecialMappers> getAssocSeq() {
    return assocSeq;
  }

  /**
   * Set assocSeq.
   *
   * @param assocSeq the value to set.
   */
  @Predicate(collectionType = CollectionType.RDFSEQ)
  public void setAssocSeq(List<SpecialMappers> assocSeq) {
    this.assocSeq = assocSeq;
  }

  /**
   * Get name.
   *
   * @return name as String.
   */
  public String getName() {
    return name;
  }

  /**
   * Set name.
   *
   * @param name the value to set.
   */
  @Predicate
  public void setName(String name) {
    this.name = name;
  }
}
