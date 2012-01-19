/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;


import java.net.URI;
import java.util.Date;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;

/**
 * This is the base class to capture common predicates between Annotations and
 * Replies (discussion threads).
 *
 * @author Pradeep Krishnan
 */
@Entity(model = "ri")
@UriPrefix(Annotea.W3C_NS)
public abstract class Annotea {
  /**
   * Annotea Namespace URI
   */
  public static final String W3C_NS = "http://www.w3.org/2000/10/annotation-ns#";
  public static final String TOPAZ_NS =  Rdf.topaz + "2008/01/annotation-ns#";
  public static final String W3C_TYPE_NS = "http://www.w3.org/2000/10/annotationType#";
  public static final String TOPAZ_TYPE_NS = Rdf.topaz + "2008/01/annotationType#";

  private Date                                              created;
  @Predicate(uri = Rdf.rdf + "type", dataType=Rdf.xsd + "anyURI")
  private String                                            type;
  @Predicate(uri = Rdf.dc + "creator")
  private String                                            creator;
  @Predicate(uri = Rdf.topaz + "anonymousCreator")
  private String                                            anonymousCreator;
  @Predicate(uri = Rdf.dc + "title")
  private String                                            title;
  @Predicate(uri = Rdf.dc_terms + "mediator")
  private String                                            mediator;
  @Predicate(uri = Rdf.topaz + "state", dataType = Predicate.UNTYPED)
  private int                                               state;

  public abstract URI getId();
  
  /**
   * Get creator.
   *
   * @return creator as String.
   */
  public String getCreator() {
    return creator;
  }

  /**
   * Set creator.
   *
   * @param creator the value to set.
   */
  public void setCreator(String creator) {
    this.creator = creator;
  }

  /**
   * Get creator.
   *
   * @return creator as String.
   */
  public String getAnonymousCreator() {
    return anonymousCreator;
  }

  /**
   * Set creator.
   *
   * @param creator the value to set.
   */
  public void setAnonymousCreator(String creator) {
    this.anonymousCreator = creator;
  }

  /**
   * Get created.
   *
   * @return created as Date.
   */
  public Date getCreated() {
    return created;
  }

  /**
   * Set created.
   *
   * @param created the value to set.
   */
  public void setCreated(Date created) {
    this.created = created;
  }


  /**
   * Get title.
   *
   * @return title as String.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set title.
   *
   * @param title the value to set.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Get mediator.
   *
   * @return mediator as String.
   */
  public String getMediator() {
    return mediator;
  }

  /**
   * Set mediator.
   *
   * @param mediator the value to set.
   */
  public void setMediator(String mediator) {
    this.mediator = mediator;
  }

  /**
   * Get state.
   *
   * @return state as int.
   */
  public int getState() {
    return state;
  }

  /**
   * Set state.
   *
   * @param state the value to set.
   */
  public void setState(int state) {
    this.state = state;
  }
  /**
   * Get type.
   *
   * @return type as String.
   */
  public String getType() {
    return type;
  }

  /**
   * Set type.
   *
   * @param type the value to set.
   */
  public void setType(String type) {
    this.type = type;
  }
}
