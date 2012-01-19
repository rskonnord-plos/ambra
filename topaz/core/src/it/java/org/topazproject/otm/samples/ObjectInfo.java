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
package org.topazproject.otm.samples;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Alias;
import org.topazproject.otm.annotations.Aliases;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.PredicateMap;

/**
 * Graph for the objects that are stored mostly in mulgara and partially in the blob-store
 * (fedora).<p>
 *
 * Much of the structure here mirrors how things were originally stored in fedora.
 *
 * @author Eric Brown
 */
@Entity(graph = "ri")
@Aliases({@Alias(alias = "bazAlias", value = "http://www.baz.com"),
          @Alias(alias = "barAlias", value = "http://www.bar.org/")})
public class ObjectInfo {
  private URI uri;

  // dublin-core predicates:
  private String title;
  private String description;
  private Set<String> authors = new HashSet<String>();
  private Date date;
  private String identifier; // looks like a uri -- doi as a uri (for doi to uri conversion?)
  private String rights;
  private URI dc_type;
  private Set<String> contributors = new HashSet<String>();

  private Article isPartOf;

  // Topaz predicates:

  private ObjectInfo nextObject;
  private String pid;
  private int state;
  private Set<String> representations = new HashSet<String>(); // PDF, TIF, PNG_S, DOC, ...
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
  @Predicate(uri = Rdf.dc + "creator")
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
  @Predicate(uri = Rdf.topaz + "contextElement")
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
  @Predicate(uri = Rdf.dc + "contributor")
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
  @PredicateMap
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
  @Predicate(uri = "dc:date", dataType = "xsd:date")
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
   * Will be: http://purl.org/dc/dcmitype/{Text|StillImage|Dataset|MovingImage|Sound}
   *
   * @param dc_type the dc_type to set
   */
  @Predicate(uri = Rdf.dc + "type")
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
  @Predicate(uri = Rdf.dc + "description", dataType = Rdf.rdf + "XMLLiteral")
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
  @Id
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
  @Predicate(uri = Rdf.dc + "identifier")
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
  @Predicate(uri = Rdf.dc_terms + "isPartOf", fetch=FetchType.eager)
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
  @Predicate(uri = Rdf.topaz + "nextObject")
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
  @Predicate(uri = Rdf.topaz + "isPID")
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
  @Predicate(uri = Rdf.topaz + "hasRepresentation", cascade={CascadeType.child})
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
  @Predicate(uri = Rdf.dc + "rights", dataType = Rdf.rdf + "XMLLiteral")
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
   * The state of this article (or its parts).
   * @param state the state to set
   */
  @Predicate(uri = Rdf.topaz + "articleState")
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
  @Predicate(uri = Rdf.dc + "title")
  public void setTitle(String title) {
    this.title = title;
  }
}
