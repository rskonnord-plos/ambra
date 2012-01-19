/* $HeadURL::                                                                                     $
 * $Id: $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

import java.io.Serializable;
import java.net.URI;

import java.util.List;
import java.util.ArrayList;

import org.topazproject.otm.CollectionType;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Predicate.PropType;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * Citation information
 *
 * @author Eric Brown
 * @author Amit Kapoor
 */
@Entity(type = PLoS.bibtex + "Entry", model = "ri")
@UriPrefix(Rdf.topaz)
public class Citation implements Serializable {
  @Id
  private URI   id;

  @Predicate(uri = PLoS.bibtex + "hasKey", dataType = Rdf.xsd + "string")
  private String key;

  /* TODO: Restore to correct datatype. Stored as double because of bug in Mulgara */
  @Predicate(uri = PLoS.bibtex + "hasYear", dataType = Rdf.xsd + "double")
  private Integer year;

  @Predicate(uri=PLoS.PLoS_Temporal + "displayYear", dataType = Rdf.xsd + "string")
  private String displayYear;

  @Predicate(uri = PLoS.bibtex + "hasMonth", dataType = Rdf.xsd + "string")
  private String month;

  /* TODO: Restore to correct datatype .Stored as double because of bug in Mulgara */
  @Predicate(uri = PLoS.bibtex + "hasVolume", dataType = Rdf.xsd + "double")
  private Integer volumeNumber;

  @Predicate(uri=PLoS.prism + "volume", dataType = Rdf.xsd + "string")
  private String volume;

  @Predicate(uri = PLoS.bibtex + "hasNumber", dataType = Rdf.xsd + "string")
  private String issue;

  @Predicate(uri = Rdf.dc + "title", dataType = Rdf.rdf + "XMLLiteral")
  private String title;

  @Predicate(uri = PLoS.bibtex + "hasAddress", dataType = Rdf.xsd + "string")
  private String publisherLocation;

  @Predicate(uri = PLoS.bibtex + "hasPublisher", dataType = Rdf.xsd + "string")
  private String publisherName;

  @Predicate(uri = PLoS.bibtex + "hasPages", dataType = Rdf.xsd + "string")
  private String pages;

  @Predicate(uri = PLoS.bibtex + "hasJournal", dataType = Rdf.xsd + "string")
  private String journal;

  @Predicate(uri = PLoS.bibtex + "hasNote", dataType = Rdf.xsd + "string")
  private String note;

  @Predicate(uri = PLoS.plos + "hasEditorList", collectionType = CollectionType.RDFSEQ)
  private List<UserProfile> editors = new ArrayList<UserProfile>();

  @Predicate(uri = PLoS.plos + "hasAuthorList", collectionType = CollectionType.RDFSEQ)
  private List<UserProfile> authors = new ArrayList<UserProfile>();

  @Predicate(uri = PLoS.bibtex + "hasURL", dataType = Rdf.xsd + "string")
  private String url;

  @Predicate(uri = PLoS.bibtex + "hasAbstract", dataType = Rdf.xsd + "string")
  private String summary;

  @Predicate(uri = Rdf.rdf + "type", type = PropType.OBJECT)
  private String citationType;

  private static final long serialVersionUID = 6405781304940950306L;

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
   * The key or label referencing a citation from within another source (such as an article).
   *
   * @return the key or label for the citation (if available)
   */
  public String getKey() {
    return key;
  }

  /**
   * @param key the key or label for this citation
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * The year of publication or, for an unpublished work, the year it was written. Generally it
   * should consist of four numerals, such as 1984, although the standard styles can handle any year
   * whose last four nonpunctuation characters are numerals, such as '(about 1984)'
   *
   * @return the year of the citation (if available)
   */
  public Integer getYear() {
    return year;
  }

  /**
   * @param year the year of the citation
   */
  public void setYear(Integer year) {
    this.year = year;
  }

  /**
   * The year of publication or, for an unpublished work, the year it was
   * written. The reason for this predicate is because of misuse of this field
   * in references. This field should only be used if the data cannot be mapped
   * to Integer.
   *
   * @return the year of the citation (if available)
   */
  public String getDisplayYear() {
    return displayYear;
  }

  /**
   * @param displayYear the year of the citation
   */
  public void setDisplayYear(String displayYear) {
    this.displayYear = displayYear;
  }

  /**
   * @return the month of the citation (if available)
   */
  public String getMonth() {
    return month;
  }

  /**
   * @param month the month of the citation
   */
  public void setMonth(String month) {
    this.month = month;
  }

  /**
   * @return the volume this citation is in
   */
  public Integer getVolumeNumber() {
    return volumeNumber;
  }

  /**
   * @param volumeNumber the volume of this citation
   */
  public void setVolumeNumber(Integer volumeNumber) {
    this.volumeNumber = volumeNumber;
  }

  /**
   * This should be used only if volumeNumber cannot be used.
   * 
   * @return the volume this citation is in 
   */
  public String getVolume() {
    return volume;
  }

