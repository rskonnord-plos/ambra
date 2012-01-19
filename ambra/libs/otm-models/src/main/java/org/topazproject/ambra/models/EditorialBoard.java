/* $HeadURL::                                                                                     $
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
package org.topazproject.ambra.models;

import java.io.Serializable;
import java.net.URI;

import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;

/**
 * Editorial Board for an aggregation.
 *
 * @author Pradeep Krishnan
  */
@Entity(type = "plos:EditorialBoard", model = "ri")
public class EditorialBoard implements Serializable {
  @Id
  @GeneratedValue(uriPrefix = "id:editorialBoard/")
  private URI            id;
  @Predicate(uri = "dcterms:replaces")
  private EditorialBoard supersedes;
  @Predicate(uri = "dcterms:isReplacedBy")
  private EditorialBoard supersededBy;
  @Predicate(uri = "plos:editors", collectionType = CollectionType.RDFSEQ,
      cascade = { CascadeType.child })
  private List<UserProfile>      editors = new ArrayList();

  /**
   * Get id.
   *
   * @return id as URI.
   */
  public URI getId() {
    return id;
  }

  /**
   * Set id.
   *
   * @param id the value to set.
   */
  public void setId(URI id) {
    this.id = id;
  }

  /**
   * Get supersedes.
   *
   * @return supersedes as EditorialBoard.
   */
  public EditorialBoard getSupersedes() {
    return supersedes;
  }

  /**
   * Set supersedes.
   *
   * @param supersedes the value to set.
   */
  public void setSupersedes(EditorialBoard supersedes) {
    this.supersedes = supersedes;
  }

  /**
   * Get supersededBy.
   *
   * @return supersededBy as EditorialBoard.
   */
  public EditorialBoard getSupersededBy() {
    return supersededBy;
  }

  /**
   * Set supersededBy.
   *
   * @param supersededBy the value to set.
   */
  public void setSupersededBy(EditorialBoard supersededBy) {
    this.supersededBy = supersededBy;
  }

  /**
   * Get editors.
   *
   * @return editors as List.
   */
  public List<UserProfile> getEditors() {
    return editors;
  }

  /**
   * Set editors.
   *
   * @param editors the value to set.
   */
  public void setEditors(List<UserProfile> editors) {
    this.editors = editors;
  }
}
