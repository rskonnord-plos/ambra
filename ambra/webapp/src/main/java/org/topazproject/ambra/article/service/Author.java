/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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

package org.topazproject.ambra.article.service;

import java.io.Serializable;

/**
 * Simple class to represent an individual author for citation purposes
 * 
 * @author Stephen Cheng
 *
 */
public class Author implements Serializable {

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
