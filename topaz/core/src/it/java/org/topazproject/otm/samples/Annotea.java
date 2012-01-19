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

import java.util.Date;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Blob;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * Annotea meta-data.
 *
 * @author Pradeep Krishnan
 */
@Entity(model = "ri")
@UriPrefix(Annotea.NS)
public class Annotea {
  /**
   * DOCUMENT ME!
   */
  public static final String NS = "http://www.w3.org/2000/10/annotation-ns#";
  private Date                                              created;
  private Body                                              body;
  @Predicate(uri = Rdf.rdf + "type")
  private String                                            type;
  @Predicate(uri = Rdf.dc + "creator")
  private String                                            creator;
  @Predicate(uri = Rdf.dc + "title")
  private String                                            title;
  @Predicate(uri = Rdf.dc_terms + "mediator")
  private String                                            mediator;
  @Predicate(uri = Rdf.topaz + "state")
  private int                                               state;

  // Embedding example
  /**
   * DOCUMENT ME!
   */
  public SampleEmbeddable foobar;

  /**
   * Creates a new Annotea object.
   */
  public Annotea() {
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
   * @return body.
   */
  public Body getBody() {
    return body;
  }

  /**
   * Set body.
   *
   * @param body the value to set.
   */
  public void setBody(Body body) {
    this.body = body;
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

  public static class Body {
    @Id
    @GeneratedValue
    private String                   id;
    @Blob
    private byte[]                   blob;

    public Body() {
    }

    public Body(byte[] blob) {
      this.blob = blob;
    }

    /**
     * Get id.
     *
     * @return id as String.
     */
    public String getId() {
      return id;
    }

    /**
     * Set id.
     *
     * @param id the value to set.
     */
    public void setId(String id) {
      this.id = id;
    }

    /**
     * Get blob.
     *
     * @return blob as byte[].
     */
    public byte[] getBlob() {
      return blob;
    }

    /**
     * Set blob.
     *
     * @param blob the value to set.
     */
    public void setBlob(byte[] blob) {
      this.blob = blob;
    }
  }
}
