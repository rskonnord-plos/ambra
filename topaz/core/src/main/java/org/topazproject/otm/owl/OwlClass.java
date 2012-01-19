/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.owl;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Id;

/**
 * Represents and Owl class in the otm.
 *
 * @author Eric Brown
 */
@Entity(model="metadata", type=Rdf.owl + "Class")
public class OwlClass {
  @Id
  private URI owlClass;
  @Predicate(uri = Rdf.rdfs + "subClassOf")
  private List<URI> superClasses = new ArrayList<URI>();
  @Predicate(uri = Rdf.topaz + "inModel")
  private URI model;

  /**
   * Get the URI for this owl class.
   *
   * @return the URI
   */
  public URI getOwlClass() {
    return owlClass;
  }

  /**
   * Set the URI of this owl class.
   *
   * @param owlClass The URI representing this class.
   */
  public void setOwlClass(URI owlClass) {
    this.owlClass = owlClass;
  }

  /**
   * Get the list of owl super classes.
   *
   * @return the list of super classes.
   */
  public List<URI> getSuperClasses() {
    return superClasses;
  }

  /**
   * Set the list of super classes.
   *
   * @param superClasses the list of super classes.
   */
  public void setSuperClasses(List<URI> superClasses) {
    this.superClasses = superClasses;
  }

  /**
   * Add a super class.
   *
   * @param superClass the super class to add
   */
  public void addSuperClass(URI superClass) {
    this.superClasses.add(superClass);
  }

  /**
   * Get the model this class is stored in
   *
   * @return the model this class is stored in
   */
  public URI getModel() {
    return model;
  }

  /**
   * Set the model this class is stored in
   *
   * @param model the model this class is stored in
   */
  public void setModel(URI model) {
    this.model = model;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (URI superClass: superClasses) {
      if (sb.length() > 0)
        sb.append(' ');
      sb.append("<").append(superClass.toString()).append(">");
    }

    if (sb.length() > 0) {
      sb.insert(0, " (");
      sb.append(")");
    }

    sb.insert(0, "<" + owlClass.toString() + ">");

    if (model != null)
      sb.append(" in <").append(model.toString()).append(">");

    return sb.toString();
  }
}
