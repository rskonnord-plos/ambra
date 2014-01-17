/*
 * Copyright (c) 2007-2014 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.views;

/**
 * Java class for holding ORCiD auth requests.
 *
 * Example response:
    {
      "access_token": "8056b6d5-f96d-42c8-8df9-41f70908ad33",
      "token_type": "bearer",
      "refresh_token": "d8c72880-00ac-4447-a2af-e90f1673f4bc",
      "expires_in": 631138286,
      "scope": "/orcid-profile/read-limited",
      "orcid": "0000-0003-4954-7894"
  }
 */
public class OrcidAuthorization {
  private final String accessToken;
  private final String tokenType;
  private final String refreshToken;
  private final long expiresIn;
  private final String scope;
  private final String orcid;

  private OrcidAuthorization(Builder builder) {
    this.accessToken = builder.accessToken;
    this.tokenType = builder.tokenType;
    this.refreshToken = builder.refreshToken;
    this.expiresIn = builder.expiresIn;
    this.scope = builder.scope;
    this.orcid = builder.orcid;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public String getScope() {
    return scope;
  }

  public String getOrcid() {
    return orcid;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Builder() {
      super();
    }

    private String accessToken;
    private String tokenType;
    private String refreshToken;
    private long expiresIn;
    private String scope;
    private String orcid;

    public Builder setAccessToken(String accessToken) {
      this.accessToken = accessToken;
      return this;
    }

    public Builder setTokenType(String tokenType) {
      this.tokenType = tokenType;
      return this;
    }

    public Builder setRefreshToken(String refreshToken) {
      this.refreshToken = refreshToken;
      return this;
    }

    public Builder setExpiresIn(long expiresIn) {
      this.expiresIn = expiresIn;
      return this;
    }

    public Builder setScope(String scope) {
      this.scope = scope;
      return this;
    }

    public Builder setOrcid(String orcid) {
      this.orcid = orcid;
      return this;
    }

    public OrcidAuthorization build() {
      return new OrcidAuthorization(this);
    }
  }
}
