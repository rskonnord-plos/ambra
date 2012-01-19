/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.annotations.Predicate;

/**
 * Model for a subset of the elements of the Dublin Core metadata
 * specification. Details on the specification and the individual elements can
 * be found at http://dublincore.org/documents/dcmi-terms/.
 *
 * Please note that in most cases the function names map directly into the term
 * name.
 *
 * @author Amit Kapoor
 */
public class DublinCore implements Serializable {
  private static final long serialVersionUID = 5420520141667994639L;

  private String         title;
  private String         description;
  private Set<String>    creators = new HashSet<String>();
  private Date           date;
  private String         identifier;
  private String         rights;
  private URI            type;
  private Set<String>    contributors = new HashSet<String>();
  private Set<String>    subjects = new HashSet<String>();
  private String         language;
  private String         publisher;
  private String         format;
  private Object         source;
  private Date           available;
  private Date           issued;
  private Date           submitted;
  private Date           accepted;
  private Integer        copyrightYear;
  private Set<String>    summary;
  private Citation       bibliographicCitation;
  private Date           created;
  private Set<License>   license;
  private Date           modified;
  private List<Citation> references;
  private URI            conformsTo;

  /**
   * Empty contructor
   */
  public DublinCore() {
  }

  /**
   * Return the list of creators of the object. Examples of a Creator include a person, an
   * organization, or a service. Typically, the name of a Creator should be used to indicate the
   * entity.
   *
   * @return the creators
   */
  public Set<String> getCreators() {
    return creators;
  }

  /**
   * Set the list of creators of the object.
   *
   * @param creators the set of creators for this object
   */
  @Predicate(uri = "dc:creator")
  public void setCreators(Set<String> creators) {
    this.creators = creators;
  }

  /**
   * Return the list of contributors. Examples of a Contributor include a person, an organization,
   * or a service. Typically, the name of a Contributor should be used to indicate the entity.
   *
   * @return the contributors
   */
  public Set<String> getContributors() {
    return contributors;
  }

  /**
   * Set the list of contributors.
   *
   * @param contributors the contributors to set
   * @see #getContributors
   */
  @Predicate(uri = "dc:contributor")
  public void setContributors(Set<String> contributors) {
    this.contributors = contributors;
  }

  /**
   * Return the date. Date may be used to express temporal information at any level of granularity.
   * Recommended best practice is to use an encoding scheme, such as the W3CDTF profile of ISO 8601
   * [W3CDTF].
   *
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
   * Set the date.
   *
   * @param date the date to set
   * @see #getDate
   */
  @Predicate(uri = "dc:date", dataType = "xsd:date")
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * Return the type of the object. Recommended best practice is to use a controlled vocabulary such
   * as the DCMI Type Vocabulary [DCMITYPE]. To describe the file format, physical medium, or
   * dimensions of the resource, use the Format element.
   *
   * @return the type
   */
  public URI getType() {
    return type;
  }

  /**
   * Set the type of the object.
   *
   * @param type the type to set
   * @see #getType
   */
  @Predicate(uri = "dc:type")
  public void setType(URI type) {
    this.type = type;
  }

  /**
   * Return the description of the object. Description may include but is not limited to: an
   * abstract, a table of contents, a graphical representation, or a free-text account of the
   * resource.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the description of the object.
   *
   * @param description the description to set
   * @see #getDescription
   */
  @Predicate(uri = "dc:description", dataType = "rdf:XMLLiteral")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Return the identifier of the object. Recommended best practice is to identify the resource by
   * means of a string conforming to a formal identification system.
   *
   * @return the identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Set the identifier of the object.
   *
   * @param identifier the identifier to set
   * @see #getIdentifier
   */
  @Predicate(uri = "dc:identifier")
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  /**
   * Return the rights of the objects. Typically, rights information includes a statement about
   * various property rights associated with the resource, including intellectual property rights.
   *
   * @return the rights
   */
  public String getRights() {
    return rights;
  }

  /**
   * Set the rights of the object.
   *
   * @param rights the rights to set
   * @see #getRights
   */
  @Predicate(uri = "dc:rights", dataType = "rdf:XMLLiteral")
  public void setRights(String rights) {
    this.rights = rights;
  }

