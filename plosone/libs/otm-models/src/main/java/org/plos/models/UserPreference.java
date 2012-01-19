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

import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

/** 
 * This defines a single user preference.
*
 * @author Ronald Tschalär
 */
@Entity(model = "preferences")
public class UserPreference {
  @Id @GeneratedValue(uriPrefix = "info:doi/10.1371/preferences/")
  private URI    id;
  /** The name of the preference. */
  @Predicate(uri = Rdf.topaz + "prefName")
  private String name;
  /** The values of the preference. */
  @Predicate(uri = Rdf.topaz + "prefValue")
  private String[] values;

  /** 
   * Create an empty user-preference. 
   */
  public UserPreference() {
  }

  /** 
   * Create a new user-preference with the given values. 
   *
   * @param name   the name of the preference
   * @param values the values of the preference 
   */
  public UserPreference(String name, String[] values) {
    this.name   = name;
    this.values = values;
  }

  /**
   * Get the id.
   *
   * @return the id.
   */
  public URI getId() {
    return id;
  }

  /**
   * Set the id.
   *
   * @param id the id.
   */
  public void setId(URI id) {
    this.id = id;
  }

  /**
   * Get the name of the preference.
   *
   * @return the preference name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the preference.
   *
   * @param name the preference name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the values of the preference.
   *
   * @return the preference values; may be null. Note that the order of the entries will be
   *         arbitrary.
   */
  public String[] getValues() {
    return values;
  }

  /**
   * Set the values of the preference.
   *
   * @param values the preference values; may be null. Note that the order will not be preserved.
   */
  public void setValues(String[] values) {
    this.values = values;
  }
}