  /**
   * @param volume the volume of the journal
   */
  public void setVolume(String volume) {
    this.volume = volume;
  }

  /**
   * Return the number of a journal, magazine, technical report, or of a work in
   * a series. An issue of a journal or magazine is usually identified by its
   * volume and number; the organization that issues a technical report usually
   * gives it a number; and sometimes books are given numbers in a named
   * series.
   *
   * @return the issue of the citation's article
   */
  public String getIssue() {
    return issue;
  }

  /**
   * @param issue the issue of the citation's article
   */
  public void setIssue(String issue) {
    this.issue = issue;
  }

  /**
   * Return the title. Typically, a title will be a name by which the resource is formally known.
   *
   * @return the title of the citation's article
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title of the citation's article
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Return the publisher's location. This is usually the address of the publisher or other type
   * of institution. For major publishing houses, van Leunen recommends omitting the information
   * entirely. For small publishers, on the other hand, you can help the reader by giving the
   * complete address.
   *
   * @return the publisher's location
   */
  public String getPublisherLocation() {
    return publisherLocation;
  }

  /**
   * @param publisherLocation the location of the publisher
   */
  public void setPublisherLocation(String publisherLocation) {
    this.publisherLocation = publisherLocation;
  }

  /**
   * @return the publisher's name
   */
  public String getPublisherName() {
    return publisherName;
  }

  /**
   * @param publisherName the name of the publisher
   */
  public void setPublisherName(String publisherName) {
    this.publisherName = publisherName;
  }

  /**
   * Return the pages. This is one or more page numbers or range of numbers, such as 42-111 or
   * 7,41,73-97 or 43+ (the `+' in this last example indicates pages following that don't form a
   * simple range). To make it easier to maintain Scribe-compatible databases, the standard styles
   * convert a single dash (as in 7-33) to the double dash used in TeX to denote number ranges (as
   * in 7-33).
   *
   * @return the pages the citation is on
   */
  public String getPages() {
    return pages;
  }

  /**
   * @param pages the pages the citation is from
   */
  public void setPages(String pages) {
    this.pages = pages;
  }

  /**
   * The journal name. Abbreviations are provided for many journals; see the Local Guide.
   *
   * @return journal the source of the citation
   */
  public String getJournal() {
    return journal;
  }

  /**
   * @param journal the journal of the citation
   */
  public void setJournal(String journal) {
    this.journal = journal;
  }

  /**
   * A note is any additional information that can help the reader. The first word should
   * be capitalized.
   *
   * @return the note associated with this citation
   */
  public String getNote() {
    return note;
  }

  /**
   * @param note the note for this citation
   */
  public void setNote(String note) {
    this.note = note;
  }

  /**
   * @return the editors of this citation
   */
  public List<UserProfile> getEditors() {
    return editors;
  }

  /**
   * @param editors the editors of this citation
   */
  public void setEditors(List<UserProfile> editors) {
    this.editors = editors;
  }

  /**
   * @return the authors of this citation
   */
  public List<UserProfile> getAuthors() {
    return authors;
  }

  /**
   * @param authors the authors for this citation
   */
  public void setAuthors(List<UserProfile> authors) {
    this.authors = authors;
  }

  /**
   * The WWW Universal Resource Locator that points to the item being
   * referenced. This often is used for technical reports to point to the ftp
   * or web site where the postscript source of the report is located.
   *
   * @return the URL for the object
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url the URL for the object
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Return the abstract/summary of the object.
   *
   * @return the abstract/summary of the object
   */
  public String getSummary() {
    return summary;
  }

  /**
   * Set the abstract/summary of the object
   *
   * @param summary the summary/abstract of the object
   */
  public void setSummary(String summary) {
    this.summary = summary;
  }

  /**
   * Set the citation type. Bibtex specifies different type of citations and
   * this field is intended to track that. Please note that the string passed
   * should be a valid URI.
   *
   * @param citationType the string representation of the URI for the type
   * @throws IllegalArgumentException if the string is not a valid URI.
   */
  public void setCitationType(String citationType) {
    assert URI.create(citationType) != null : "Invalid PLoS Citation Type" + citationType;
    this.citationType = citationType;
  }

  /**
   * Return the type of the citation. The returned string is an URI.
   *
   * @return the citation type as a string representation of a URI.
   */
  public String getCitationType() {
    return citationType;
  }

  /**
   * Utility method to return the ordered list of author names by recursing through the
   * author's UserProfile instances.
   *
   * This is so frequently desired in common usage, that it is provided here.
   * N.B. if the author UserProfile objects were not touched in the transaction (lazy
   * load), this routine may return inaccurate data.
   *
   * @return the ordered list of author names
   */
  public List<String> getAuthorsRealNames() {
    if (authors == null)
      return null;

    List<String> realNames = new ArrayList<String>();
    for (UserProfile profile: authors)
      realNames.add(profile.getRealName());

    return realNames;
  }
}
