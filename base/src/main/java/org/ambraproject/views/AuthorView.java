/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.views;

import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.List;

/**
 * Immutable view wrapper around an author + a bunch of extra meta data about the author
 *
 * @author Alex Kudlick
 * @author Joe Osowski
 */
public class AuthorView {
  private final String givenNames;
  private final String surnames;
  private final String suffix;
  private final String currentAddress;
  private final boolean equalContrib;
  private final boolean deceased;
  private final String corresponding;
  private final List<String> affiliations;
  private final List<String> customFootnotes;

  public AuthorView(String givenNames, String surnames, String suffix,
                    String currentAddress, boolean equalContrib, boolean deceased,
                    String corresponding, List<String>affiliations,
                    List<String> customFootnotes) {

    this.givenNames = givenNames;
    this.surnames = surnames;
    this.suffix = suffix;
    this.currentAddress = currentAddress;
    this.equalContrib = equalContrib;
    this.deceased = deceased;
    this.corresponding = corresponding;
    this.affiliations = affiliations;
    this.customFootnotes = customFootnotes;
  }

  public String getGivenNames() {
    return givenNames;
  }

  public String getSurnames() {
    return surnames;
  }

  public String getSuffix() {
    return suffix;
  }

  public List<String> getAffiliations() {
    return this.affiliations;
  }

  public boolean getEqualContrib() {
    return this.equalContrib;
  }

  public String getCurrentAddress() {
    return this.currentAddress;
  }

  public boolean getDeceased() {
    return this.deceased;
  }

  public String getCorresponding()
  {
    return this.corresponding;
  }

  public List<String> getCustomFootnotes()
  {
    return this.customFootnotes;
  }

  public String getFullName() {
    StringBuilder sb = new StringBuilder();

    if(!StringUtils.isEmpty(this.givenNames)) {
      sb.append(this.givenNames);
    }

    if(!StringUtils.isEmpty(this.surnames)) {
      if(sb.length() > 0) {
        sb.append(" ");
      }
      sb.append(this.surnames);
    }

    if(!StringUtils.isEmpty(this.suffix)) {
      if(sb.length() > 0) {
        sb.append(" ");
      }
      sb.append(this.suffix);
    }

    return sb.toString();
  }

  /**
   * Build a comma-delimited list of author names.
   *
   * @param authors a list of non-null authors
   * @return the list of author names, as text
   */
  public static String buildNameList(List<? extends AuthorView> authors) {
    Iterator<? extends AuthorView> iterator = authors.iterator();
    if (!iterator.hasNext()) {
      return "";
    }
    StringBuilder textList = new StringBuilder();
    textList.append(iterator.next().getFullName());
    while (iterator.hasNext()) {
      textList.append(", ").append(iterator.next().getFullName());
    }
    return textList.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AuthorView that = (AuthorView) o;

    if (deceased != that.deceased) return false;
    if (affiliations != null ? !affiliations.equals(that.affiliations) : that.affiliations != null) return false;
    if (corresponding != null ? !corresponding.equals(that.corresponding) : that.corresponding != null) return false;
    if (currentAddress != null ? !currentAddress.equals(that.currentAddress) : that.currentAddress != null) return false;
    if (customFootnotes != null ? !customFootnotes.equals(that.customFootnotes) : that.customFootnotes != null) return false;
    if (equalContrib != that.equalContrib) return false;
    if (givenNames != null ? !givenNames.equals(that.givenNames) : that.givenNames != null) return false;
    if (suffix != null ? !suffix.equals(that.suffix) : that.suffix != null) return false;
    if (surnames != null ? !surnames.equals(that.surnames) : that.surnames != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = givenNames != null ? givenNames.hashCode() : 0;
    result = 31 * result + (surnames != null ? surnames.hashCode() : 0);
    result = 31 * result + (suffix != null ? suffix.hashCode() : 0);
    result = 31 * result + (equalContrib ? 1 : 0);
    result = 31 * result + (affiliations != null ? affiliations.hashCode() : 0);
    result = 31 * result + (currentAddress != null ? currentAddress.hashCode() : 0);
    result = 31 * result + (deceased ? 1 : 0);
    result = 31 * result + (corresponding != null ? corresponding.hashCode() : 0);
    result = 31 * result + (customFootnotes != null ? customFootnotes.hashCode() : 0);
    return result;
  }
}