/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.article.service;

/**
 * Simple class to represent an individual author for citation purposes
 * 
 * @author Stephen Cheng
 *
 */
public class Author {

  private String givenNames;
  private String surname;
  private String suffix;
  private boolean isPrimary;

  /**
   * 
   * @param given
   * @param sur
   * @param suf
   */
  public Author(String given, String sur, String suf, boolean isP) {
    givenNames = given;
    surname = sur;
    suffix = suf;
    isPrimary = isP;
  }

  /**
   * 
   *
   */
  public Author (){
  }

  /**
   * @return Returns the givenName.
   */
  public String getGivenNames() {
    return givenNames;
  }

  /**
   * @param givenName The givenName to set.
   */
  public void setGivenNames(String givenName) {
    this.givenNames = givenName;
  }


  /**
   * @return Returns the surname.
   */
  public String getSurname() {
    return surname;
  }

  /**
   * @param surname The surname to set.
   */
  public void setSurname(String surname) {
    this.surname = surname;
  }

  /**
   * @return Returns the suffix.
   */
  public String getSuffix() {
    return suffix;
  }

  /**
   * @param suffix The suffix to set.
   */
  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  /**
   * @return Returns the isPrimary.
   */
  public boolean getIsPrimary() {
    return isPrimary;
  }

  /**
   * @param isPrimary The isPrimary to set.
   */
  public void setIsPrimary(boolean isPrimary) {
    this.isPrimary = isPrimary;
  }
}
