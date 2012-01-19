/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Topaz, Inc.
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
package org.topazproject.ambra.models;

import java.io.Serializable;
import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;

/**
 * Ambra articles have a category and sub-category. Theoretically these are well defined
 * but in practice they seem to be somewhat adhoc (at this time). The sub-category is
 * optional.<p>
 *
 * On ingestion, category information is abstracted from the dc:subject terms.
 *
 * @author Eric Brown
 */
@Entity(graph = "ri")
public class Category implements Serializable {
  private static final long serialVersionUID = -3215078944392749142L;

  private URI id;
  private String mainCategory;
  private String subCategory;

  /**
   * @return the id
   */
  public URI getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  @Id @GeneratedValue(uriPrefix = "id:category/")
  public void setId(URI id) {
    this.id = id;
  }

  /**
   * @return the mainCategory
   */
  public String getMainCategory() {
    return mainCategory;
  }

  /**
   * @param mainCategory the value of the main category
   */
  @Predicate(uri = "topaz:mainCategory")
  public void setMainCategory(String mainCategory) {
    this.mainCategory = mainCategory;
  }

  /**
   * @return the subCategory
   */
  public String getSubCategory() {
    return subCategory;
  }

  /**
   * @param subCategory the value of the sub-category
   */
  @Predicate(uri = "topaz:subCategory")
  public void setSubCategory(String subCategory) {
    this.subCategory = subCategory;
  }
}