  /**
   * Return the title of the object. Typically, a Title will be a name by which the resource is
   * formally known.
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set the title of the object.
   *
   * @param title the title to set
   * @see #getTitle
   */
  @Predicate(uri = "dc:title", dataType = "rdf:XMLLiteral")
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Return the list of subjects the object is about. Typically, the topic will be represented
   * using keywords, key phrases, or classification codes. Recommended best practice is to use a
   * controlled vocabulary. To describe the spatial or temporal topic of the resource, use the
   * Coverage element.
   *
   * @return the subjects the object is about
   */
  public Set<String> getSubjects() {
    return subjects;
  }

  /**
   * Set the list of subjects the object is about.
   *
   * @param subjects the subjects the object is about
   * @see #getSubjects
   */
  @Predicate(uri = "dc:subject", dataType = "rdf:XMLLiteral")
  public void setSubjects(Set<String> subjects) {
    this.subjects = subjects;
  }

  /**
   * Get the language of the object. Recommended best practice is to use a controlled vocabulary
   * such as RFC 3066 [RFC3066].
   *
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Set the language of the object.
   *
   * @param language the language to set
   * @see #getLanguage
   */
  @Predicate(uri = "dc:language")
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * Get the name of the publisher of this object. Examples of a Publisher include a person, an
   * organization, or a service. Typically, the name of a Publisher should be used to indicate the
   * entity.
   *
   * @return the publisher
   */
  public String getPublisher() {
    return publisher;
  }

  /**
   * Set the name of the publisher of this object.
   *
   * @param publisher the name of the publisher
   * @see #getPublisher
   */
  @Predicate(uri = "dc:publisher", dataType = "rdf:XMLLiteral")
  public void setPublisher(String publisher) {
    this.publisher = publisher;
   }

  /**
   * Get the format of the object. Recommended best practice is to use a controlled vocabulary such
   * as the list of Internet Media Types [MIME].
   *
   * @return format
   */
  public String getFormat() {
    return format;
  }

  /**
   * Set the format of the object.
   *
   * @param format the dc:format to set
   * @see #getFormat
   */
  @Predicate(uri = "dc:format")
  public void setFormat(String format) {
    this.format = format;
  }

  /**
   * Get the date the object was made available.
   *
   * @return the date the object was made available
   */
  public Date getAvailable() {
    return available;
  }

  /**
   * Set the date the object was made available.
   *
   * @param available the date the object was made available
   * @see #getAvailable
   */
  @Predicate(uri = "dcterms:available", dataType = "xsd:date")
  public void setAvailable(Date available) {
    this.available = available;
  }

  /**
   * Get the issued date. This is the date of formal issuance (e.g., publication) of the resource.
   *
   * @return the issued date
   */
  public Date getIssued() {
    return issued;
  }

  /**
   * Set the issued date.
   *
   * @param issued the date the object was issued
   * @see #getIssued
   */
  @Predicate(uri = "dcterms:issued", dataType = "xsd:date")
  public void setIssued(Date issued) {
    this.issued = issued;
  }

  /**
   * Get the submission date (e.g. thesis, articles, etc.).
   *
   * @return the submission date
   */
  public Date getSubmitted() {
    return submitted;
  }

  /**
   * Set the submission date.
   *
   * @param submitted the date the object was submitted
   * @see #getSubmitted
   */
  @Predicate(uri = "dcterms:dateSubmitted", dataType = "xsd:date")
  public void setSubmitted(Date submitted) {
    this.submitted = submitted;
  }

  /**
   * Return the acceptance date (e.g. of thesis by university department, of article by journal,
   * etc.).
   *
   * @return the accpetance date
   */
  public Date getAccepted() {
    return accepted;
  }

  /**
   * Set the acceptance date.
   *
   * @param accepted the date the object was accepted
   * @see #getAccepted
   */
  @Predicate(uri = "dcterms:dateAccepted", dataType = "xsd:date")
  public void setAccepted(Date accepted) {
    this.accepted = accepted;
  }

