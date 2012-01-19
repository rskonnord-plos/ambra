/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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
package org.plos.models;

import java.io.Serializable;
import java.net.URI;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.SimpleTimeZone;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Predicate.PropType;

/**
 * This is the base class to capture common predicates between Annotations and
 * Replies (discussion threads).
 *
 * @author Pradeep Krishnan
 */
@Entity(model = "ri")
@UriPrefix(Annotea.W3C_NS)
public abstract class Annotea implements Serializable {
  /**
   * Annotea Namespace URI
   */
  public static final String W3C_NS = "http://www.w3.org/2000/10/annotation-ns#";
  public static final String TOPAZ_NS =  Rdf.topaz + "2008/01/annotation-ns#";
  public static final String W3C_TYPE_NS = "http://www.w3.org/2000/10/annotationType#";
  public static final String TOPAZ_TYPE_NS = Rdf.topaz + "2008/01/annotationType#";

  private Date                                              created;
  @Predicate(uri = Rdf.rdf + "type", type = PropType.OBJECT)
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

  static final long serialVersionUID = 7228784439724175998L;
  private static SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  static {
    fmt.setTimeZone(new SimpleTimeZone(0, "UTC"));
  }

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

  /**
   * Gets the created date as a formatted String in UTC.
   *
   * @retun created date as a string
   */
  public String getCreatedAsString() {
    if (created == null)
      return null;
    synchronized(fmt) {
      return fmt.format(created);
    }
  }

  /**
   * Human friendly string for display and debugging.
   *
   * @return String for human consumption.
   */
  public String toString() {
    String name = getClass().getName();
    return name.substring(1+name.lastIndexOf('.')) + ": {"
            + "id: " + getId()
            + ", created: " + getCreated()
            + ", creator: " + getCreator()
            + ", title: "   + getTitle() + "}";
  }
}
