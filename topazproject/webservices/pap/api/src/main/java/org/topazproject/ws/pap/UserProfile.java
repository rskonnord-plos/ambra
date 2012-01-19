/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap;

/**
 * This defines a user's profile. It is modeled on <a href="http://xmlns.com/foaf/0.1/">foaf</a>
 * and <a href="http://vocab.org/bio/0.1/">bio</a>, consisting of a subset of the
 * <var>foaf:Person</var> and <var>bio:*</var> plus a number topaz specific additions.
 *
 * <p>Schema's borrowed from:
 * <dl>
 *   <dt>foaf
 *   <dd><a href="http://xmlns.com/foaf/0.1/">http://xmlns.com/foaf/0.1/</a></dd>
 *   <dt>bio
 *   <dd><a href="http://vocab.org/bio/0.1/">http://vocab.org/bio/0.1/</a></dd>
 *   <dt>address
 *   <dd><a href="http://rdfweb.org/topic/AddressVocab">http://rdfweb.org/topic/AddressVocab</a></dd>
 * </dl>
 *
 * <p>This class is a bean.
 *
 * @author Ronald Tschalär
 */
public class UserProfile {
  /** The name to use for display; stored in topaz:displayName; subPropertyOf foaf:nick */
  private String displayName;
  /** Their real name, usually as &lt;first&gt; &lt;last&gt;; stored in foaf:name */
  private String realName;
  /** Their given names; stored in foaf:givenname */
  private String givenNames;
  /** Their surnames; stored in foaf:surname */
  private String surnames;
  /** The title by which they go; stored in foaf:title */
  private String title;
  /** 'male' or 'female'; stored in foaf:gender */
  private String gender;
  /** Their organizational position type; stored in topaz:positionType. */
  private String positionType;
  /** Their organization name; stored in topaz:organizationName. */
  private String organizationName;
  /** Their organization type; stored in topaz:organizationType. */
  private String organizationType;
  /** postal address; stored in topaz:postalAddress */
  private String postalAddress;
  /** city; stored in addr:town */
  private String city;
  /** country; stored in addr:country */
  private String country;
  /** email address; stored in foaf:mbox */
  private String email;
  /** url of their homepage; stored in foaf:homepage */
  private String homePage;
  /** url of their blog; stored in foaf:weblog */
  private String weblog;
  /** url pointing to their biography; stored in bio:olb */
  private String biography;
  /** a list of urls pointing to stuff representing their interests; foaf:interest */
  private String[] interests;
  /** url pointing to a webpage listing their publications; foaf:publications */
  private String publications;
  /** text containing their biography; stored in topaz:bio */
  private String biographyText;
  /** text containing their interests; stored in topaz:interests */
  private String interestsText;
  /** text containing their research areas; stored in topaz:researchAreas */
  private String researchAreasText;

  /**
   * Get the name to use for display on the site.
   *
   * @return the display name, or null
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Set the name to use for display on the site.
   *
   * @param displayName the display name; may be null
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
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
   * Get the organizational position type.
   *
   * @return the position type.
   */
  public String getPositionType() {
    return positionType;
  }

  /**
   * Set the organizational position type.
   *
   * @param positionType the position type.
   */
  public void setPositionType(String positionType) {
    this.positionType = positionType;
  }

  /**
   * Get the organization name.
   *
   * @return the organization name.
   */
  public String getOrganizationName() {
    return organizationName;
  }

  /**
   * Set the organization name.
   *
   * @param organizationName the organization name.
   */
  public void setOrganizationName(String organizationName) {
    this.organizationName = organizationName;
  }

  /**
   * Get the organization type.
   *
   * @return the organization type.
   */
  public String getOrganizationType() {
    return organizationType;
  }

  /**
   * Set the organization type.
   *
   * @param organizationType the organization type.
   */
  public void setOrganizationType(String organizationType) {
    this.organizationType = organizationType;
  }

  /**
   * Get the postal address.
   *
   * @return the postal address.
   */
  public String getPostalAddress() {
    return postalAddress;
  }

  /**
   * Set the postal address.
   *
   * @param postalAddress the postal address.
   */
  public void setPostalAddress(String postalAddress) {
    this.postalAddress = postalAddress;
  }

  /**
   * Get the city.
   *
   * @return the city.
   */
  public String getCity() {
    return city;
  }

  /**
   * Set the city.
   *
   * @param city the city.
   */
  public void setCity(String city) {
    this.city = city;
  }

  /**
   * Get the country.
   *
   * @return the country.
   */
  public String getCountry() {
    return country;
  }

  /**
   * Set the country.
   *
   * @param country the country.
   */
  public void setCountry(String country) {
    this.country = country;
  }

  /**
   * Get the email address.
   *
   * @return the email, or null
   */
  public String getEmail() {
    return email;
  }

  /**
   * Set the email address.
   *
   * @param email the email; may be null
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Get the url of the user's homepage.
   *
   * @return the homepage url, or null
   */
  public String getHomePage() {
    return homePage;
  }

  /**
   * Set the url of the user's homepage.
   *
   * @param homePage the homepage url; may be null
   */
  public void setHomePage(String homePage) {
    this.homePage = homePage;
  }

  /**
   * Get the url of the user's blog.
   *
   * @return the weblog url, or null
   */
  public String getWeblog() {
    return weblog;
  }

  /**
   * Set the url of the user's blog.
   *
   * @param weblog the weblog url; may be null
   */
  public void setWeblog(String weblog) {
    this.weblog = weblog;
  }

  /**
   * Get the url of the user's biography.
   *
   * @return the biography url, or null
   */
  public String getBiography() {
    return biography;
  }

  /**
   * Set the url of the user's biography.
   *
   * @param biography the biography url; may be null
   */
  public void setBiography(String biography) {
    this.biography = biography;
  }

  /**
   * Get a list of url's, usually of webpages, representing the user's interests.
   *
   * @return the list of url's, or null. Note that the order of the entries will be arbitrary.
   */
  public String[] getInterests() {
    return interests;
  }

  /**
   * Set a list of url's, usually of webpages, representing the user's interests.
   *
   * @param interests the list of url's; may be null. Note that the order will not be preserved.
   */
  public void setInterests(String[] interests) {
    this.interests = interests;
  }

  /**
   * Get the url of the page listing the user's publications.
   *
   * @return a url, or null
   */
  public String getPublications() {
    return publications;
  }

  /**
   * Set the url of the page listing the user's publications.
   *
   * @param publications the url; may be null
   */
  public void setPublications(String publications) {
    this.publications = publications;
  }

  /**
   * Get the text biography.
   *
   * @return the biographyText.
   */
  public String getBiographyText() {
    return biographyText;
  }

  /**
   * Set the text description of the biography.
   *
   * @param biographyText the text description of the biography.
   */
  public void setBiographyText(String biographyText) {
    this.biographyText = biographyText;
  }

  /**
   * Get the text description of the interests.
   *
   * @return the text description of the interests.
   */
  public String getInterestsText() {
    return interestsText;
  }

  /**
   * Set the text description of the interests.
   *
   * @param interestsText the text description of the interests.
   */
  public void setInterestsText(String interestsText) {
    this.interestsText = interestsText;
  }

  /**
   * Get the text description of the researchAreas.
   *
   * @return the text description of the research areas.
   */
  public String getResearchAreasText() {
    return researchAreasText;
  }

  /**
   * Set the text description of the researchAreas.
   *
   * @param researchAreasText the text description of the research areas.
   */
  public void setResearchAreasText(String researchAreasText) {
    this.researchAreasText = researchAreasText;
  }
}
