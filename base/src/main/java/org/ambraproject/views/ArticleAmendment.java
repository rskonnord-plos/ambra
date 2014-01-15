/*
 * Copyright (c) 2006-2014 by Public Library of Science
 *
 *    http://plos.org
 *    http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.views;

/**
 * View object for article amendments; currently, used for sending amendments to solr for indexing
 */
public class ArticleAmendment {
  private final String parentArticleURI;
  private final String otherArticleDoi;
  private final String relationshipType;

  private ArticleAmendment(String parentArticleURL, String otherArticleDoi, String relationshipType) {
    this.parentArticleURI = parentArticleURL;
    this.otherArticleDoi = otherArticleDoi;
    this.relationshipType = relationshipType;
  }

  public String getParentArticleURI() {
    return parentArticleURI;
  }

  public String getOtherArticleDoi() {
    return otherArticleDoi;
  }

  public String getRelationshipType() {
    return relationshipType;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder  {

    private String parentArticleURI;
    private String otherArticleDoi;
    private String relationshipType;

    private Builder() {
      super();
    }

    public Builder setParentArticleURI(String parentArticleURI) {
      this.parentArticleURI = parentArticleURI;
      return this;
    }

    public Builder setOtherArticleDoi(String otherArticleDoi) {
      this.otherArticleDoi = otherArticleDoi;
      return this;
    }

    public Builder setRelationshipType(String relationshipType) {
      this.relationshipType = relationshipType;
      return this;
    }

    public ArticleAmendment build() {
      return new ArticleAmendment(parentArticleURI, otherArticleDoi, relationshipType);
    }
  }
}
