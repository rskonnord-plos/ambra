/*
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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

/**
 * Value object that holds the result of a single search item
 *
 */
public class SavedSearchHit implements Serializable {

  private static final long serialVersionUID = 2450207404766765639L;

  private final String uri;
  private final String title;
  private final String creator;
  private final Collection<String> subjectsPolyhierarchy;
  private final Collection<String> subjects;

  /**
   * Create a search hit with the values set
   *
   * @param uri Article ID
   * @param title Article title
   * @param creators Creators
   * @param subjects Subjects
   */
  private SavedSearchHit(String uri, String title, String creators, Collection<String> subjects, Collection<String> subjectsPolyhierarchy) {
    this.uri  = uri;
    this.title = title;
    this.creator = creators;
    this.subjects = subjects;
    this.subjectsPolyhierarchy = subjectsPolyhierarchy;
  }

  /**
   * @return the hit object's uri
   */
  public String getUri() {
    return uri;
  }

  /**
   * Getter for property 'creator'.
   * @return Value for property 'creator'.
   */
  public String getCreator() {
    return creator;
  }

   /**
   * Getter for property 'title'.
   * @return Value for property 'title'.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Getter for property 'subjects'.
   * @return Value for property 'subjects'.
   */
  public Collection<String> getSubjects() {
    return subjects;
  }

  /**
   * Getter for property 'subjectsPolyhierarchy'.
   * @return Value for property 'subjectsPolyhierarchy'.
   */
  public Collection<String> getSubjectsPolyhierarchy() {
    return subjectsPolyhierarchy;
  }

  @Override
  public String toString() {
    return "SearchHit{" +
        ", uri='" + uri + '\'' +
        ", title='" + title + '\'' +
        ", creator='" + creator + '\'' +
        ", subjects='" + (subjects == null ? null : Arrays.asList(subjects)) + "'" +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SavedSearchHit searchHit = (SavedSearchHit) o;

    return uri.equals(searchHit.uri);
  }

  @Override
  public int hashCode() {
    return uri.hashCode();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String uri;
    private String title;
    private String creator;
    private Collection<String> subjectsPolyhierarchy;
    private Collection<String> subjects;

    private Builder() {
      super();
    }

    public Builder setUri(String uri) {
      this.uri = uri;
      return this;
    }

    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder setCreator(String creator) {
      this.creator = creator;
      return this;
    }

    public Builder setSubjectsPolyhierarchy(Collection<String> subjectsPolyhierarchy) {
      this.subjectsPolyhierarchy = subjectsPolyhierarchy;
      return this;
    }

    public Builder setSubjects(Collection<String> subjects) {
      this.subjects = subjects;
      return this;
    }

    public SavedSearchHit build() {
      return new SavedSearchHit(
        uri,
        title,
        creator,
        subjects,
        subjectsPolyhierarchy
      );
    }
  }
}