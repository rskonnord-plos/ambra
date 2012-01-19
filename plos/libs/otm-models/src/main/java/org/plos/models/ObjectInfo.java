/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.PredicateMap;

/**
 * Model for the generic PLoS object. This is the base class for any PLoS
 * object that needs to be stored and retrieved.
 *
 * @author Eric Brown
 * @author Amit Kapoor
 */
@Entity(model = "ri")
public class ObjectInfo {
  @Id
  private URI id;

  // Dublin Core predicates
  @Embedded
  private DublinCore dublinCore = new DublinCore();

  @Predicate(uri = Rdf.dc_terms + "isPartOf")
  private Article isPartOf;

  // PLoS specific predicates:
  @Predicate(uri = Rdf.topaz + "nextObject")
  private ObjectInfo nextObject;

  @Predicate(uri = Rdf.topaz + "isPID")
  private String pid;

  /** The state of this object (or its parts). */
  @Predicate(uri = Rdf.topaz + "articleState")
  private int state;

  // PDF, TIF, PNG_S, DOC, ....
  @Predicate(uri = Rdf.topaz + "hasRepresentation")
  private Set<String> representations = new HashSet<String>();

  @Predicate(uri = Rdf.topaz + "contextElement")
  private String contextElement;

  @Predicate(uri = "http://prismstandard.org/namespaces/1.2/basic/eIssn")
  private String eIssn;

  /**
   * There is a convention for two dynamic predicates:
   * <ul>
   *  <li>&lt;topaz:<em>content-type</em>-contentType&gt;
   *  <li>&lt;topaz:<em>content-type</em>-objectSize&gt;
   * </ul>
   *
   * So the data map should have two entries. One for the contentType and one
   * for the objectSize.
   */
  @PredicateMap
  private Map<String, List<String>> data = new HashMap<String, List<String>>();

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
  public void setContextElement(String contextElement) {
    this.contextElement = contextElement;
  }

  /**
   * Return the data
   *
   * @return the data
   */
  public Map<String, List<String>> getData() {
    return data;
  }

  /**
   * Set the data
   *
   * @param data the data to set
   */
  public void setData(Map<String, List<String>> data) {
    this.data = data;
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
  public void setIsPartOf(Article isPartOf) {
    this.isPartOf = isPartOf;
  }

  /**
   * Get the next object
   *
   * @return the nextObject
   */
  public ObjectInfo getNextObject() {
    return nextObject;
  }

  /**
   * Set the next object
   *
   * @param nextObject the nextObject to set
   */
  public void setNextObject(ObjectInfo nextObject) {
    this.nextObject = nextObject;
  }

  /**
   * Get the PID associated with this
   *
   * @return the pid
   */
  public String getPid() {
    return pid;
  }

  /**
   * Set the PID associated with this
   *
   * @param pid the pid to set
   */
  public void setPid(String pid) {
    this.pid = pid;
  }

  /**
   * Return the list of representations
   *
   * @return the representations
   */
  public Set<String> getRepresentations() {
    return representations;
  }

  /**
   * Set the list of representations
   *
   * @param representations the representations to set
   */
  public void setRepresentations(Set<String> representations) {
    this.representations = representations;
  }

  /**
   * Get the state of the object
   *
   * @return the state
   */
  public int getState() {
    return state;
  }

  /**
   * Set the state of the object
   *
   * @param state the state to set
   */
  public void setState(int state) {
    this.state = state;
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
  public void setDublinCore(DublinCore dublinCore) {
    this.dublinCore = dublinCore;
  }

  /**
   * Get the e-issn of the publication in which this object was published.
   *
   * @return the e-issn.
   */
  public String getEIssn() {
    return eIssn;
  }

  /**
   * Set the e-issn of the publication in which this object was published.
   *
   * @param eIssn the e-issn.
   */
  public void setEIssn(String eIssn) {
    this.eIssn = eIssn;
  }
}
