/* $HeadURL::                                                                            $
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

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.SubClassResolver;
import org.topazproject.otm.metadata.RdfDefinition;

/**
 * This specifies a representation that is text and that should be indexed for text searching.
 * All this does is mark the body as searchable.
 *
 * @author Ronald Tschalär
 */
@Entity
public class TextRepresentation extends Representation {
  private static final long serialVersionUID = 6290224164799186321L;

  /**
   * No argument constructor for OTM to instantiate.
   */
  public TextRepresentation() {
  }

  /**
   * Creates a new TextRepresentation object. The id of the object is
   * derived from the doi of the ObjectInfo and the representation name.
   *
   * @param object the object this representation belongs to
   * @param name the name of this representation
   */
  public TextRepresentation(ObjectInfo object, String name) {
    super(object, name);
  }

  /**
   * Set the body. The string is UTF-8 encoded to create the byte[]. This assumes a body
   * already exists and replaces its contents.
   *
   * @param body the body
   */
  public void setBodyAsText(String body) {
    try {
      super.getBody().writeAll(body.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException("Unexpected problem with UTF-8", uee);
    }
  }

  /**
   * Get the body as a String. The encoding is assumed to be UTF-8.
   *
   * @return the body
   */
  public String getBodyAsText() {
    try {
      return new String(super.getBody().readAll(), "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException("Unexpected problem with UTF-8", uee);
    }
  }

  /**
   * A {@link org.topazproject.otm.SubClassResolver} which detects Representation's with a
   * content-type of 'text/...' and selects this class instead.
   * @param superEntity       the root of the entity hierarchy that the resolved class
   *                          must be a sub-class-of
   * @param instantiatableIn  the entity mode that the resolved sub-class must be instantiatable in
   * @param sf                the session factory
   * @param typeUris          the rdf:type values found in the data
   * @param statements        the data
   * @return                  the resolved class or null
   */
  @SubClassResolver
  public static ClassMetadata resolve(ClassMetadata superEntity, EntityMode instantiatableIn,
                                      SessionFactory sf, Collection<String> typeUris,
                                      TripleStore.Result statements) {
    if (instantiatableIn != EntityMode.POJO ||
        !superEntity.isAssignableFrom(sf.getClassMetadata(Representation.class), EntityMode.POJO))
      return null;

    String p = ((RdfDefinition) sf.getClassMetadata(Representation.class).
                                   getMapperByName("contentType").getDefinition()).getUri();

    if (statements.getFValues().get(p).get(0).startsWith("text/"))
      return sf.getClassMetadata(TextRepresentation.class);
    else
      return sf.getClassMetadata(Representation.class);
  }

}
