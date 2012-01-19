/*
 * $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.access;

/**
 * A subject attribute. Contains a category in addition to the Attribute values.
 *
 * @author Pradeep Krishnan
 */
public class SubjectAttribute extends Attribute {
  private String category;

  /**
   * Creates a new SubjectAttribute object.
   *
   * @param category the subject category or <code>null</code> for default.
   * @param name the name of the attribute
   * @param type the type of the attribute
   * @param value the text encoded value of the attribute
   */
  public SubjectAttribute(String category, String name, String type, String value) {
    super(name, type, value);
    this.category = category;
  }

  /**
   * Creates a new SubjectAttribute object.
   */
  public SubjectAttribute() {
    category = null;
  }

  /**
   * Get the subject category.
   *
   * @return category as String. <code>null</code> represents default category.
   */
  public String getCategory() {
    return category;
  }

  /**
   * Set the subject category
   *
   * @param category the value to set. <code>null</code> for default category.
   */
  public void setCategory(String category) {
    this.category = category;
  }
}
