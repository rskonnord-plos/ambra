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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AuthorView {

  private final String givenNames;
  private final String surnames;
  private final String suffix;
  private final String onBehalfOf;
  private final boolean equalContrib;
  private final boolean deceased;
  private final boolean relatedFootnote;
  private final String corresponding;
  private final List<String> currentAddresses;
  private final List<String> affiliations;
  private final List<String> customFootnotes;

  private AuthorView(String givenNames,
                     String surnames,
                     String suffix,
                     String onBehalfOf,
                     boolean equalContrib,
                     boolean deceased,
                     boolean relatedFootnote,
                     String corresponding,
                     List<String> currentAddresses,
                     List<String> affiliations,
                     List<String> customFootnotes) {
    super();
    this.givenNames = givenNames;
    this.surnames = surnames;
    this.suffix = suffix;
    this.onBehalfOf = onBehalfOf;
    this.equalContrib = equalContrib;
    this.deceased = deceased;
    this.relatedFootnote = relatedFootnote;
    this.corresponding = corresponding;
    this.currentAddresses = (currentAddresses == null)
      ? Collections.<String>emptyList()
      : Collections.unmodifiableList(currentAddresses);
    this.affiliations = (affiliations == null)
      ? Collections.<String>emptyList()
      : Collections.unmodifiableList(affiliations);
    this.customFootnotes = (customFootnotes == null)
      ? Collections.<String>emptyList()
      : Collections.unmodifiableList(customFootnotes);
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

  public String getOnBehalfOf() {
    return onBehalfOf;
  }

  public boolean getEqualContrib() {
    return equalContrib;
  }

  public boolean getDeceased() {
    return deceased;
  }

  public boolean getRelatedFootnote() {
    return relatedFootnote;
  }

  public String getCorresponding() {
    return corresponding;
  }

  public List<String> getCurrentAddresses() {
    return currentAddresses;
  }

  public List<String> getAffiliations() {
    return affiliations;
  }

  public List<String> getCustomFootnotes() {
    return customFootnotes;
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

  /**
   * Build a comma-delimited list of names of equally contributing authors.
   *
   * @param authors a list of non-null authors
   * @return the list of equally contributing authors' names, as text
   */
  public static String buildContributingAuthorsList(List<? extends AuthorView> authors) {
    List<AuthorView> contributingAuthors = new ArrayList<AuthorView>(authors.size());
    for (AuthorView author : authors) {
      if (author.getEqualContrib()) {
        contributingAuthors.add(author);
      }
    }
    return buildNameList(contributingAuthors);
  }

  /**
   * Check whether any author in an iterable has any iterations. This method predicts whether {@link
   * org.ambraproject.action.article.FetchArticleTabsAction#getAuthorsByAffiliation()} will return an empty result.
   *
   * @param authors an iterable of non-null authors
   * @return {@code true} if any author in the iterable has an affiliation
   */
  public static boolean anyHasAffiliation(Iterable<? extends AuthorView> authors) {
    for (AuthorView author : authors) {
      if (CollectionUtils.isNotEmpty(author.getAffiliations())) {
        return true;
      }
    }
    return false;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Create a builder set to create a copy of the passed in view
   * @param av
   * @return
   */
  public static Builder builder(AuthorView av) {
    Builder builder = new Builder();

    builder.setGivenNames(av.getGivenNames());
    builder.setSurnames(av.getSurnames());
    builder.setSuffix(av.getSuffix());
    builder.setOnBehalfOf(av.getOnBehalfOf());
    builder.setEqualContrib(av.getEqualContrib());
    builder.setDeceased(av.getDeceased());
    builder.setCorresponding(av.getCorresponding());
    builder.setCurrentAddresses(av.getCurrentAddresses());
    builder.setAffiliations(av.getAffiliations());
    builder.setCustomFootnotes(av.getCustomFootnotes());

    return builder;
  }

  public static class Builder {
    private Builder() {
      super();
    }

    private String givenNames;
    private String surnames;
    private String suffix;
    private String onBehalfOf;
    private boolean equalContrib;
    private boolean deceased;
    private boolean relatedFootnote;
    private String corresponding;
    private List<String> currentAddresses;
    private List<String> affiliations;
    private List<String> customFootnotes;

    public Builder setGivenNames(String givenNames) {
      this.givenNames = givenNames;
      return this;
    }

    public Builder setSurnames(String surnames) {
      this.surnames = surnames;
      return this;
    }

    public Builder setSuffix(String suffix) {
      this.suffix = suffix;
      return this;
    }

    public Builder setOnBehalfOf(String onBehalfOf) {
      this.onBehalfOf = onBehalfOf;
      return this;
    }

    public Builder setEqualContrib(boolean equalContrib) {
      this.equalContrib = equalContrib;
      return this;
    }

    public Builder setDeceased(boolean deceased) {
      this.deceased = deceased;
      return this;
    }

    public Builder setRelatedFootnote(boolean relatedFootnote) {
      this.relatedFootnote = relatedFootnote;
      return this;
    }

    public Builder setCorresponding(String corresponding) {
      this.corresponding = corresponding;
      return this;
    }

    public Builder setCurrentAddresses(List<String> currentAddresses) {
      this.currentAddresses = currentAddresses;
      return this;
    }

    public Builder setAffiliations(List<String> affiliations) {
      this.affiliations = affiliations;
      return this;
    }

    public Builder setCustomFootnotes(List<String> customFootnotes) {
      this.customFootnotes = customFootnotes;
      return this;
    }

    public AuthorView build() {
      return new AuthorView(
        givenNames,
        surnames,
        suffix,
        onBehalfOf,
        equalContrib,
        deceased,
        relatedFootnote,
        corresponding,
        currentAddresses,
        affiliations,
        customFootnotes);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null) return false;
    if (obj.getClass() != getClass()) return false;

    AuthorView that = (AuthorView) obj;
    return (this.givenNames == null ? that.givenNames == null : this.givenNames.equals(that.givenNames))
      && (this.surnames == null ? that.surnames == null : this.surnames.equals(that.surnames))
      && (this.suffix == null ? that.suffix == null : this.suffix.equals(that.suffix))
      && (this.onBehalfOf == null ? that.onBehalfOf == null : this.onBehalfOf.equals(that.onBehalfOf))
      && (this.equalContrib == that.equalContrib)
      && (this.deceased == that.deceased)
      && (this.relatedFootnote == that.relatedFootnote)
      && (this.corresponding == null ? that.corresponding == null : this.corresponding.equals(that.corresponding))
      && (this.currentAddresses == null ? that.currentAddresses == null : this.currentAddresses.equals(that.currentAddresses))
      && (this.affiliations == null ? that.affiliations == null : this.affiliations.equals(that.affiliations))
      && (this.customFootnotes == null ? that.customFootnotes == null : this.customFootnotes.equals(that.customFootnotes));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = 1;
    hash = prime * hash + (givenNames == null ? 0 : givenNames.hashCode());
    hash = prime * hash + (surnames == null ? 0 : surnames.hashCode());
    hash = prime * hash + (suffix == null ? 0 : suffix.hashCode());
    hash = prime * hash + (onBehalfOf == null ? 0 : onBehalfOf.hashCode());
    hash = prime * hash + Boolean.valueOf(equalContrib).hashCode();
    hash = prime * hash + Boolean.valueOf(deceased).hashCode();
    hash = prime * hash + Boolean.valueOf(relatedFootnote).hashCode();
    hash = prime * hash + (corresponding == null ? 0 : corresponding.hashCode());
    hash = prime * hash + (currentAddresses == null ? 0 : currentAddresses.hashCode());
    hash = prime * hash + (affiliations == null ? 0 : affiliations.hashCode());
    hash = prime * hash + (customFootnotes == null ? 0 : customFootnotes.hashCode());
    return hash;
  }
}
