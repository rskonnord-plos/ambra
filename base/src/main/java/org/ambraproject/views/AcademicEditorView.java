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

public class AcademicEditorView {

  private final String id;
  private final String name;
  private final String lastName;
  private final String institute;
  private final String country;
  private final List<String> subjects;
  private final String type;
  private final String journalKey;

  private AcademicEditorView(String id,
                             String name,
                             String lastName,
                             String institute,
                             String country,
                             List<String> subjects,
                             String type,
                             String journalKey) {
    super();

    if(subjects != null) {
      Collections.sort(subjects);
    }

    this.id = id;
    this.name = name;
    this.lastName = lastName;
    this.institute = institute;
    this.country = country;
    this.subjects = (subjects == null)
      ? Collections.<String>emptyList()
      : Collections.unmodifiableList(subjects);
    this.type = type;
    this.journalKey = journalKey;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getLastName() {
    return lastName;
  }

  public String getInstitute() {
    return institute;
  }

  public String getCountry() {
    return country;
  }

  public List<String> getSubjects() {
    return subjects;
  }

  public String getType() {
    return type;
  }

  public String getJournalKey() {
    return journalKey;
  }


  public static Builder builder() {
    return new Builder();
  }

  /**
   * Create a builder set to create a copy of the passed in view
   * @param av
   * @return
   */
  public static Builder builder(AcademicEditorView av) {
    Builder builder = new Builder();

    builder.setId(av.getId());
    builder.setName(av.getName());
    builder.setLastName(av.getLastName());
    builder.setInstitute(av.getInstitute());
    builder.setCountry(av.getCountry());
    builder.setSubjects(av.getSubjects());
    builder.setType(av.getType());
    builder.setJournalKey(av.getJournalKey());

    return builder;
  }

  public static class Builder {
    private Builder() {
      super();
    }

    private String id;
    private String name;
    private String lastName;
    private String institute;
    private String country;
    private List<String> subjects;
    private String type;
    private String journalKey;

    public Builder setId(String id) {
      this.id = id;
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setLastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public Builder setInstitute(String institute) {
      this.institute = institute;
      return this;
    }

    public Builder setCountry(String country) {
      this.country = country;
      return this;
    }

    public Builder setSubjects(List<String> subjects) {
      this.subjects = subjects;
      return this;
    }

    public Builder setType(String type) {
      this.type = type;
      return this;
    }

    public Builder setJournalKey(String journalKey) {
      this.journalKey = journalKey;
      return this;
    }

    public AcademicEditorView build() {
      return new AcademicEditorView(
        id,
        name,
        lastName,
        institute,
        country,
        subjects,
        type,
        journalKey);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AcademicEditorView that = (AcademicEditorView) o;

    if (country != null ? !country.equals(that.country) : that.country != null) return false;
    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (institute != null ? !institute.equals(that.institute) : that.institute != null) return false;
    if (journalKey != null ? !journalKey.equals(that.journalKey) : that.journalKey != null) return false;
    if (lastName != null ? !lastName.equals(that.lastName) : that.lastName != null) return false;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    if (!subjects.equals(that.subjects)) return false;
    if (type != null ? !type.equals(that.type) : that.type != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
    result = 31 * result + (institute != null ? institute.hashCode() : 0);
    result = 31 * result + (country != null ? country.hashCode() : 0);
    result = 31 * result + subjects.hashCode();
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (journalKey != null ? journalKey.hashCode() : 0);
    return result;
  }
}
