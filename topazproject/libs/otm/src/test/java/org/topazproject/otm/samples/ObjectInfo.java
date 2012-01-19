/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.samples;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.PredicateMap;
import org.topazproject.otm.annotations.Rdf;

/**
 * Model for the objects that are stored mostly in mulgara and partially in the blob-store
 * (fedora).<p>
 *
 * Much of the structure here mirrors how things were originally stored in fedora.
 *
 * @author Eric Brown
 */
@Entity(model = "ri")
public class ObjectInfo {
  @Id
  private URI uri;

  // dublin-core predicates:
  @Predicate(uri = Rdf.dc + "title")
  private String title;
  @Predicate(uri = Rdf.dc + "description", dataType = Rdf.rdf + "XMLLiteral")
  private String description;
  @Predicate(uri = Rdf.dc + "creator")
  private Set<String> authors = new HashSet<String>();
  @Predicate(uri = Rdf.dc + "date", dataType = Rdf.xsd + "date")
  private Date date;
  @Predicate(uri = Rdf.dc + "identifier")
  private String identifier; // looks like a uri -- doi as a uri (for doi to uri conversion?)
  @Predicate(uri = Rdf.dc + "rights", dataType = Rdf.rdf + "XMLLiteral")
  private String rights;
  /** Will be: http://purl.org/dc/dcmitype/{Text|StillImage|Dataset|MovingImage|Sound} */
  @Predicate(uri = Rdf.dc + "type")
  private URI dc_type;
  @Predicate(uri = Rdf.dc + "contributor")
  private Set<String> contributors = new HashSet<String>();

  @Predicate(uri = Rdf.dc_terms + "isPartOf")
  private Article isPartOf;

  // Topaz predicates:

  @Predicate(uri = Rdf.topaz + "nextObject")
  private ObjectInfo nextObject;
  @Predicate(uri = Rdf.topaz + "isPID")
  private String pid;
  /** The state of this article (or its parts). */
  @Predicate(uri = Rdf.topaz + "articleState")
  private int state;
  @Predicate(uri = Rdf.topaz + "hasRepresentation")
  private Set<String> representations = new HashSet<String>(); // PDF, TIF, PNG_S, DOC, ...
  @Predicate(uri = Rdf.topaz + "contextElement")
  private String contextElement;

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
   * @return the authors
   */
  public Set<String> getAuthors() {
    return authors;
  }

  /**
   * @param authors the set of authors for this object
   */
  public void setAuthors(Set<String> authors) {
    this.authors = authors;
  }

  /**
   * @return the contextElement
   */
  public String getContextElement() {
    return contextElement;
  }

  /**
   * @param contextElement the contextElement to set
   */
  public void setContextElement(String contextElement) {
    this.contextElement = contextElement;
  }

  /**
   * @return the contributors
   */
  public Set<String> getContributors() {
    return contributors;
  }

  /**
   * @param contributors the contributors to set
   */
  public void setContributors(Set<String> contributors) {
    this.contributors = contributors;
  }

  /**
   * @return the data
   */
  public Map<String, List<String>> getData() {
    return data;
  }

  /**
   * @param data the data to set
   */
  public void setData(Map<String, List<String>> data) {
    this.data = data;
  }

  /**
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
   * @param date the date to set
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * @return the dc_type
   */
  public URI getDc_type() {
    return dc_type;
  }

  /**
   * @param dc_type the dc_type to set
   */
  public void setDc_type(URI dc_type) {
    this.dc_type = dc_type;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the id
   */
  public URI getUri() {
    return uri;
  }

  /**
   * @param id the id to set
   */
  public void setUri(URI uri) {
    this.uri = uri;
  }

  /**
   * @return the identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * @param identifier the identifier to set
   */
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
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
   * @return the nextObject
   */
  public ObjectInfo getNextObject() {
    return nextObject;
  }

  /**
   * @param nextObject the nextObject to set
   */
  public void setNextObject(ObjectInfo nextObject) {
    this.nextObject = nextObject;
  }

  /**
   * @return the pid
   */
  public String getPid() {
    return pid;
  }

  /**
   * @param pid the pid to set
   */
  public void setPid(String pid) {
    this.pid = pid;
  }

  /**
   * @return the representations
   */
  public Set<String> getRepresentations() {
    return representations;
  }

  /**
   * @param representations the representations to set
   */
  public void setRepresentations(Set<String> representations) {
    this.representations = representations;
  }

  /**
   * @return the rights
   */
  public String getRights() {
    return rights;
  }

  /**
   * @param rights the rights to set
   */
  public void setRights(String rights) {
    this.rights = rights;
  }

  /**
   * @return the state
   */
  public int getState() {
    return state;
  }

  /**
   * @param state the state to set
   */
  public void setState(int state) {
    this.state = state;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }
}
