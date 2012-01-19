/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.plos.user.action;

import org.plos.user.PlosOneUser;
import org.plos.util.TextUtils;

/**
 * A wrapper around PlosOneUser to be used to render PlosOneUser attributes as non-malicious chars
 */
public class PlosOneUserDecorator extends PlosOneUser {
  private final PlosOneUser plosOneUser;

  public PlosOneUserDecorator(final PlosOneUser plosOneUser) {
    super(plosOneUser);
    this.plosOneUser = plosOneUser;
  }

  public String getBiography() {
    return getSafe(plosOneUser.getBiography());
  }

  public String getBiographyText() {
    return getSafe(plosOneUser.getBiographyText());
  }

  public String getCity() {
    return getSafe(plosOneUser.getCity());
  }

  public String getCountry() {
    return getSafe(plosOneUser.getCountry());
  }

  public String getDisplayName() {
    return getSafe(plosOneUser.getDisplayName());
  }

  public String getEmail() {
    return getSafe(plosOneUser.getEmail());
  }

  public String getGender() {
    return getSafe(plosOneUser.getGender());
  }

  public String getGivenNames() {
    return getSafe(plosOneUser.getGivenNames());
  }

  public String getHomePage() {
    return getSafe(plosOneUser.getHomePage());
  }

  public String getInterestsText() {
    return getSafe(plosOneUser.getInterestsText());
  }

  public String getOrganizationName() {
    return getSafe(plosOneUser.getOrganizationName());
  }

  public String getOrganizationType() {
    return getSafe(plosOneUser.getOrganizationType());
  }

  public String getPositionType() {
    return getSafe(plosOneUser.getPositionType());
  }

  public String getPostalAddress() {
    return getSafe(plosOneUser.getPostalAddress());
  }

  public String getPublications() {
    return getSafe(plosOneUser.getPublications());
  }

  public String getRealName() {
    return getSafe(plosOneUser.getRealName());
  }

  public String getResearchAreasText() {
    return getSafe(plosOneUser.getResearchAreasText());
  }

  public String getSurnames() {
    return getSafe(plosOneUser.getSurnames());
  }

  public String getTitle() {
    return getSafe(plosOneUser.getTitle());
  }

  public String getWeblog() {
    return getSafe(plosOneUser.getWeblog());
  }

  private String getSafe(final String value) {
    return TextUtils.escapeHtml(value);
  }
}
