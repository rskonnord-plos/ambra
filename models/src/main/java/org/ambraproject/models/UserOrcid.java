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
package org.ambraproject.models;

import java.util.Calendar;

/**
 * POJO for the userOrcid table
 */
public class UserOrcid extends AmbraEntity {
  private String orcid;
  private String accessToken;
  private String refreshToken;
  private String tokenScope;
  private Calendar tokenExpires;

  public String getOrcid() {
    return orcid;
  }

  public void setOrcid(String orcid) {
    this.orcid = orcid;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getTokenScope() {
    return tokenScope;
  }

  public void setTokenScope(String tokenScope) {
    this.tokenScope = tokenScope;
  }

  public Calendar getTokenExpires() {
    return tokenExpires;
  }

  public void setTokenExpires(Calendar tokenExpires) {
    this.tokenExpires = tokenExpires;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserOrcid)) return false;

    UserOrcid userOrcid = (UserOrcid) o;

    if (!accessToken.equals(userOrcid.accessToken)) return false;
    if (!orcid.equals(userOrcid.orcid)) return false;
    if (!refreshToken.equals(userOrcid.refreshToken)) return false;
    if (!tokenExpires.equals(userOrcid.tokenExpires)) return false;
    if (!tokenScope.equals(userOrcid.tokenScope)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = orcid.hashCode();
    result = 31 * result + accessToken.hashCode();
    result = 31 * result + refreshToken.hashCode();
    result = 31 * result + tokenScope.hashCode();
    result = 31 * result + tokenExpires.hashCode();
    return result;
  }
}
