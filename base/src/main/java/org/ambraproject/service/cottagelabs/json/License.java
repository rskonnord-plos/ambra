/*
 * Copyright (c) 2006-2013 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
package org.ambraproject.service.cottagelabs.json;

import com.google.gson.annotations.Expose;
import javax.annotation.Generated;

@Generated("com.googlecode.jsonschema2pojo")
public class License {
  @Expose
  private String status;
  @Expose
  private Boolean open_access;
  @Expose
  private Provenance provenance;
  @Expose
  private String maintainer;
  @Expose
  private String description;
  @Expose
  private String family;
  @Expose
  private String title;
  @Expose
  private Boolean domain_data;
  @Expose
  private Boolean NC;
  @Expose
  private Boolean ND;
  @Expose
  private String jurisdiction;
  @Expose
  private String url;
  @Expose
  private String version;
  @Expose
  private Boolean domain_content;
  @Expose
  private Boolean is_okd_compliant;
  @Expose
  private Boolean is_osi_compliant;
  @Expose
  private Boolean SA;
  @Expose
  private Boolean domain_software;
  @Expose
  private String type;
  @Expose
  private Boolean BY;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Boolean getOpen_access() {
    return open_access;
  }

  public void setOpen_access(Boolean open_access) {
    this.open_access = open_access;
  }

  public Provenance getProvenance() {
    return provenance;
  }

  public void setProvenance(Provenance provenance) {
    this.provenance = provenance;
  }

  public String getMaintainer() {
    return maintainer;
  }

  public void setMaintainer(String maintainer) {
    this.maintainer = maintainer;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getFamily() {
    return family;
  }

  public void setFamily(String family) {
    this.family = family;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Boolean getDomain_data() {
    return domain_data;
  }

  public void setDomain_data(Boolean domain_data) {
    this.domain_data = domain_data;
  }

  public Boolean getNC() {
    return NC;
  }

  public void setNC(Boolean NC) {
    this.NC = NC;
  }

  public Boolean getND() {
    return ND;
  }

  public void setND(Boolean ND) {
    this.ND = ND;
  }

  public String getJurisdiction() {
    return jurisdiction;
  }

  public void setJurisdiction(String jurisdiction) {
    this.jurisdiction = jurisdiction;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Boolean getDomain_content() {
    return domain_content;
  }

  public void setDomain_content(Boolean domain_content) {
    this.domain_content = domain_content;
  }

  public Boolean getIs_okd_compliant() {
    return is_okd_compliant;
  }

  public void setIs_okd_compliant(Boolean is_okd_compliant) {
    this.is_okd_compliant = is_okd_compliant;
  }

  public Boolean getIs_osi_compliant() {
    return is_osi_compliant;
  }

  public void setIs_osi_compliant(Boolean is_osi_compliant) {
    this.is_osi_compliant = is_osi_compliant;
  }

  public Boolean getSA() {
    return SA;
  }

  public void setSA(Boolean SA) {
    this.SA = SA;
  }

  public Boolean getDomain_software() {
    return domain_software;
  }

  public void setDomain_software(Boolean domain_software) {
    this.domain_software = domain_software;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Boolean getBY() {
    return BY;
  }

  public void setBY(Boolean BY) {
    this.BY = BY;
  }
}
