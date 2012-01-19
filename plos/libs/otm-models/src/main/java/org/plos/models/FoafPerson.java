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
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * A person according to foaf. This is not complete, but just the subset used by PLoS-ONE.
 *
 * @author Ronald Tschalär
 */
@UriPrefix(Rdf.foaf)
@Entity(type = Rdf.foaf + "Person")
public class FoafPerson {
  @Id @GeneratedValue(uriPrefix = "info:doi/10.1371/profile/")
  private URI      id;

  /** Their real name, usually as &lt;first&gt; &lt;last&gt; */
  @Predicate(uri = Rdf.foaf + "name")
  private String   realName;

  /** Their given names */
  @Predicate(uri = Rdf.foaf + "givenname")
  private String   givenNames;

  /** Their surnames */
  @Predicate(uri = Rdf.foaf + "surname")
  private String   surnames;

  /** The title by which they go */
  private String   title;

  /** 'male' or 'female' */
  private String   gender;

  /** email address */
  @Predicate(uri = Rdf.foaf + "mbox")
  private URI      email;

  /** url of their homepage */
  @Predicate(uri = Rdf.foaf + "homepage")
  private URI      homePage;

  /** url of their blog */
  private URI      weblog;

  /** url pointing to a webpage listing their publications; foaf:publications */
  private URI      publications;

  /** a list of urls pointing to stuff representing their interests */
  @Predicate(uri = Rdf.foaf + "interest")
  private Set<URI> interests = new HashSet<URI>();

  /**
   * @return the id
   */
  public URI getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(URI id) {
    this.id = id;
  }

  /**
   * Get the real name, usually as &lt;first&gt;, &lt;last&gt;.
   *
   * @return real name, or null
   */
  public String getRealName() {
    return realName;
  }

  /**
   * Set the real name, usually as &lt;first&gt;, &lt;last&gt;.
   *
   * @param realName the real name; may be null
   */
  public void setRealName(String realName) {
    this.realName = realName;
  }

  /**
   * Get the given names.
   *
   * @return the given names.
   */
  public String getGivenNames() {
    return givenNames;
  }

  /**
   * Set the given names.
   *
   * @param givenNames the given names.
   */
  public void setGivenNames(String givenNames) {
    this.givenNames = givenNames;
  }

  /**
   * Get the surnames.
   *
   * @return the surnames.
   */
  public String getSurnames() {
    return surnames;
  }

  /**
   * Set the surnames.
   *
   * @param surnames the surnames.
   */
  public void setSurnames(String surnames) {
    this.surnames = surnames;
  }

  /**
   * Get the title (e.g. 'Mrs', 'Dr', etc).
   *
   * @return the title, or null
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set the title (e.g. 'Mrs', 'Dr', etc).
   *
   * @param title the title; may be null
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Get the gender. Valid values are 'male' and 'female'.
   *
   * @return the gender, or null
   */
  public String getGender() {
    return gender;
  }

  /**
   * Set the gender. Valid values are 'male' and 'female'.
   *
   * @param gender the gender; may be null
   */
  public void setGender(String gender) {
    this.gender = gender;
  }

  /**
   * Get the email address.
   *
   * @return the email address.
   */
  public URI getEmail() {
    return email;
  }

  /**
   * Set the email address.
   *
   * @param email the email address.
   */
  public void setEmail(URI email) {
    this.email = email;
  }

  /**
   * Get the raw email address.
   *
   * @return the email address.
   */
  public String getEmailAsString() {
    return (email != null) ? email.getSchemeSpecificPart() : null;
  }

  /**
   * Set the raw email address.
   *
   * @param email the email address.
   */
  public void setEmailFromString(String email) {
    try {
      this.email = new URI("mailto", email, null);
    } catch (URISyntaxException use) {
      throw new RuntimeException("Unexpected exception creating mailto url from '" + email + "'",
                                 use);
    }
  }

  /**
   * Get the url of the user's homepage.
   *
   * @return the url of the homepage, or null.
   */
  public URI getHomePage() {
    return homePage;
  }

  /**
   * Set the url of the user's homepage.
   *
   * @param homePage the url of the homepage; may be null.
   */
  public void setHomePage(URI homePage) {
    this.homePage = homePage;
  }

  /**
   * Get the url of the user's blog.
   *
   * @return the url of the blog, or null.
   */
  public URI getWeblog() {
    return weblog;
  }

  /**
   * Set the url of the user's blog.
   *
   * @param weblog the url of the blog; may be null.
   */
  public void setWeblog(URI weblog) {
    this.weblog = weblog;
  }

  /**
   * Get the url of the page listing the user's publications.
   *
   * @return the url, or null.
   */
  public URI getPublications() {
    return publications;
  }

  /**
   * Set the url of the page listing the user's publications.
   *
   * @param pubs the url; may be null.
   */
  public void setPublications(URI publications) {
    this.publications = publications;
  }

  /**
   * Get a list of url's, usually of webpages, representing the user's interests.
   *
   * @return the list of url's, or null.
   */
  public Set<URI> getInterests() {
    return interests;
  }

  /**
   * Set a list of url's, usually of webpages, representing the user's interests.
   *
   * @param interests the list of url's; may be null.
   */
  public void setInterests(Set<URI> interests) {
    this.interests = interests;
  }
}
