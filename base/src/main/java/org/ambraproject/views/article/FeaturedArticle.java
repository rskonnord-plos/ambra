/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.views.article;

/**
 * Simple view for featured articles.
 *
 */
public class FeaturedArticle {
  private final String doi;
  private final String title;
  private final String StrkImgURI;
  private final String type;

  private FeaturedArticle(String doi, String title, String strkImgURI, String type) {
    this.doi = doi;
    this.title = title;
    StrkImgURI = strkImgURI;
    this.type = type;
  }

  public String getDoi() {
    return doi;
  }

  public String getTitle() {
    return title;
  }

  public String getStrkImgURI() {
    return StrkImgURI;
  }

  public String getType() {
    return type;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String doi;
    private String title;
    private String strkImgURI;
    private String type;

    private Builder() {
      super();
    }

    public Builder setDoi(String doi) {
      this.doi = doi;
      return this;
    }

    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder setStrkImgURI(String strkImgURI) {
      this.strkImgURI = strkImgURI;
      return this;
    }

    public Builder setType(String type) {
      this.type = type;
      return this;
    }

    public FeaturedArticle build() {
      return new FeaturedArticle(doi, title, strkImgURI, type);
    }
  }
}
