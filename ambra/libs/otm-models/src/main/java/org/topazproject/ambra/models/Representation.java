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
package org.topazproject.ambra.models;

import java.util.Date;

import org.topazproject.otm.FetchType;
import org.topazproject.otm.Session;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.event.PostLoadEventListener;
import org.topazproject.otm.event.PreInsertEventListener;
import org.topazproject.otm.mapping.Mapper;

/**
 * This holds the information returned about a representation of an object.
 *
 * @author Pradeep Krishnan
 */
@Entity(graph = "ri", types = {"topaz:Representation"})
public class Representation extends Blob implements PostLoadEventListener, PreInsertEventListener {
  private static final long serialVersionUID = 8927830952382002736L;

  private String     id;
  private String     name;
  private String     contentType;
  private long       size;
  private Date       lastModified;
  private ObjectInfo object;

  private transient boolean modified = true;

  /**
   * No argument constructor for OTM to instantiate.
   */
  public Representation() {
  }

  /**
   * Creates a new Representation object. The id of the object is
   * derived from the doi of the ObjectInfo and the representation name.
   *
   * @param object the object this representation belongs to
   * @param name the name of this representation
   */
  public Representation(ObjectInfo object, String name) {
    setObject(object);
    setName(name);
    setId(object.getId().toString() + "/" + name);
  }

  /**
   * Get the name of the representation.
   *
   * @return the name.
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the representation.
   *
   * @param name the name.
   */
  @Predicate(uri = "dcterms:identifier")
  public void setName(String name) {
    this.name = name;
    modified = true;
  }

  /**
   * Get the object that this representation represents.
   *
   * @return the doi
   */
  public ObjectInfo getObject() {
    return object;
  }

  /**
   * Set the object that this representation represents.
   *
   * @param object the object
   */
  @Predicate(uri = "topaz:hasRepresentation", inverse=Predicate.BT.TRUE,
             notOwned=Predicate.BT.TRUE, fetch=FetchType.eager)
  public void setObject(ObjectInfo object) {
    this.object = object;
    modified = true;
  }

  /**
   * Get the mime-type of the content of the representation.
   *
   * @return the content type.
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Set the mime-type of the content of the representation.
   *
   * @param contentType the content type.
   */
  @Predicate(uri = "topaz:contentType")
  public void setContentType(String contentType) {
    this.contentType = contentType;
    modified = true;
  }

  /**
   * Get the object size of this representation.
   *
   * @return the object size.
   */
  public long getSize() {
    return size;
  }

  /**
   * Set the object size of this representation.
   *
   * @param size the object size
   */
  @Predicate(uri = "topaz:objectSize")
  public void setSize(long size) {
    this.size = size;
    modified = true;
  }

  /**
   * Return the last-modified date.
   *
   * @return the last-modified date
   */
  public Date getLastModified() {
    return lastModified;
  }

  /**
   * Set the last-modified date.
   *
   * @param lastModified the date the object was last modified
   */
  @Predicate(uri = "dcterms:modified", dataType = "xsd:dateTime")
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * Get id.
   *
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * Set id.
   *
   * @param id the value to set.
   */
  @Id @GeneratedValue(uriPrefix = "id:representation/")
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public void setBody(org.topazproject.otm.Blob body) {
    super.setBody(body);
  }

  public void onPostLoad(Session session, Object object) {
    modified = false;
  }

  public void onPostLoad(Session session, Object object, Mapper field) {
  }

  public void onPreInsert(Session session, Object object) {
    if (modified || getBody().getChangeState() != org.topazproject.otm.Blob.ChangeState.NONE) {
      setLastModified(new Date());
      modified = false;
    }
  }
}
