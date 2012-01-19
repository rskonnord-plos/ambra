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
package org.topazproject.otm.samples;

import java.util.Date;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Blob;
import org.topazproject.otm.annotations.Embedded;
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
@Entity(graph = "ri")
@UriPrefix(Annotea.NS)
public class Annotea {
  /**
   * DOCUMENT ME!
   */
  public static final String NS = "http://www.w3.org/2000/10/annotation-ns#";
  private Date                                              created;
  private Body                                              body;
  private String                                            type;
  private String                                            creator;
  private String                                            title;
  private String                                            mediator;
  private int                                               state;
  private SampleEmbeddable foobar;

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
  @Predicate(uri = Rdf.rdf + "type")
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
  @Predicate(uri = Rdf.dc + "creator")
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
  @Predicate
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
  @Predicate
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
  @Predicate(uri = Rdf.dc + "title")
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
  @Predicate(uri = Rdf.dc_terms + "mediator")
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
  @Predicate(uri = Rdf.topaz + "state")
  public void setState(int state) {
    this.state = state;
  }

  /**
   * Get foobar.
   *
   * @return foobar as SampleEmbeddable.
   */
  public SampleEmbeddable getFoobar() {
    return foobar;
  }

  /**
   * Set foobar.
   *
   * @param foobar the value to set.
   */
  @Embedded
  public void setFoobar(SampleEmbeddable foobar) {
    this.foobar = foobar;
  }

  public static class Body {
    private String                   id;
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
    @Id
    @GeneratedValue
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
    @Blob
    public void setBlob(byte[] blob) {
      this.blob = blob;
    }
  }
}
