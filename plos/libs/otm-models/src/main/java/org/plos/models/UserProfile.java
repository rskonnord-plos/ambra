/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.models;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * This defines a user's profile in PLoS-ONE. It is modeled on <a
 * href="http://xmlns.com/foaf/0.1/">foaf</a> and <a href="http://vocab.org/bio/0.1/">bio</a>,
 * consisting of a subset of the <var>foaf:Person</var> and <var>bio:*</var> plus a number topaz
 * specific additions.
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
 * @author Ronald Tschalär
 */
@UriPrefix(Rdf.topaz)
@Entity(model = "profiles")
public class UserProfile extends FoafPerson {
  private static final String BIO_URI  = "http://purl.org/vocab/bio/0.1/";
  private static final String ADDR_URI = "http://wymiwyg.org/ontologies/foaf/postaddress#";

  /** The name to use for display; stored in topaz:displayName; subPropertyOf foaf:nick */
  private String displayName;

  /** Their organizational position type */
  private String positionType;

  /** Their organization name */
  private String organizationName;

  /** Their organization type */
  private String organizationType;

  /** postal address */
  private String postalAddress;

  /** city */
  @Predicate(uri = ADDR_URI + "town")
  private String city;

  /** country */
  @Predicate(uri = ADDR_URI + "country")
  private String country;

  /** url pointing to their biography */
  @Predicate(uri = BIO_URI + "olb")
  private String biography;

  /** text containing their biography */
  @Predicate(uri = Rdf.topaz + "bio")
  private String biographyText;

  /** text containing their interests */
  @Predicate(uri = Rdf.topaz + "interests")
  private String interestsText;

  /** text containing their research areas */
  @Predicate(uri = Rdf.topaz + "researchAreas")
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
