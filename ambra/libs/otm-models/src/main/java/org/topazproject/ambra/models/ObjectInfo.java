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
import java.util.Set;
import java.util.HashSet;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;

/**
 * Model for main objects that make up an article, including the article itself and things
 * like images, data-sets, additional docs, etc.
 *
 * @author Eric Brown
 * @author Amit Kapoor
 */
@Entity(types = {"topaz:ObjectInfo"}, graph = "ri")
public class ObjectInfo implements Serializable {
  private static final long serialVersionUID = 4074534426473235595L;

  private URI                 id;
  private DublinCore          dublinCore = new DublinCore();
  private Article             isPartOf;
  private Set<Representation> representations = new HashSet<Representation>();
  private String              contextElement;
  private String              eIssn;

  /**
   * Return the context for the object
   *
   * @return the contextElement
   */
  public String getContextElement() {
    return contextElement;
  }

  /**
   * Set the context for the object
   *
   * @param contextElement the contextElement to set
   */
  @Predicate(uri = "topaz:contextElement")
  public void setContextElement(String contextElement) {
    this.contextElement = contextElement;
  }

  /**
   * Return the identifier of the object
   *
   * @return the id
   */
  public URI getId() {
    return id;
  }

  /**
   * Set the identifier of the object
   *
   * @param id the id to set
   */
  @Id
  public void setId(URI id) {
    this.id = id;
  }

  /** 
   * @return the isPartOf 
   */
  public Article getIsPartOf() {
    return isPartOf;
  }

  /** 
   * @param isPartOf the isPartOf to set 
   */
  @Predicate(uri = "dcterms:isPartOf", fetch = FetchType.eager)
  public void setIsPartOf(Article isPartOf) {
    this.isPartOf = isPartOf;
  }

  /**
   * Return the list of representations
   *
   * @return the representations
   */
  public Set<Representation> getRepresentations() {
    return representations;
  }

  /**
   * Set the list of representations
   *
   * @param representations the representations to set
   */
  @Predicate(uri = "topaz:hasRepresentation", cascade = { CascadeType.child } )
  public void setRepresentations(Set<Representation> representations) {
    this.representations = representations;
  }

  /**
   * Gets the representation that matches the given name.
   *
   * @param name the name of this representation (PDF, XML, ...)
   *
   * @return matching representation or null
   */
  public Representation getRepresentation(String name) {
    if (getRepresentations() == null)
      return null;

    for (Representation rep : getRepresentations())
      if (name.equals(rep.getName()))
        return rep;

    return null;
  }

  /**
   * Return the dublin core predicate associated with this object
   *
   * @return the dublin core predicates
   */
  public DublinCore getDublinCore() {
    return dublinCore;
  }

  /**
   * Set the dublin core predicates associated with this object
   *
   * @param dublinCore the dublin core object
   */
  @Embedded
  public void setDublinCore(DublinCore dublinCore) {
    this.dublinCore = dublinCore;
  }

  /**
   * Get the e-issn of the publication in which this object was published.
   *
   * @return the e-issn.
   */
  public String geteIssn() {
    return eIssn;
  }

  /**
   * Set the e-issn of the publication in which this object was published.
   *
   * @param eIssn the e-issn.
   */
  @Predicate(uri = "prism:eIssn")
  public void seteIssn(String eIssn) {
    this.eIssn = eIssn;
  }

  public String toString() {
    return "ObjectInfo: [" +
           "id: " + getId() +
           ", contextElement: " + getContextElement() +
           "]";
  }
}
