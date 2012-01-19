/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

/**
 * PLOSOne articles have a category and sub-category. Theoretically these are well defined
 * but in practice they seem to be somewhat adhoc (at this time). The sub-category is
 * optional.<p>
 *
 * On ingestion, category information is abstracted from the dc:subject terms.
 *
 * @author Eric Brown
 */
@Entity(model = "ri")
public class Category {
  @Id
  private URI id;
  @Predicate(uri = Rdf.topaz + "isPID")
  private String pid;
  @Predicate(uri = Rdf.topaz + "articleState")
  private int state;
  @Predicate(uri = Rdf.topaz + "mainCategory")
  private String mainCategory;
  @Predicate(uri = Rdf.topaz + "subCategory")
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
  public void setMainCategory(String mainCategory) {
    this.mainCategory = mainCategory;
  }

  /**
   * @return a literal representation of the pid this category is for
   */
  public String getPid() {
    return pid;
  }

  /**
   * @param pid a literal representation of the article's pid
   */
  public void setPid(String pid) {
    this.pid = pid;
  }

  /**
   * @return the article state
   */
  public int getState() {
    return state;
  }

  /**
   * @param state the article state to set
   */
  public void setState(int state) {
    this.state = state;
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
  public void setSubCategory(String subCategory) {
    this.subCategory = subCategory;
  }
}