  /**
   * Return the year of the copyright.
   *
   * @return the year of the copyright
   */
  public Integer getCopyrightYear() {
    return copyrightYear;
  }

  /**
   * Set the year of the copyright.
   *
   * @param copyrightYear the year of the copyright
   * @see #getCopyrightYear
   */
  @Predicate(uri = "dcterms:dateCopyrighted")
  public void setCopyrightYear(Integer copyrightYear) {
    this.copyrightYear = copyrightYear;
  }

  /**
   * Return the source of the object. The described resource may be derived from the related
   * resource in whole or in part. Recommended best practice is to identify the related resource by
   * means of a string conforming to a formal identification system.
   *
   * @return the source of the object
   */
  public Object getSource() {
    return source;
  }

  /**
   * Set the source of the object.
   *
   * @param source the source of the object
   * @see #getSource
   */
  @Predicate(uri = "dc:source")
  public void setSource(Object source) {
    this.source = source;
  }

  /**
   * Return the abstract/summary on the object.
   *
   * @return the abstract/summary of the object
   */
  public Set<String> getSummary() {
    return summary;
  }

  /**
   * Set the abstract/summary of the object.
   *
   * @param summary the summary/abstract of the object
   * @see #getSummary
   */
  @Predicate(uri = "dcterms:abstract")
  public void setSummary(Set<String> summary) {
    this.summary = summary;
  }

  /**
   * Return the bibliographic citation for the object.
   *
   * @return bibliographic citation for the object
   */
  public Citation getBibliographicCitation() {
    return bibliographicCitation;
  }

  /**
   * Set the bibliographic citation for the object.
   *
   * @param bibliographicCitation bibliographic citation for the object
   * @see #getBibliographicCitation
   */
  @Predicate(uri = "dcterms:bibliographicCitation", cascade = { CascadeType.child })
  public void setBibliographicCitation(Citation bibliographicCitation) {
    this.bibliographicCitation = bibliographicCitation;
  }

  /**
   * Return the creation date.
   *
   * @return the creation date
   */
  public Date getCreated() {
    return created;
  }

  /**
   * Set the creation date.
   *
   * @param created the date the object was created
   */
  @Predicate(uri = "dcterms:created", dataType = "xsd:date")
  public void setCreated(Date created) {
    this.created = created;
  }

  /**
   * Return the set of licenses for this object. A licenses is a legal document giving official
   * permission to do something with the resource.
   *
   * @return the set of licenses for this object
   */
  public Set<License> getLicense() {
    return license;
  }

  /**
   * Set the set of licenses for this object.
   *
   * @param license the set of licenses governing this object
   * @see #getLicense
   */
  @Predicate(uri = "dcterms:license")
  public void setLicense(Set<License> license) {
    this.license = license;
  }

  /**
   * Return the modified date.
   *
   * @return the modified date
   */
  public Date getModified() {
    return modified;
  }

  /**
   * Set the modified date.
   *
   * @param modified the date the object was modified
   * @see #getModified
   */
  @Predicate(uri = "dcterms:modified", dataType = "xsd:date")
  public void setModified(Date modified) {
    this.modified = modified;
  }

  /**
   * Return the set of objects referred to by this object.
   *
   * @return the set of objects referred to by this object
   */
  public List<Citation> getReferences() {
    return references;
  }

  /**
   * Set the set of objects referred to by this object.
   *
   * @param references the set of objects referred to by this object
   * @see #getReferences
   */
  @Predicate(uri = "dcterms:references", cascade = { CascadeType.child } )
  public void setReferences(List<Citation> references) {
    this.references = references;
  }

  /**
   * Return the URI identifying the standard the object conforms to. For example, for an XML
   * document this can point to the URI identifying the DTD.
   *
   * @return the URI identifying the standard the object conforms to
   */
  public URI getConformsTo() {
    return conformsTo;
  }

  /**
   * Set the format this object conforms to.
   *
   * @param conformsTo the URI specifying the conformance standard
   * @see #getConformsTo
   */
  @Predicate(uri = "dcterms:conformsTo")
  public void setConformsTo(URI conformsTo) {
    this.conformsTo = conformsTo;
  }
}
