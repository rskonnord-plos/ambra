/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.samples;

import java.net.URI;
import java.util.Date;

import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Model;
import org.topazproject.otm.annotations.Ns;
import org.topazproject.otm.annotations.Rdf;

/**
 * Annotia meta-data.
 *
 * @author Pradeep Krishnan
 */
@Model("ri")
@Ns(Annotia.NS)
public class Annotia {
  /**
   * DOCUMENT ME!
   */
  public static final String NS = "http://www.w3.org/2000/10/annotation-ns#";
  @Id
  private URI                                   id;
  private Date                                  created;
  private String                                body;
  @Rdf(Rdf.rdf + "type")
  private String                                type;
  @Rdf(Rdf.dc + "creator")
  private String                                creator;
  @Rdf(Rdf.dc + "title")
  private String                                title;
  @Rdf(Rdf.dc_terms + "mediator")
  private String                                mediator;
  @Rdf(Rdf.topaz + "state")
  private int                                   state;

  // Embedding example
  /**
   * DOCUMENT ME!
   */
  public SampleEmbeddable foobar;

  /**
   * Creates a new Annotia object.
   */
  public Annotia() {
  }

  /**
   * Creates a new Annotia object.
   */
  public Annotia(URI id) {
    this.id = id;
  }

  /**
   * Get annotation type.
   *
   * @return annotation type as String.
   */
  public String getType() {
    return type;
  }

  /**
   * Set annotation type.
   *
   * @param type the value to set.
   */
  public void setType(String type) {
    this.type = type;
  }

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
   * Get body.
   *
   * @return body as String.
   */
  public String getBody() {
    return body;
  }

  /**
   * Set body.
   *
   * @param body the value to set.
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * Get id.
   *
   * @return id as URI.
   */
  public URI getId() {
    return id;
  }

  /**
   * Set id.
   *
   * @param id the value to set.
   */
  public void setId(URI id) {
    this.id = id;
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
}
